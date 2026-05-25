package GUI.Utils;

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

public class FinalReportGenerator {

    private static final Logger LOGGER = Logger.getLogger(FinalReportGenerator.class.getName());
    private static final String TEMPLATE = "/GUI/Utils/basedocuments/reporteFinal.docx";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int ROWS_PER_ACTIVITY = 1;

    private FinalReportGenerator() {
    }

    public static String generate(PartialAndFinalReport report,
                                   ReportGenerationContext context,
                                   ReportContent content) throws IOException {
        Map<String, String> values = buildValues(report, context);
        List<DocxTemplateEngine.RowExpansion> expansions = buildAllExpansions(content);

        byte[] docxBytes = DocxTemplateEngine.fill(TEMPLATE, values, expansions);
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
        values.put("nrc",                 String.valueOf(report.getIdProyect()));
        values.put("school_term",         safe(report.getPeriod()));
        values.put("name",                context.getInternFullName());
        values.put("organization",        context.getOrganizationName());
        values.put("project",             context.getProjectName());
        values.put("hours",               String.valueOf(report.getCoveredHours()));
        values.put("date",                LocalDate.now().format(DATE_FORMAT));
        values.put("project_objective",   safe(report.getGeneralObjective()));
        values.put("metodology",          safe(report.getMethodology()));
        values.put("observations",        safe(report.getObservations()));
        values.put("technician",          context.getTechnicianName());
        values.put("technician_position", context.getTechnicianPosition());
        values.put("profesor",            context.getProfessorName());
        return values;
    }

    private static List<DocxTemplateEngine.RowExpansion> buildAllExpansions(ReportContent content) {
        List<DocxTemplateEngine.RowExpansion> expansions = new ArrayList<>();
        List<Map<String, String>> activityRows = buildActivityRows(content.getActivities());
        if (!activityRows.isEmpty()) {
            expansions.add(new DocxTemplateEngine.RowExpansion("activity_01", activityRows, ROWS_PER_ACTIVITY));
        }
        List<Map<String, String>> deliverableRows = buildDeliverableRows(content.getDeliverables());
        if (!deliverableRows.isEmpty()) {
            expansions.add(new DocxTemplateEngine.RowExpansion("deliverable_result_01", deliverableRows, ROWS_PER_ACTIVITY));
        }
        return expansions;
    }

    private static List<Map<String, String>> buildActivityRows(List<ReportActivity> activities) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (activities != null) {
            int index = 1;
            for (ReportActivity activity : activities) {
                Map<String, String> row = new HashMap<>();
                String key = formatIndex(index);
                row.put("activity_" + key,          safe(activity.getActivityName()));
                row.put("a" + index + "_advance",   activity.getPorcentajeAvance() + "%");
                row.put("a" + index + "_observations", safe(activity.getObservaciones()));
                rows.add(row);
                index++;
            }
        }
        return rows;
    }

    private static List<Map<String, String>> buildDeliverableRows(List<ReportDeliverable> deliverables) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (deliverables != null) {
            int index = 1;
            for (ReportDeliverable deliverable : deliverables) {
                Map<String, String> row = new HashMap<>();
                String key = formatIndex(index);
                row.put("deliverable_result_" + key,          safe(deliverable.getResultado()));
                row.put("dr" + key + "_advance",              deliverable.getPorcentajeAvance() + "%");
                row.put("dr" + key + "_observations",         safe(deliverable.getObservaciones()));
                rows.add(row);
                index++;
            }
        }
        return rows;
    }

    private static String buildStoragePath(PartialAndFinalReport report, String matricula) {
        String path = "storage/intern_" + matricula
                + "/proyecto_" + report.getIdProyect()
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

    private static String formatIndex(int index) {
        String formatted = String.format("%02d", index);
        return formatted;
    }

    private static String safe(String value) {
        String result = (value != null) ? value : "";
        return result;
    }
}
