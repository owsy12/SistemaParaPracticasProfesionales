package GUI.Utils;

import Logic.DTOs.MonthlyReport;
import Logic.DTOs.ReportActivity;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonthlyReportGenerator {

    private static final Logger LOGGER = Logger.getLogger(MonthlyReportGenerator.class.getName());
    private static final String TEMPLATE = "/GUI/Utils/basedocuments/reporteMensual.docx";
    private static final int ROWS_PER_ACTIVITY = 1;

    private MonthlyReportGenerator() {
    }

    public static String generate(MonthlyReport report,
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

        String fileName = "Reporte_Mensual_" + safe(report.getMonth()) + "_" + report.getYear() + ".pdf";
        showSaveDialog(pdfBytes, fileName, context.getOwnerWindow());

        return storagePath;
    }

    private static Map<String, String> buildValues(MonthlyReport report,
                                                    ReportGenerationContext context) {
        Map<String, String> values = new HashMap<>();
        values.put("reportnumber", String.valueOf(report.getIdReport()));
        values.put("month",        safe(report.getMonth()) + " " + report.getYear());
        values.put("report_hours", String.valueOf(report.getMonthlyHours()));
        values.put("total_hours",  String.valueOf(context.getTotalApprovedHours()));
        values.put("intern",       context.getInternFullName());
        values.put("block",        safe(report.getBlock()));
        values.put("section",      safe(report.getSection()));
        values.put("technician",   context.getTechnicianName());
        values.put("profesor",     context.getProfessorName());
        return values;
    }

    private static List<Map<String, String>> buildActivityRows(List<ReportActivity> activities) {
        List<Map<String, String>> rows = new ArrayList<>();
        if (activities != null) {
            int index = 1;
            for (ReportActivity activity : activities) {
                Map<String, String> row = new HashMap<>();
                String key = formatIndex(index);
                row.put("activity_" + key,                    safe(activity.getActivityName()));
                row.put("activity_" + key + "_period",        safe(activity.getPeriodo()));
                row.put("activity_" + key + "_observations",  safe(activity.getObservaciones()));
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

    private static String buildStoragePath(MonthlyReport report, String matricula) {
        String path = "storage/intern_" + matricula
                + "/proyecto_" + report.getIdProyect()
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
