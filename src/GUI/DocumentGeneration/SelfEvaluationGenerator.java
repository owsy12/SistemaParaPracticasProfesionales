package GUI.DocumentGeneration;

import Logic.DTOs.SelfEvaluation;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SelfEvaluationGenerator {

    private static final Logger LOGGER = Logger.getLogger(SelfEvaluationGenerator.class.getName());
    private static final String TEMPLATE = "/GUI/DocumentGeneration/basedocuments/autoevaluacion.docx";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int LIKERT_QUESTIONS = 10;
    private static final int LIKERT_COLUMNS = 5;
    private static final Pattern MARKER = Pattern.compile("\\{\\{([^}]+)}}");
    private static final Pattern TEXT_IN_RUN = Pattern.compile("<w:t[^>]*>([^<]*)</w:t>");

    private SelfEvaluationGenerator() {
    }

    public static String generate(SelfEvaluation evaluation, ReportGenerationContext context) throws IOException {
        Map<String, String> values = buildValues(evaluation, context);
        byte[] docxBytes = fillTemplate(values);
        byte[] pdfBytes = DocxToPdfConverter.convert(docxBytes);

        String storagePath = buildStoragePath(evaluation, context.getRegistrationNumber());
        saveFile(pdfBytes, storagePath);

        String fileName = "self_evaluation_" + context.getRegistrationNumber() + ".pdf";
        showSaveDialog(pdfBytes, fileName, context.getOwnerWindow());

        return storagePath;
    }

    private static Map<String, String> buildValues(SelfEvaluation evaluation,
                                                    ReportGenerationContext context) {
        Map<String, String> values = new HashMap<>();
        values.put("name", context.getInternFullName());
        values.put("id", context.getRegistrationNumber());
        values.put("organization", context.getOrganizationName());
        values.put("depart", safe(context.getOrganizationDepartment()));
        values.put("technician", context.getTechnicianName());
        values.put("project", context.getProjectName());
        values.put("place", safe(evaluation.getPlaceAndDate()));
        values.put("date", LocalDate.now().format(DATE_FORMAT));
        values.put("final_score", String.valueOf(evaluation.getFinalScore()));
        fillLikertValues(values, evaluation);
        return values;
    }

    private static void fillLikertValues(Map<String, String> values, SelfEvaluation evaluation) {
        int[] answers = collectAnswers(evaluation);
        for (int questionNumber = 1; questionNumber <= LIKERT_QUESTIONS; questionNumber++) {
            int selectedValue = answers[questionNumber - 1];
            for (int columnNumber = 1; columnNumber <= LIKERT_COLUMNS; columnNumber++) {
                String cellValue = (columnNumber == selectedValue) ? "X" : "";
                values.put("p" + questionNumber + "_" + columnNumber, cellValue);
            }
        }
    }

    private static int[] collectAnswers(SelfEvaluation evaluation) {
        int[] answers = new int[]{
                evaluation.getStatement01(), evaluation.getStatement02(),
                evaluation.getStatement03(), evaluation.getStatement04(),
                evaluation.getStatement05(), evaluation.getStatement06(),
                evaluation.getStatement07(), evaluation.getStatement08(),
                evaluation.getStatement09(), evaluation.getStatement10()
        };
        return answers;
    }

    private static byte[] fillTemplate(Map<String, String> values) throws IOException {
        InputStream templateStream = SelfEvaluationGenerator.class.getResourceAsStream(TEMPLATE);
        if (templateStream == null) {
            throw new IOException("Template not found: " + TEMPLATE);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipInputStream zipIn = new ZipInputStream(templateStream);
             ZipOutputStream zipOut = new ZipOutputStream(out)) {

            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                byte[] data = readAllBytes(zipIn);
                if ("word/document.xml".equals(entry.getName())) {
                    String xml = new String(data, StandardCharsets.UTF_8);
                    xml = reconstructSplitMarkers(xml);
                    xml = replaceMarkers(xml, values);
                    data = xml.getBytes(StandardCharsets.UTF_8);
                }
                zipOut.putNextEntry(new ZipEntry(entry.getName()));
                zipOut.write(data);
                zipOut.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
        return out.toByteArray();
    }

    private static String reconstructSplitMarkers(String xml) {
        String cleaned = xml.replaceAll("<w:proofErr[^>]*/> *", "");

        StringBuilder result = new StringBuilder();
        int position = 0;

        boolean scanning = position < cleaned.length();
        while (scanning) {
            int openPosition = cleaned.indexOf("{{", position);
            int closePosition = (openPosition >= 0) ? cleaned.indexOf("}}", openPosition + 2) : -1;
            boolean hasMarker = false;
            if (openPosition >= 0) {
                if (closePosition >= 0) {
                    hasMarker = true;
                }
            }
            if (!hasMarker) {
                result.append(cleaned, position, cleaned.length());
                scanning = false;
            } else {
                String between = cleaned.substring(openPosition + 2, closePosition);
                result.append(cleaned, position, openPosition);
                if (!between.contains("<")) {
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
                scanning = position < cleaned.length();
            }
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
        return buffer.toString();
    }

    private static String buildStoragePath(SelfEvaluation evaluation, String matricula) {
        String path = "storage/intern_" + matricula
                + "/project_" + evaluation.getIdProject()
                + "/self_evaluation/self_evaluation_" + evaluation.getIdSelfEvaluation() + ".pdf";
        return path;
    }

    private static void saveFile(byte[] pdfBytes, String storagePath) throws IOException {
        Path path = Paths.get(storagePath);
        Files.createDirectories(path.getParent());
        Files.write(path, pdfBytes);
        LOGGER.log(Level.INFO, "PDF saved to: {0}", path.toAbsolutePath());
    }

    private static void showSaveDialog(byte[] pdfBytes, String fileName, Window window) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PDF como...");
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo PDF (*.pdf)", "*.pdf"));
        File downloads = new File(System.getProperty("user.home"), "Downloads");
        if (downloads.exists()) {
            fileChooser.setInitialDirectory(downloads);
        }
        File selected = (window != null) ? fileChooser.showSaveDialog(window) : null;
        if (selected != null) {
            try (FileOutputStream fileOut = new FileOutputStream(selected)) {
                fileOut.write(pdfBytes);
            }
        }
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int bytesRead = inputStream.read(chunk);
        while (bytesRead != -1) {
            buffer.write(chunk, 0, bytesRead);
            bytesRead = inputStream.read(chunk);
        }
        return buffer.toByteArray();
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
