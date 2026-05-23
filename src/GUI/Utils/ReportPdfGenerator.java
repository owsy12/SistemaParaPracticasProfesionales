package GUI.Utils;

import Logic.DTOs.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportPdfGenerator {

    private static final Logger LOGGER = Logger.getLogger(ReportPdfGenerator.class.getName());

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String buildReportStoragePath(String matricula, int idProject,
                                                String reportType, int idReport) {
        return "storage/intern_" + matricula
                + "/proyecto_" + idProject
                + "/reportes_generados/reporte_"
                + reportType.toLowerCase() + "_" + idReport;
    }

    public static String generateMonthlyReport(MonthlyReport report,
                                               String internFullName,
                                               String professorName,
                                               String organizationName,
                                               String projectName,
                                               String technicianName,
                                               int totalApprovedHours,
                                               List<ReportActivity> reportActivities)
            throws IOException {

        SimplePdfWriter pdf = new SimplePdfWriter();

        pdf.title("REPORTE MENSUAL DE PRÁCTICAS PROFESIONALES Y SERVICIO SOCIAL");
        pdf.spacer();

        pdf.field("No. Reporte", String.valueOf(report.getIdReport()));
        pdf.field("Mes",         report.getMonth() + " " + report.getYear());
        pdf.field("Horas del período",   String.valueOf(report.getMonthlyHours()));
        pdf.field("Horas Acumuladas",    String.valueOf(totalApprovedHours));
        pdf.spacer();

        pdf.field("Nombre del alumno",       internFullName);
        pdf.field("Bloque",                  report.getBlock()   != null ? report.getBlock()   : "—");
        pdf.field("Sección",                 report.getSection() != null ? report.getSection() : "—");
        pdf.spacer();

        pdf.field("Nombre del responsable directo del servicio social", technicianName);
        pdf.field("Académico de la experiencia educativa",              professorName);
        pdf.spacer();

        pdf.section("ACTIVIDADES REALIZADAS EN EL PERÍODO");
        String[] headers = {"Período", "Actividad", "Observaciones"};
        String[][] rows;

        if (reportActivities != null && !reportActivities.isEmpty()) {
            rows = new String[reportActivities.size()][3];
            for (int i = 0; i < reportActivities.size(); i++) {
                ReportActivity ra = reportActivities.get(i);
                rows[i][0] = ra.getPeriodo()        != null ? ra.getPeriodo()        : "";
                rows[i][1] = ra.getActivityName()   != null ? ra.getActivityName()   : "";
                rows[i][2] = ra.getObservaciones()  != null ? ra.getObservaciones()  : "";
            }
        } else {
            rows = new String[][]{{"", "Sin actividades registradas", ""}};
        }
        pdf.table(headers, rows);
        pdf.spacer();

        pdf.section("FIRMAS");
        pdf.field("Alumno",                       internFullName);
        pdf.field("Docente",                       professorName);
        pdf.field("Responsable Técnico",           technicianName);
        pdf.field("Sello de la organización",      organizationName);

        return saveToFile(pdf.toBytes(), report.getDocumentPath());
    }

    public static String generatePartialReport(PartialAndFinalReport report,
                                               String internFullName,
                                               String professorName,
                                               String organizationName,
                                               String projectName,
                                               String projectObjective,
                                               String technicianName,
                                               String technicianPosition,
                                               List<ReportActivity> reportActivities)
            throws IOException {

        SimplePdfWriter pdf = new SimplePdfWriter();

        pdf.title("REPORTE PARCIAL DE PRÁCTICAS PROFESIONALES Y SERVICIO SOCIAL");
        pdf.spacer();

        pdf.section("DATOS GENERALES DE LA EE");
        pdf.field("Carrera",          "Lic. Ingeniería de Software");
        pdf.field("NRC",              String.valueOf(report.getIdProyect()));
        pdf.field("Profesor",         professorName);
        pdf.field("Período escolar",  report.getPeriod());
        pdf.spacer();

        pdf.section("DATOS DEL PROYECTO");
        pdf.field("Alumno(s)",                internFullName);
        pdf.field("Organización vinculada",   organizationName);
        pdf.field("Proyecto",                 projectName);
        pdf.field("Período del reporte",      report.getPeriod());
        pdf.field("Horas cubiertas",          String.valueOf(report.getCoveredHours()));
        pdf.field("Fecha del reporte",        LocalDate.now().format(DATE_FMT));
        pdf.field("Número del informe",       String.valueOf(report.getReportNumber()));
        pdf.spacer();

        pdf.section("OBJETIVO(S) GENERAL DEL PROYECTO");
        pdf.paragraph(projectObjective != null && !projectObjective.isBlank()
                ? projectObjective : "—");
        pdf.spacer();

        pdf.section("METODOLOGÍA");
        pdf.paragraph(report.getMethodology() != null ? report.getMethodology() : "—");
        pdf.spacer();

        pdf.section("AVANCE DE ACTIVIDADES REALIZADAS EN RELACIÓN AL PLAN DE TRABAJO");
        pdf.paragraph("NOTA: Los tiempos están expresados en semanas.");
        pdf.spacer();

        buildParcialActivityTable(pdf, reportActivities);
        pdf.spacer();

        pdf.section("RESULTADOS OBTENIDOS AL MOMENTO");
        pdf.paragraph(report.getObtainedResults() != null ? report.getObtainedResults() : "—");
        pdf.spacer();

        pdf.section("OBSERVACIONES");
        pdf.paragraph(report.getObservations() != null ? report.getObservations() : "—");
        pdf.spacer();

        pdf.section("FIRMAS");
        pdf.field("Nombre(s) y Firma(s) del Estudiante", internFullName);
        pdf.field("Docente",                             professorName);
        pdf.field("Vo. Bo.",                             "");
        pdf.field("Responsable Técnico",                 technicianName
                + (technicianPosition != null && !technicianPosition.isBlank()
                   ? ", " + technicianPosition : ""));
        pdf.field("Sello de la organización",            organizationName);

        return saveToFile(pdf.toBytes(), report.getDocumentPath());
    }

    public static String generateFinalReport(PartialAndFinalReport report,
                                             String internFullName,
                                             String professorName,
                                             String organizationName,
                                             String projectName,
                                             String projectObjective,
                                             String technicianName,
                                             String technicianPosition,
                                             List<ReportActivity>   reportActivities,
                                             List<ReportDeliverable> reportDeliverables)
            throws IOException {

        SimplePdfWriter pdf = new SimplePdfWriter();

        pdf.title("REPORTE FINAL DE PRÁCTICAS PROFESIONALES Y SERVICIO SOCIAL");
        pdf.spacer();

        pdf.section("DATOS GENERALES DE LA EE");
        pdf.field("Carrera",          "Lic. Ingeniería de Software");
        pdf.field("NRC",              String.valueOf(report.getIdProyect()));
        pdf.field("Profesor",         professorName);
        pdf.field("Período escolar",  report.getPeriod());
        pdf.spacer();

        pdf.section("DATOS DEL PROYECTO");
        pdf.field("Alumno(s)",                internFullName);
        pdf.field("Organización vinculada",   organizationName);
        pdf.field("Proyecto",                 projectName);
        pdf.field("Total de horas cubiertas", String.valueOf(report.getCoveredHours()));
        pdf.field("Fecha del reporte",        LocalDate.now().format(DATE_FMT));
        pdf.field("Tipo de reporte",          "FINAL");
        pdf.spacer();

        pdf.section("OBJETIVO(S) GENERAL DEL PROYECTO");
        pdf.paragraph(projectObjective != null && !projectObjective.isBlank()
                ? projectObjective : "—");
        pdf.spacer();

        pdf.section("METODOLOGÍA APLICADA");
        pdf.paragraph(report.getMethodology() != null ? report.getMethodology() : "—");
        pdf.spacer();

        pdf.section("AVANCE DE ACTIVIDADES REALIZADAS EN RELACIÓN AL PLAN DE TRABAJO");
        String[] actHeaders = {"Actividad programada", "% Avance", "Observaciones"};
        String[][] actRows;

        if (reportActivities != null && !reportActivities.isEmpty()) {
            actRows = new String[reportActivities.size()][3];
            for (int i = 0; i < reportActivities.size(); i++) {
                ReportActivity ra = reportActivities.get(i);
                actRows[i][0] = ra.getActivityName()  != null ? ra.getActivityName()  : "";
                actRows[i][1] = ra.getPorcentajeAvance() + "%";
                actRows[i][2] = ra.getObservaciones() != null ? ra.getObservaciones() : "";
            }
        } else {
            actRows = new String[][]{{"Sin actividades registradas", "—", "—"}};
        }
        pdf.table(actHeaders, actRows);
        pdf.paragraph("NOTA: En caso de que alguna actividad no hubiera sido cubierta " +
                "con un 100% de avance, indicar las razones en la columna de observaciones.");
        pdf.spacer();

        pdf.section("RESULTADOS EN TÉRMINO DE PRODUCTOS COMPROMETIDOS");
        String[] delHeaders = {"Resultado entregable", "% Avance", "Observaciones"};
        String[][] delRows;

        if (reportDeliverables != null && !reportDeliverables.isEmpty()) {
            delRows = new String[reportDeliverables.size()][3];
            for (int i = 0; i < reportDeliverables.size(); i++) {
                ReportDeliverable rd = reportDeliverables.get(i);
                delRows[i][0] = rd.getResultado()       != null ? rd.getResultado()       : "";
                delRows[i][1] = rd.getPorcentajeAvance() + "%";
                delRows[i][2] = rd.getObservaciones()   != null ? rd.getObservaciones()   : "";
            }
        } else {
            delRows = new String[][]{{"Sin entregables registrados", "—", "—"}};
        }
        pdf.table(delHeaders, delRows);
        pdf.spacer();

        pdf.section("OBSERVACIONES");
        pdf.paragraph(report.getObservations() != null ? report.getObservations() : "—");
        pdf.spacer();

        pdf.section("FIRMAS");
        pdf.field("Nombre(s) y Firma(s) del Estudiante", internFullName);
        pdf.field("Responsable Técnico",technicianName);
        pdf.field("Puesto", technicianPosition != null ? technicianPosition : "—");
        pdf.field("Sello de la organización", organizationName);

        return saveToFile(pdf.toBytes(), report.getDocumentPath());
    }

    public static String generateSelfEvaluation(String internFullName,
                                                String matricula,
                                                String organizationName,
                                                String department,
                                                String technicianName,
                                                String projectName,
                                                int[] answers,  // answers[0..9] = selected value 1-5 per question
                                                String place,
                                                String outputPath)
            throws IOException {

        SimplePdfWriter pdf = new SimplePdfWriter();

        pdf.title("AUTOEVALUACIÓN DEL ALUMNO");
        pdf.spacer();

        pdf.field("Nombre del alumno",           internFullName);
        pdf.field("Matrícula",                   matricula);
        pdf.field("Organización vinculada",      organizationName);
        pdf.field("Departamento",                department);
        pdf.field("Responsable del proyecto",    technicianName);
        pdf.field("Nombre del proyecto",         projectName);
        pdf.spacer();

        pdf.section("INSTRUCCIONES");
        pdf.paragraph(
                "Responde a cada una de las afirmaciones presentadas, marcando con una \"X\" " +
                "la casilla correspondiente de acuerdo a los siguientes criterios:");
        pdf.spacer();

        String[] criteriaHeaders = {"Criterio", "1", "2", "3", "4", "5"};
        String[][] criteriaRows = {{
                "Totalmente en desacuerdo | En desacuerdo | Indeciso | De acuerdo | Totalmente de acuerdo",
                "1", "2", "3", "4", "5"
        }};
        pdf.table(criteriaHeaders, criteriaRows);
        pdf.spacer();

        pdf.section("AFIRMACIONES");

        String[] questions = {
            "1. Mi participación en la Organización Vinculada fue productiva.",
            "2. Logré la aplicación de los conocimientos teórico-prácticos adquiridos en la Licenciatura en Ingeniería de Software.",
            "3. Me sentí seguro al realizar las actividades encomendadas.",
            "4. Las actividades encomendadas despertaron mi interés.",
            "5. La Organización Vinculada me proporcionó la información y facilidades adecuados durante el desarrollo de las prácticas.",
            "6. La Organización Vinculada me dio a conocer las reglas internas que debía seguir al conducirme durante el desarrollo de las prácticas.",
            "7. El Responsable del Proyecto me orientó correctamente para el desarrollo de mis actividades.",
            "8. El Responsable del Proyecto realizó un seguimiento efectivo de mis actividades.",
            "9. El proyecto es congruente con la formación de mi carrera.",
            "10. Considero que las prácticas son importantes para mi formación profesional."
        };

        String[] qHeaders = {"Afirmación", "1", "2", "3", "4", "5"};
        String[][] qRows = new String[questions.length][6];
        int totalScore = 0;

        for (int i = 0; i < questions.length; i++) {
            int selected = (answers != null && i < answers.length) ? answers[i] : 0;
            if (selected >= 1 && selected <= 5) totalScore += selected;
            qRows[i][0] = questions[i];
            for (int col = 1; col <= 5; col++) {
                qRows[i][col] = (col == selected) ? "X" : "";
            }
        }

        pdf.table(qHeaders, qRows);
        pdf.spacer();

        pdf.field("PUNTUACIÓN FINAL", String.valueOf(totalScore));
        pdf.spacer();

        pdf.field("Lugar y fecha", place + " / " + LocalDate.now().format(DATE_FMT));
        pdf.spacer();

        pdf.field("Nombre y Firma del Alumno", internFullName);

        return saveToFile(pdf.toBytes(), outputPath);
    }

    private static void buildParcialActivityTable(SimplePdfWriter pdf,
                                                   List<ReportActivity> activities) {
        String[] headers = {"Actividades", "Tiempo", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"};

        String[][] rows;

        if (activities == null || activities.isEmpty()) {
            rows = new String[][]{
                    {"Sin actividades", "Plan", "", "", "", "", "", "", "", ""},
                    {"",               "Real", "", "", "", "", "", "", "", ""}
            };
        } else {
            rows = new String[activities.size() * 2][10];
            for (int i = 0; i < activities.size(); i++) {
                ReportActivity ra = activities.get(i);
                String name = ra.getActivityName() != null ? ra.getActivityName() : "";

                rows[i * 2][0] = name;
                rows[i * 2][1] = "Plan";
                for (int s = 1; s <= 8; s++) {
                    rows[i * 2][s + 1] = ra.planCell(s);
                }

                rows[i * 2 + 1][0] = "";
                rows[i * 2 + 1][1] = "Real";
                for (int s = 1; s <= 8; s++) {
                    rows[i * 2 + 1][s + 1] = ra.realCell(s);
                }
            }
        }

        pdf.table(headers, rows);
    }

    private static String saveToFile(byte[] pdfBytes, String relativePath) throws IOException {
        Path path = Paths.get(relativePath + ".pdf");
        Files.createDirectories(path.getParent());
        Files.write(path, pdfBytes);
        LOGGER.log(Level.INFO, "PDF generado en: {0}", path.toAbsolutePath());
        return path.toString();
    }
}
