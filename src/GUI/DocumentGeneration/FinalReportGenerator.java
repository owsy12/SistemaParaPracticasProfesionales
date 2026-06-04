package GUI.DocumentGeneration;

import Logic.DTOs.PartialAndFinalReport;
import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportDeliverable;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FinalReportGenerator {

    private static final Logger LOGGER = Logger.getLogger(FinalReportGenerator.class.getName());
    private static final String TEMPLATE = "/GUI/DocumentGeneration/basedocuments/reporteFinal.docx";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Pattern MARKER = Pattern.compile("\\{\\{([^}]+)}}");
    private static final Pattern TEXT_IN_RUN = Pattern.compile("<w:t[^>]*>([^<]*)</w:t>");

    private FinalReportGenerator() {
    }

    public static String generate(PartialAndFinalReport report, ReportGenerationContext context, ReportContent content) throws IOException {
        List<ReportActivity> activities = content.getActivities();
        List<ReportDeliverable> deliverables = content.getDeliverables();
        Map<String, String> values = buildValues(report, context);
        byte[] docxBytes = fillTemplate(values, content);
        byte[] pdfBytes = DocxToPdfConverter.convert(docxBytes);

        String storagePath = buildStoragePath(report, context.getMatricula());
        saveFile(pdfBytes, storagePath);

        String fileName = "Reporte_Final_" + report.getReportNumber() + ".pdf";
        showSaveDialog(pdfBytes, fileName, context.getOwnerWindow());

        return storagePath;
    }

    private static Map<String, String> buildValues(PartialAndFinalReport report,
                                                    ReportGenerationContext context) {
        Map<String, String> values = new HashMap<>();
        values.put("nrc", String.valueOf(report.getIdProject()));
        values.put("school_term", safe(report.getPeriod()));
        values.put("name", context.getInternFullName());
        values.put("organization", context.getOrganizationName());
        values.put("project", context.getProjectName());
        values.put("hours", String.valueOf(report.getCoveredHours()));
        values.put("date", LocalDate.now().format(DATE_FORMAT));
        values.put("project_objective", safe(report.getGeneralObjective()));
        values.put("metodology", safe(report.getMethodology()));
        values.put("observations", safe(report.getObservations()));
        values.put("technician", context.getTechnicianName());
        values.put("technician_position", context.getTechnicianPosition());
        values.put("profesor", context.getProfessorName());
        return values;
    }

    private static byte[] fillTemplate(Map<String, String> values,
                                        ReportContent content) throws IOException {
        InputStream templateStream = FinalReportGenerator.class.getResourceAsStream(TEMPLATE);
        if (templateStream == null) {
            throw new IOException("Plantilla no encontrada: " + TEMPLATE);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipInputStream zipIn = new ZipInputStream(templateStream);
             ZipOutputStream zipOut = new ZipOutputStream(out)) {

            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                byte[] data = readAllBytes(zipIn);
                if ("word/document.xml".equals(entry.getName())) {
                    String xml = new String(data, StandardCharsets.UTF_8);
                    xml = normalizeMarkers(xml);
                    xml = expandActivityRows(xml, content.getActivities());
                    xml = expandDeliverableRows(xml, content.getDeliverables());
                    xml = replaceMarkers(xml, values);
                    data = xml.getBytes(StandardCharsets.UTF_8);
                }
                zipOut.putNextEntry(new ZipEntry(entry.getName()));
                zipOut.write(data);
                zipOut.closeEntry();
            }
        }
        return out.toByteArray();
    }

    private static String expandActivityRows(String xml, List<ReportActivity> activities) {
        boolean hasActivities = activities != null && !activities.isEmpty();
        int markerPos = -1;
        if (hasActivities) {
            markerPos = xml.indexOf("{{activity_01}}");
        }
        int rowStart = -1;
        if (markerPos >= 0) {
            rowStart = xml.lastIndexOf("<w:tr ", markerPos);
            if (rowStart < 0) {
                rowStart = xml.lastIndexOf("<w:tr>", markerPos);
            }
        }
        int blockEnd = -1;
        if (rowStart >= 0) {
            int rowEnd = xml.indexOf("</w:tr>", rowStart);
            if (rowEnd >= 0) {
                blockEnd = rowEnd + "</w:tr>".length();
            }
        }
        String result = xml;
        if (blockEnd >= 0) {
            result = buildExpandedActivityXml(xml, activities, rowStart, blockEnd);
        }
        return result;
    }

    private static String buildExpandedActivityXml(String xml, List<ReportActivity> activities,
                                                     int rowStart, int blockEnd) {
        String templateBlock = xml.substring(rowStart, blockEnd);
        StringBuilder expanded = new StringBuilder();
        for (int i = 0; i < activities.size(); i++) {
            String newIdx = String.format("%02d", i + 1);
            String block = templateBlock;
            boolean notFirstRow = i > 0;
            if (notFirstRow) {
                block = block.replace("activity_01", "activity_" + newIdx);
                block = block.replace("a1_", "a" + (i + 1) + "_");
            }
            Map<String, String> rowValues = buildActivityRowValues(activities.get(i), i + 1);
            block = replaceMarkers(block, rowValues);
            expanded.append(block);
        }
        String prefix = xml.substring(0, rowStart);
        String suffix = xml.substring(blockEnd);
        String result = prefix + expanded.toString() + suffix;
        return result;
    }

    private static Map<String, String> buildActivityRowValues(ReportActivity activity, int index) {
        String key = String.format("%02d", index);
        Map<String, String> row = new HashMap<>();
        row.put("activity_" + key, safe(activity.getActivityName()));
        row.put("a" + index + "_advance", activity.getAdvancePercentage() + "%");
        row.put("a" + index + "_observations", safe(activity.getObservaciones()));
        return row;
    }

    private static String expandDeliverableRows(String xml, List<ReportDeliverable> deliverables) {
        boolean hasDeliverables = deliverables != null && !deliverables.isEmpty();
        int markerPos = -1;
        if (hasDeliverables) {
            markerPos = xml.indexOf("{{deliverable_result_01}}");
        }
        int rowStart = -1;
        if (markerPos >= 0) {
            rowStart = xml.lastIndexOf("<w:tr ", markerPos);
            if (rowStart < 0) {
                rowStart = xml.lastIndexOf("<w:tr>", markerPos);
            }
        }
        int blockEnd = -1;
        if (rowStart >= 0) {
            int rowEnd = xml.indexOf("</w:tr>", rowStart);
            if (rowEnd >= 0) {
                blockEnd = rowEnd + "</w:tr>".length();
            }
        }
        String result = xml;
        if (blockEnd >= 0) {
            result = buildExpandedDeliverableXml(xml, deliverables, rowStart, blockEnd);
        }
        return result;
    }

    private static String buildExpandedDeliverableXml(String xml, List<ReportDeliverable> deliverables,
                                                        int rowStart, int blockEnd) {
        String templateBlock = xml.substring(rowStart, blockEnd);
        StringBuilder expanded = new StringBuilder();
        for (int i = 0; i < deliverables.size(); i++) {
            String newIdx = String.format("%02d", i + 1);
            String block = templateBlock;
            boolean notFirstRow = i > 0;
            if (notFirstRow) {
                block = block.replace("deliverable_result_01", "deliverable_result_" + newIdx);
                block = block.replace("dr01_", "dr" + newIdx + "_");
            }
            Map<String, String> rowValues = buildDeliverableRowValues(deliverables.get(i), i + 1);
            block = replaceMarkers(block, rowValues);
            expanded.append(block);
        }
        String prefix = xml.substring(0, rowStart);
        String suffix = xml.substring(blockEnd);
        String result = prefix + expanded.toString() + suffix;
        return result;
    }

    private static Map<String, String> buildDeliverableRowValues(ReportDeliverable deliverable,
                                                                   int index) {
        String key = String.format("%02d", index);
        Map<String, String> row = new HashMap<>();
        row.put("deliverable_result_" + key, safe(deliverable.getResultado()));
        row.put("dr" + key + "_advance", deliverable.getAdvancePercentage() + "%");
        row.put("dr" + key + "_observations", safe(deliverable.getObservaciones()));
        return row;
    }

    private static String normalizeMarkers(String xml) {
        String cleaned = xml.replaceAll("<w:proofErr[^>]*/> *", "");
        StringBuilder result = new StringBuilder();
        int position = 0;

        while (position < cleaned.length()) {
            int openPos = cleaned.indexOf("{{", position);
            boolean noMore = openPos < 0;
            if (noMore) {
                result.append(cleaned, position, cleaned.length());
                break;
            }
            int closePos = cleaned.indexOf("}}", openPos + 2);
            boolean unclosed = closePos < 0;
            if (unclosed) {
                result.append(cleaned, position, cleaned.length());
                break;
            }
            String between = cleaned.substring(openPos + 2, closePos);
            result.append(cleaned, position, openPos);
            boolean isPlainText = !between.contains("<");
            if (isPlainText) {
                result.append("{{").append(between).append("}}");
            } else {
                Matcher textMatcher = TEXT_IN_RUN.matcher(between);
                StringBuilder markerName = new StringBuilder();
                while (textMatcher.find()) {
                    markerName.append(textMatcher.group(1));
                }
                result.append("{{").append(markerName.toString().trim()).append("}}");
            }
            position = closePos + 2;
        }
        return result.toString();
    }

    private static String replaceMarkers(String xml, Map<String, String> values) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = MARKER.matcher(xml);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String replacement = values.getOrDefault(key, "");
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(escapeXml(replacement)));
        }
        matcher.appendTail(buffer);
        String result = buffer.toString();
        return result;
    }

    private static String buildStoragePath(PartialAndFinalReport report, String matricula) {
        String path = "storage/intern_" + matricula
                + "/project_" + report.getIdProject()
                + "/reports/final_" + report.getIdReport() + ".pdf";
        return path;
    }

    private static void saveFile(byte[] pdfBytes, String storagePath) throws IOException {
        Path path = Paths.get(storagePath);
        Files.createDirectories(path.getParent());
        Files.write(path, pdfBytes);
        LOGGER.log(Level.INFO, "PDF guardado en: {0}", path.toAbsolutePath());
    }

    private static void showSaveDialog(byte[] pdfBytes, String fileName,
                                        Window window) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PDF como...");
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo PDF (*.pdf)", "*.pdf"));
        File downloads = new File(System.getProperty("user.home"), "Downloads");
        boolean downloadsExists = downloads.exists();
        if (downloadsExists) {
            fileChooser.setInitialDirectory(downloads);
        }
        boolean hasWindow = window != null;
        File selected = hasWindow ? fileChooser.showSaveDialog(window) : null;
        boolean fileSelected = selected != null;
        if (fileSelected) {
            try (FileOutputStream out = new FileOutputStream(selected)) {
                out.write(pdfBytes);
            }
        }
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(chunk)) != -1) {
            buffer.write(chunk, 0, bytesRead);
        }
        byte[] result = buffer.toByteArray();
        return result;
    }

    private static String escapeXml(String value) {
        String escaped = (value != null) ? value : "";
        escaped = escaped.replace("&", "&amp;").replace("<", "&lt;")
                         .replace(">", "&gt;").replace("\"", "&quot;")
                         .replace("'", "&apos;");
        return escaped;
    }

    private static String safe(String value) {
        String result = (value != null) ? value : "";
        return result;
    }
}
