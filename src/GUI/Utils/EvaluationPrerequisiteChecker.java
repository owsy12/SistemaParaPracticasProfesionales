package GUI.Utils;

import Logic.DAO.InitialFormatDAO;
import Logic.DAO.InternActivityDAO;
import Logic.DAO.OvEvaluationDAO;
import Logic.DAO.ReportDAO;
import Logic.DAO.SelfEvaluationDAO;
import Logic.DTOs.InitialFormat;
import Logic.DTOs.InternActivity;
import Logic.DTOs.OvEvaluation;
import Logic.DTOs.Project;
import Logic.DTOs.Report;
import Logic.DTOs.SelfEvaluation;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.time.LocalDate;
import java.util.List;

public class EvaluationPrerequisiteChecker {
    private static final String STATUS_COMPLETED = "Completada";
    private static final String STATUS_EVALUATED = "Evaluado";
    private static final String STATUS_DOCUMENT_EVALUATED = "Evaluada";


    private static final int REQUIRED_HOURS = 420;
    private static final String REPORT_TYPE_MONTHLY = "Mensual";
    private static final String REPORT_TYPE_PARTIAL = "Parcial";
    private static final String REPORT_TYPE_FINAL = "Final";

    private EvaluationPrerequisiteChecker() {
    }

    public static boolean isPracticeComplete(int internId, int projectId)
            throws ServiceException, ValidationException {
        boolean complete = false;
        if (hasEvaluatedReportOfType(internId, REPORT_TYPE_MONTHLY)) {
            if (hasEvaluatedReportOfType(internId, REPORT_TYPE_PARTIAL)) {
                if (hasEvaluatedReportOfType(internId, REPORT_TYPE_FINAL)) {
                    if (isSelfEvaluationEvaluated(internId)) {
                        if (isOvEvaluationEvaluated(internId, projectId)) {
                            complete = true;
                        }
                    }
                }
            }
        }
        return complete;
    }

    private static boolean hasEvaluatedReportOfType(int internId, String reportType)
            throws ServiceException, ValidationException {
        ReportDAO reportDAO = new ReportDAO();
        List<Report> reports = reportDAO.getByIdIntern(internId);
        boolean found = false;
        for (Report report : reports) {
            boolean matches = false;
            if (reportType.equals(report.getReportType())) {
                if (STATUS_EVALUATED.equals(report.getStatus())) {
                    matches = true;
                }
            }
            if (matches) {
                found = true;
            }
        }
        return found;
    }

    private static boolean isSelfEvaluationEvaluated(int internId)
            throws ServiceException, ValidationException {
        SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();
        SelfEvaluation selfEvaluation = selfEvaluationDAO.findByIdIntern(internId);
        boolean evaluated = false;
        if (selfEvaluation != null) {
            if (STATUS_DOCUMENT_EVALUATED.equals(selfEvaluation.getStatus())) {
                evaluated = true;
            }
        }
        return evaluated;
    }

    private static boolean isOvEvaluationEvaluated(int internId, int projectId)
            throws ServiceException, ValidationException {
        OvEvaluationDAO ovEvaluationDAO = new OvEvaluationDAO();
        OvEvaluation ovEvaluation = ovEvaluationDAO.findByInternAndProject(internId, projectId);
        boolean evaluated = false;
        if (ovEvaluation != null) {
            if (STATUS_DOCUMENT_EVALUATED.equals(ovEvaluation.getStatus())) {
                evaluated = true;
            }
        }
        return evaluated;
    }

    public static String check(int internId, Project project)
            throws ServiceException, ValidationException {
        int projectId = project.getIdProject();
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
        boolean isBeforeStart = false;
        if (projectStart != null) {
            if (today.isBefore(projectStart)) {
                isBeforeStart = true;
            }
        }
        boolean isAfterEnd = false;
        if (projectEnd != null) {
            if (today.isAfter(projectEnd)) {
                isAfterEnd = true;
            }
        }
        String message = null;
        if (isBeforeStart || isAfterEnd) {
            message = "El período del proyecto ha finalizado o aún no ha comenzado.";
        }
        return message;
    }
}
