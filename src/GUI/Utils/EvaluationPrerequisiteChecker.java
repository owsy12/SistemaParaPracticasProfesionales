package GUI.Utils;

import Logic.DAO.InitialFormatDAO;
import Logic.DAO.InternActivityDAO;
import Logic.DAO.ReportDAO;
import Logic.DTOs.InitialFormat;
import Logic.DTOs.InternActivity;
import Logic.DTOs.Project;
import Logic.DTOs.Report;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.time.LocalDate;
import java.util.List;

public class EvaluationPrerequisiteChecker {
    private static final String STATUS_COMPLETED = "Completada";
    private static final String STATUS_EVALUATED = "Evaluado";


    private static final int REQUIRED_HOURS = 420;

    private EvaluationPrerequisiteChecker() {
    }

    public static String check(int internId, Project project)
            throws ServiceException, ValidationException {
        int projectId = project.getIdProyect();
        LocalDate projectStart = project.getStartDate();
        LocalDate projectEnd = project.getEndDate();

        String result = checkPendingDocuments(internId);
        if (result == null) {
            result = checkApprovedHours(internId);
        }
        if (result == null) {
            result = checkActivitiesCompleted(internId, projectId);
        }
        if (result == null) {
            result = checkReportsEvaluated(internId);
        }
        if (result == null) {
            result = checkProjectPeriod(projectStart, projectEnd);
        }
        return result;
    }

    private static String checkPendingDocuments(int internId)
            throws ServiceException, ValidationException {
        InitialFormatDAO initialFormatDAO = new InitialFormatDAO();
        List<InitialFormat> pendingDocs = initialFormatDAO.findPendingByIntern(internId);
        String message = null;
        if (!pendingDocs.isEmpty()) {
            int count = pendingDocs.size();
            message = "Faltan " + count + " documento(s) inicial(es) por entregar.";
        }
        return message;
    }

    private static String checkApprovedHours(int internId)
            throws ServiceException, ValidationException {
        ReportDAO reportDAO = new ReportDAO();
        int approvedHours = reportDAO.getTotalApprovedHoursByIntern(internId);
        String message = null;
        if (approvedHours < REQUIRED_HOURS) {
            message = "Horas insuficientes: tiene " + approvedHours
                    + " de " + REQUIRED_HOURS + " horas válidas requeridas.";
        }
        return message;
    }

    private static String checkActivitiesCompleted(int internId, int projectId)
            throws ServiceException, ValidationException {
        InternActivityDAO internActivityDAO = new InternActivityDAO();
        List<InternActivity> activities = internActivityDAO.findByInternAndProject(internId, projectId);
        String message = null;
        for (InternActivity activity : activities) {
            boolean isNotCompleted = !STATUS_COMPLETED.equals(activity.getStatus());
            if (isNotCompleted && message == null) {
                message = "Existen actividades pendientes por completar.";
            }
        }
        return message;
    }

    private static String checkReportsEvaluated(int internId)
            throws ServiceException, ValidationException {
        ReportDAO reportDAO = new ReportDAO();
        List<Report> reports = reportDAO.getByIdIntern(internId);
        String message = null;
        for (Report report : reports) {
            boolean isNotEvaluated = !STATUS_EVALUATED.equals(report.getStatus());
            if (isNotEvaluated && message == null) {
                message = "El reporte \"" + report.getReportType() + " - " + report.getPeriod()
                        + "\" no está evaluado (estado: " + report.getStatus() + ").";
            }
        }
        return message;
    }

    private static String checkProjectPeriod(LocalDate projectStart, LocalDate projectEnd) {
        LocalDate today = LocalDate.now();
        boolean isBeforeStart = projectStart != null && today.isBefore(projectStart);
        boolean isAfterEnd = projectEnd != null && today.isAfter(projectEnd);
        String message = null;
        if (isBeforeStart || isAfterEnd) {
            message = "El período del proyecto ha finalizado o aún no ha comenzado.";
        }
        return message;
    }
}
