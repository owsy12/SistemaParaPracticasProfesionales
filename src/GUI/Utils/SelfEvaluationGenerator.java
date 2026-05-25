package GUI.Utils;

import Logic.DTOs.SelfEvaluation;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelfEvaluationGenerator {

    private static final Logger LOGGER = Logger.getLogger(SelfEvaluationGenerator.class.getName());
    private static final String TEMPLATE = "/GUI/Utils/basedocuments/autoevaluacion.docx";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int LIKERT_QUESTIONS = 10;
    private static final int LIKERT_COLUMNS = 5;

    private SelfEvaluationGenerator() {
    }

    public static String generate(SelfEvaluation evaluation, ReportGenerationContext context) throws IOException {
        Map<String, String> values = buildValues(evaluation, context);

        byte[] docxBytes = DocxTemplateEngine.fill(TEMPLATE, values);
        byte[] pdfBytes = DocxToPdfConverter.convert(docxBytes);

        String storagePath = buildStoragePath(evaluation, context.getMatricula());
        saveFile(pdfBytes, storagePath);

        String fileName = "Autoevaluacion_" + context.getMatricula() + ".pdf";
        showSaveDialog(pdfBytes, fileName, context.getOwnerWindow());

        return storagePath;
    }

    private static Map<String, String> buildValues(SelfEvaluation evaluation,
                                                    ReportGenerationContext context) {
        Map<String, String> values = new HashMap<>();
        values.put("name",         context.getInternFullName());
        values.put("id",           context.getMatricula());
        values.put("organization", context.getOrganizationName());
        values.put("depart",       safe(context.getOrganizationDepartment()));
        values.put("technician",   context.getTechnicianName());
        values.put("project",      context.getProjectName());
        values.put("place",        safe(evaluation.getPlaceAndDate()));
        values.put("date",         LocalDate.now().format(DATE_FORMAT));
        values.put("final_score",  String.valueOf(evaluation.getFinalScore()));
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

    private static String buildStoragePath(SelfEvaluation evaluation, String matricula) {
        String path = "storage/intern_" + matricula
                + "/proyecto_" + evaluation.getIdProyect()
                + "/reports/selfevaluation_" + evaluation.getIdSelfEvalation() + ".pdf";
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
        if (downloads.exists()) {
            fileChooser.setInitialDirectory(downloads);
        }
        File selected = (window != null) ? fileChooser.showSaveDialog(window) : null;
        if (selected != null) {
            try (FileOutputStream out = new FileOutputStream(selected)) {
                out.write(pdfBytes);
            }
        }
    }

    private static String safe(String value) {
        String result = (value != null) ? value : "";
        return result;
    }
}
