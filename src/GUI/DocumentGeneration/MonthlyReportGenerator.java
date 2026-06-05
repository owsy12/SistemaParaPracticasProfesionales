package GUI.DocumentGeneration;

import Logic.DTOs.MonthlyReport;
import Logic.DTOs.ReportActivity;
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

public class MonthlyReportGenerator {

    private static final Logger LOGGER = Logger.getLogger(MonthlyReportGenerator.class.getName());
    private static final String TEMPLATE = "/GUI/DocumentGeneration/basedocuments/reporteMensual.docx";
    private static final Pattern MARKER     = Pattern.compile("\\{\\{([^}]+)}}");
    private static final Pattern TEXT_IN_RUN = Pattern.compile("<w:t[^>]*>([^<]*)</w:t>");

    private MonthlyReportGenerator() {
    }

    public static String generate(MonthlyReport report,
                                   ReportGenerationContext context,
                                   ReportContent content) throws IOException {
        List<ReportActivity> activities = content.getActivities();
        Map<String, String> values = buildValues(report, context);
        byte[] docxBytes = fillTemplate(values, activities);
        byte[] pdfBytes = DocxToPdfConverter.convert(docxBytes);

        String storagePath = buildStoragePath(report, context.getMatricula());
        saveFile(pdfBytes, storagePath);

        String monthLabel = safe(report.getMonth()) + "_" + report.getYear();
        String fileName = "Reporte_Mensual_" + monthLabel + ".pdf";
        showSaveDialog(pdfBytes, fileName, context.getOwnerWindow());

        return storagePath;
    }

    private static Map<String, String> buildValues(MonthlyReport report, ReportGenerationContext context) {
        Map<String, String> values = new HashMap<>();
        values.put("reportnumber", String.valueOf(report.getReportNumber()));
        values.put("month", safe(report.getMonth()) + " " + report.getYear());
        values.put("report_hours", String.valueOf(report.getMonthlyHours()));
        values.put("total_hours", String.valueOf(context.getTotalApprovedHours()));
        values.put("intern", context.getInternFullName());
        values.put("block", safe(report.getBlock()));
        values.put("section", safe(report.getSection()));
        values.put("technician", context.getTechnicianName());
        values.put("profesor", context.getProfessorName());
        return values;
    }

    private static byte[] fillTemplate(Map<String, String> values,
                                        List<ReportActivity> activities) throws IOException {
        InputStream templateStream = MonthlyReportGenerator.class.getResourceAsStream(TEMPLATE);
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
                    xml = expandActivityRows(xml, activities);
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
        int markerPosition = -1;
        if (hasActivities) {
            markerPosition = xml.indexOf("{{activity_01}}");
        }
        int rowStart = -1;
        if (markerPosition >= 0) {
            rowStart = xml.lastIndexOf("<w:tr ", markerPosition);
            if (rowStart < 0) {
                rowStart = xml.lastIndexOf("<w:tr>", markerPosition);
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
            result = buildExpandedXml(xml, activities, rowStart, blockEnd);
        }
        return result;
    }

    private static String buildExpandedXml(String xml, List<ReportActivity> activities,
                                             int rowStart, int blockEnd) {
        String templateBlock = xml.substring(rowStart, blockEnd);
        StringBuilder expanded = new StringBuilder();
        for (int i = 0; i < activities.size(); i++) {
            String newIdx = String.format("%02d", i + 1);
            String block = templateBlock;
            boolean notFirstRow = i > 0;
            if (notFirstRow) {
                block = block.replace("activity_01", "activity_" + newIdx);
            }
            Map<String, String> rowValues = buildRowValues(activities.get(i), i + 1);
            block = replaceMarkers(block, rowValues);
            expanded.append(block);
        }
        String prefix = xml.substring(0, rowStart);
        String suffix = xml.substring(blockEnd);
        String result = prefix + expanded.toString() + suffix;
        return result;
    }

    private static Map<String, String> buildRowValues(ReportActivity activity, int index) {
        String key = String.format("%02d", index);
        Map<String, String> row = new HashMap<>();
        row.put("activity_" + key, safe(activity.getActivityName()));
        row.put("activity_" + key + "_period", safe(activity.getPeriod()));
        row.put("activity_" + key + "_observations", safe(activity.getObservaciones()));
        return row;
    }

    private static String normalizeMarkers(String xml) {
        String cleaned = xml.replaceAll("<w:proofErr[^>]*/> *", "");
        StringBuilder result = new StringBuilder();
        int position = 0;

        while (position < cleaned.length()) {
            int openPosition = cleaned.indexOf("{{", position);
            boolean noMore = openPosition < 0;
            if (noMore) {
                result.append(cleaned, position, cleaned.length());
                break;
            }
            int closePosition = cleaned.indexOf("}}", openPosition + 2);
            boolean unclosed = closePosition < 0;
            if (unclosed) {
                result.append(cleaned, position, cleaned.length());
                break;
            }
            String between = cleaned.substring(openPosition + 2, closePosition);
            result.append(cleaned, position, openPosition);
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
            position = closePosition + 2;
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

    private static String buildStoragePath(MonthlyReport report, String matricula) {
        String path = "storage/intern_" + matricula
                + "/project_" + report.getIdProject()
                + "/reports/monthly_" + report.getIdReport() + ".pdf";
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
        escaped = escaped.replace("&", "&amp;")
                         .replace("<", "&lt;")
                         .replace(">", "&gt;")
                         .replace("\"", "&quot;")
                         .replace("'", "&apos;");
        return escaped;
    }

    private static String safe(String value) {
        String result = (value != null) ? value : "";
        return result;
    }
}
