package GUI.Utils;

import Logic.DTOs.PartialAndFinalReport;
import Logic.DTOs.ReportActivity;
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

public class PartialReportGenerator {

    private static final Logger LOGGER = Logger.getLogger(PartialReportGenerator.class.getName());
    private static final String TEMPLATE = "/GUI/Utils/basedocuments/reporteParcial.docx";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int WEEKS_COUNT = 8;
    private static final int ROWS_PER_ACTIVITY = 2;

    private PartialReportGenerator() {
    }

    public static String generate(PartialAndFinalReport report,
                                   ReportGenerationContext context,
                                   ReportContent content) throws IOException {
        Map<String, String> values = buildValues(report, context);
        List<Map<String, String>> activityRows = buildActivityRows(content.getActivities());
        List<DocxTemplateEngine.RowExpansion> expansions =
                buildExpansions(activityRows, "activity_01", ROWS_PER_ACTIVITY);

        byte[] docxBytes = DocxTemplateEngine.fill(TEMPLATE, values, expansions);
        byte[] pdfBytes = DocxToPdfConverter.convert(docxBytes);

        String storagePath = buildStoragePath(report, context.getMatricula());
        saveFile(pdfBytes, storagePath);

        String fileName = "Reporte_Parcial_" + report.getReportNumber() + ".pdf";
        showSaveDialog(pdfBytes, fileName, context.getOwnerWindow());

        return storagePath;
    }

    private static Map<String, String> buildValues(PartialAndFinalReport report,
                                                    ReportGenerationContext context) {
        Map<String, String> values = new HashMap<>();
        values.put("nrc",                 String.valueOf(report.getIdProyect()));
        values.put("school_term",         safe(report.getPeriod()));
        values.put("intern",              context.getInternFullName());
        values.put("linked_organization", context.getOrganizationName());
        values.put("project",             context.getProjectName());
        values.put("report_term",         safe(report.getPeriod()));
        values.put("hours",               String.valueOf(report.getCoveredHours()));
        values.put("report_date",         LocalDate.now().format(DATE_FORMAT));
        values.put("report_number",       String.valueOf(report.getReportNumber()));
        values.put("Project_objective",   safe(report.getGeneralObjective()));
        values.put("metodology",          safe(report.getMethodology()));
        values.put("result",              safe(report.getObtainedResults()));
        values.put("observations",        safe(report.getObservations()));
        values.put("name",                context.getInternFullName());
        values.put("technician",          context.getTechnicianName());
        values.put("technician_position", context.getTechnicianPosition());
        values.put("profesor",            context.getProfessorName());
        return values;
    }

    private static List<Map<String, String>> buildActivityRows(List<ReportActivity> activities) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (activities != null) {
            int index = 1;
            for (ReportActivity activity : activities) {
                Map<String, String> row = new HashMap<>();
                row.put("activity_" + formatIndex(index), safe(activity.getActivityName()));
                for (int week = 1; week <= WEEKS_COUNT; week++) {
                    row.put("a" + index + "_plan_s" + week, activity.planCell(week));
                    row.put("a" + index + "_real_s" + week, activity.realCell(week));
                }
                rows.add(row);
                index++;
            }
        }
        return rows;
    }

    private static List<DocxTemplateEngine.RowExpansion> buildExpansions(
            List<Map<String, String>> rows, String marker, int rowsPerItem) {
        List<DocxTemplateEngine.RowExpansion> expansions = new ArrayList<>();
        if (!rows.isEmpty()) {
            expansions.add(new DocxTemplateEngine.RowExpansion(marker, rows, rowsPerItem));
        }
        return expansions;
    }

    private static String buildStoragePath(PartialAndFinalReport report, String matricula) {
        String path = "storage/intern_" + matricula
                + "/proyecto_" + report.getIdProyect()
                + "/reports/partial_" + report.getIdReport() + ".pdf";
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
