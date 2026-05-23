package GUI.Utils;

import Logic.DTOs.MonthlyReport;
import Logic.DTOs.PartialAndFinalReport;
import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportDeliverable;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportDocxGenerator {

    private static final Logger LOGGER = Logger.getLogger(ReportDocxGenerator.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String TEMPLATE_MENSUAL = "/GUI/Utils/basedocuments/reporteMensual.docx";
    private static final String TEMPLATE_PARCIAL = "/GUI/Utils/basedocuments/reporteParcial.docx";
    private static final String TEMPLATE_FINAL = "/GUI/Utils/basedocuments/reporteFinal.docx";
    private static final String TEMPLATE_AUTOEVAL = "/GUI/Utils/basedocuments/autoevaluacion.docx";

    private static final int WEEKS_PER_REPORT = 8;
    private static final int LIKERT_COLUMNS = 5;
    private static final int LIKERT_QUESTIONS = 10;

    private ReportDocxGenerator() {
    }

    public static String generateMonthlyReport(MonthlyReport report,
                                               ReportGenerationContext context,
                                               ReportContent content)
            throws IOException {

        Map<String, String> values = buildMonthlyValues(report, context);
        List<Map<String, String>> activityRows = buildMonthlyActivityRows(content.getActivities());
        List<DocxTemplateEngine.RowExpansion> expansions = buildExpansions(
                activityRows, "activity_01", 1);

        byte[] docxBytes = DocxTemplateEngine.fill(TEMPLATE_MENSUAL, values, expansions);
        byte[] pdfBytes = DocxToPdfConverter.convert(docxBytes);

        String internalPath = buildInternalPath(
                context.getMatricula(), report.getIdProyect(), "monthly", report.getIdReport());
        saveInternalCopy(pdfBytes, internalPath);

        String fileName = "Reporte_Mensual_" + nullSafe(report.getMonth())
                + "_" + report.getYear() + ".pdf";
        showPdfSaveDialog(pdfBytes, fileName, context.getOwnerWindow());

        return internalPath;
    }

    public static String generatePartialReport(PartialAndFinalReport report,
                                               ReportGenerationContext context,
                                               ReportContent content)
            throws IOException {

        Map<String, String> values = buildPartialValues(report, context);
        List<Map<String, String>> activityRows = buildPartialActivityRows(content.getActivities());
        List<DocxTemplateEngine.RowExpansion> expansions = buildExpansions(
                activityRows, "activity_01", 2);

        byte[] docxBytes = DocxTemplateEngine.fill(TEMPLATE_PARCIAL, values, expansions);
        byte[] pdfBytes = DocxToPdfConverter.convert(docxBytes);

        String internalPath = buildInternalPath(
                context.getMatricula(), report.getIdProyect(), "partial", report.getIdReport());
        saveInternalCopy(pdfBytes, internalPath);

        String fileName = "Reporte_Parcial_" + report.getReportNumber() + ".pdf";
        showPdfSaveDialog(pdfBytes, fileName, context.getOwnerWindow());

        return internalPath;
    }

    public static String generateFinalReport(PartialAndFinalReport report,
                                             ReportGenerationContext context,
                                             ReportContent content)
            throws IOException {

        Map<String, String> values = buildFinalValues(report, context);
        List<Map<String, String>> activityRows = buildFinalActivityRows(content.getActivities());
        List<Map<String, String>> deliverableRows = buildDeliverableRows(content.getDeliverables());

        List<DocxTemplateEngine.RowExpansion> expansions = new ArrayList<>();
        if (!activityRows.isEmpty()) {
            expansions.add(new DocxTemplateEngine.RowExpansion("activity_01", activityRows, 1));
        }
        if (!deliverableRows.isEmpty()) {
            expansions.add(new DocxTemplateEngine.RowExpansion(
                    "deliverable_result_01", deliverableRows, 1));
        }

        byte[] docxBytes = DocxTemplateEngine.fill(TEMPLATE_FINAL, values, expansions);
        byte[] pdfBytes = DocxToPdfConverter.convert(docxBytes);

        String internalPath = buildInternalPath(
                context.getMatricula(), report.getIdProyect(), "final", report.getIdReport());
        saveInternalCopy(pdfBytes, internalPath);

        String fileName = "Reporte_Final_" + report.getReportNumber() + ".pdf";
        showPdfSaveDialog(pdfBytes, fileName, context.getOwnerWindow());

        return internalPath;
    }

    public static String generateSelfEvaluation(int[] answers,
                                                String placeAndDate,
                                                ReportGenerationContext context,
                                                int idProject,
                                                int idSelfEvaluation)
            throws IOException {

        Map<String, String> values = buildSelfEvaluationValues(
                answers, placeAndDate, context);

        byte[] docxBytes = DocxTemplateEngine.fill(TEMPLATE_AUTOEVAL, values);
        byte[] pdfBytes = DocxToPdfConverter.convert(docxBytes);

        String internalPath = buildInternalPath(
                context.getMatricula(), idProject, "selfevaluation", idSelfEvaluation);
        saveInternalCopy(pdfBytes, internalPath);

        String fileName = "Autoevaluacion_" + context.getMatricula() + ".pdf";
        showPdfSaveDialog(pdfBytes, fileName, context.getOwnerWindow());

        return internalPath;
    }

    public static String buildInternalPath(String matricula,
                                           int idProject,
                                           String reportType,
                                           int idReport) {
        return "storage/intern_" + matricula
                + "/proyecto_" + idProject
                + "/reports/" + reportType + "_" + idReport + ".pdf";
    }

    private static Map<String, String> buildMonthlyValues(MonthlyReport report,
                                                          ReportGenerationContext context) {
        Map<String, String> values = new HashMap<>();
        values.put("reportnumber", String.valueOf(report.getIdReport()));
        values.put("month",        nullSafe(report.getMonth()) + " " + report.getYear());
        values.put("report_hours", String.valueOf(report.getMonthlyHours()));
        values.put("total_hours",  String.valueOf(context.getTotalApprovedHours()));
        values.put("intern",       context.getInternFullName());
        values.put("block",        nullSafe(report.getBlock()));
        values.put("section",      nullSafe(report.getSection()));
        values.put("technician",   context.getTechnicianName());
        values.put("profesor",     context.getProfessorName());
        return values;
    }

    private static Map<String, String> buildPartialValues(PartialAndFinalReport report,
                                                          ReportGenerationContext context) {
        Map<String, String> values = new HashMap<>();
        values.put("nrc",                 String.valueOf(report.getIdProyect()));
        values.put("school_term",         nullSafe(report.getPeriod()));
        values.put("intern",              context.getInternFullName());
        values.put("linked_organization", context.getOrganizationName());
        values.put("project",             context.getProjectName());
        values.put("report_term",         nullSafe(report.getPeriod()));
        values.put("hours",               String.valueOf(report.getCoveredHours()));
        values.put("report_date",         LocalDate.now().format(DATE_FORMATTER));
        values.put("report_number",       String.valueOf(report.getReportNumber()));
        values.put("Project_objective",   nullSafe(report.getGeneralObjective()));
        values.put("metodology",          nullSafe(report.getMethodology()));
        values.put("result",              nullSafe(report.getObtainedResults()));
        values.put("observations",        nullSafe(report.getObservations()));
        values.put("name",                context.getInternFullName());
        values.put("technician",          context.getTechnicianName());
        values.put("technician_position", context.getTechnicianPosition());
        values.put("profesor",            context.getProfessorName());
        return values;
    }

    private static Map<String, String> buildFinalValues(PartialAndFinalReport report,
                                                        ReportGenerationContext context) {
        Map<String, String> values = new HashMap<>();
        values.put("nrc",                 String.valueOf(report.getIdProyect()));
        values.put("school_term",         nullSafe(report.getPeriod()));
        values.put("name",                context.getInternFullName());
        values.put("organization",        context.getOrganizationName());
        values.put("project",             context.getProjectName());
        values.put("hours",               String.valueOf(report.getCoveredHours()));
        values.put("date",                LocalDate.now().format(DATE_FORMATTER));
        values.put("project_objective",   nullSafe(report.getGeneralObjective()));
        values.put("metodology",          nullSafe(report.getMethodology()));
        values.put("observations",        nullSafe(report.getObservations()));
        values.put("technician",          context.getTechnicianName());
        values.put("technician_position", context.getTechnicianPosition());
        values.put("profesor",            context.getProfessorName());
        return values;
    }

    private static Map<String, String> buildSelfEvaluationValues(int[] answers,
                                                                  String placeAndDate,
                                                                  ReportGenerationContext context) {
        Map<String, String> values = new HashMap<>();
        values.put("name",         context.getInternFullName());
        values.put("id",           context.getMatricula());
        values.put("organization", context.getOrganizationName());
        values.put("depart",       nullSafe(context.getOrganizationDepartment()));
        values.put("technician",   context.getTechnicianName());
        values.put("project",      context.getProjectName());
        values.put("place",        nullSafe(placeAndDate));
        values.put("date",         LocalDate.now().format(DATE_FORMATTER));

        int finalScore = 0;
        for (int questionNumber = 1; questionNumber <= LIKERT_QUESTIONS; questionNumber++) {
            int selectedValue = resolveAnswer(answers, questionNumber);
            finalScore += selectedValue;
            fillLikertRow(values, questionNumber, selectedValue);
        }
        values.put("final_score", String.valueOf(finalScore));
        return values;
    }

    private static int resolveAnswer(int[] answers, int questionNumber) {
        int resolvedAnswer = 0;
        if (answers != null && questionNumber - 1 < answers.length) {
            resolvedAnswer = answers[questionNumber - 1];
        }
        return resolvedAnswer;
    }

    private static void fillLikertRow(Map<String, String> values,
                                      int questionNumber,
                                      int selectedValue) {
        for (int columnNumber = 1; columnNumber <= LIKERT_COLUMNS; columnNumber++) {
            String cellValue = (columnNumber == selectedValue) ? "X" : "";
            values.put("p" + questionNumber + "_" + columnNumber, cellValue);
        }
    }

    private static List<DocxTemplateEngine.RowExpansion> buildExpansions(
            List<Map<String, String>> rows,
            String markerBase,
            int rowsPerItem) {
        List<DocxTemplateEngine.RowExpansion> expansions = new ArrayList<>();
        if (!rows.isEmpty()) {
            expansions.add(new DocxTemplateEngine.RowExpansion(markerBase, rows, rowsPerItem));
        }
        return expansions;
    }

    private static List<Map<String, String>> buildMonthlyActivityRows(
            List<ReportActivity> activities) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (activities != null) {
            int activityIndex = 1;
            for (ReportActivity activity : activities) {
                Map<String, String> rowMap = new HashMap<>();
                String indexLabel = formatIndex(activityIndex);
                rowMap.put("activity_" + indexLabel, nullSafe(activity.getActivityName()));
                rowMap.put("activity_" + indexLabel + "_period", nullSafe(activity.getPeriodo()));
                rowMap.put("activity_" + indexLabel + "_observations",
                        nullSafe(activity.getObservaciones()));
                rows.add(rowMap);
                activityIndex++;
            }
        }
        return rows;
    }

    private static List<Map<String, String>> buildPartialActivityRows(
            List<ReportActivity> activities) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (activities != null) {
            int activityIndex = 1;
            for (ReportActivity activity : activities) {
                Map<String, String> rowMap = new HashMap<>();
                rowMap.put("activity_" + formatIndex(activityIndex),
                        nullSafe(activity.getActivityName()));
                for (int weekNumber = 1; weekNumber <= WEEKS_PER_REPORT; weekNumber++) {
                    rowMap.put("a" + activityIndex + "_plan_s" + weekNumber,
                            activity.planCell(weekNumber));
                    rowMap.put("a" + activityIndex + "_real_s" + weekNumber,
                            activity.realCell(weekNumber));
                }
                rows.add(rowMap);
                activityIndex++;
            }
        }
        return rows;
    }

    private static List<Map<String, String>> buildFinalActivityRows(
            List<ReportActivity> activities) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (activities != null) {
            int activityIndex = 1;
            for (ReportActivity activity : activities) {
                Map<String, String> rowMap = new HashMap<>();
                rowMap.put("activity_" + formatIndex(activityIndex),
                        nullSafe(activity.getActivityName()));
                rowMap.put("a" + activityIndex + "_advance",
                        activity.getPorcentajeAvance() + "%");
                rowMap.put("a" + activityIndex + "_observations",
                        nullSafe(activity.getObservaciones()));
                rows.add(rowMap);
                activityIndex++;
            }
        }
        return rows;
    }

    private static List<Map<String, String>> buildDeliverableRows(
            List<ReportDeliverable> deliverables) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (deliverables != null) {
            int deliverableIndex = 1;
            for (ReportDeliverable deliverable : deliverables) {
                Map<String, String> rowMap = new HashMap<>();
                String indexLabel = formatIndex(deliverableIndex);
                rowMap.put("deliverable_result_" + indexLabel,
                        nullSafe(deliverable.getResultado()));
                rowMap.put("dr" + indexLabel + "_advance",
                        deliverable.getPorcentajeAvance() + "%");
                rowMap.put("dr" + indexLabel + "_observations",
                        nullSafe(deliverable.getObservaciones()));
                rows.add(rowMap);
                deliverableIndex++;
            }
        }
        return rows;
    }

    private static void saveInternalCopy(byte[] pdfBytes,
                                         String relativePath) throws IOException {
        Path path = Paths.get(relativePath);
        Files.createDirectories(path.getParent());
        Files.write(path, pdfBytes);
        LOGGER.log(Level.INFO, "Copia interna guardada en: {0}", path.toAbsolutePath());
    }

    private static void showPdfSaveDialog(byte[] pdfBytes,
                                          String defaultFileName,
                                          Window ownerWindow) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar PDF como...");
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo PDF (*.pdf)", "*.pdf"));

        File downloadsFolder = new File(System.getProperty("user.home"), "Downloads");
        if (downloadsFolder.exists()) {
            fileChooser.setInitialDirectory(downloadsFolder);
        }

        File selectedFile = (ownerWindow != null)
                ? fileChooser.showSaveDialog(ownerWindow) : null;

        if (selectedFile != null) {
            try (FileOutputStream fileOutput = new FileOutputStream(selectedFile)) {
                fileOutput.write(pdfBytes);
            }
            LOGGER.log(Level.INFO, "PDF guardado por el usuario en: {0}",
                    selectedFile.getAbsolutePath());
        }
    }

    private static String formatIndex(int index) {
        return String.format("%02d", index);
    }

    private static String nullSafe(String value) {
        String safe = value != null ? value : "";
        return safe;
    }
}
