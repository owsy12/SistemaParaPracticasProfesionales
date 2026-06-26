package Logic.DTOs;

import java.util.Objects;
import java.time.LocalDate;
import java.util.Date;

public class Report {
    private static final String STATUS_SUBMITTED = "Entregado";

    private int idReport;
    private int idIntern;
    private int idProject;
    private int idProfessor;
    private String reportType;
    private String period;
    private String documentPath;
    private String signedDocumentPath;
    private String status;
    private int reportedHours;
    private String professorObservations;
    private LocalDate reviewDate;
    private Date submissionDate;
    private LocalDate deadline;
    private boolean lateDelivery;
    private String monthName;
    private Double grade;
    private LocalDate evaluationDate;

    public Report(int idReport, int idIntern, int idProject, int idProfessor, String reportType, String period, String documentPath, String status, int grade, String feedback, Date submissionDate, Date evaluationDate, int reportNumber, int coveredHours, String methodology, String obtainedResults, String observations, String month, int year, String block, String section) {
        this.idReport = idReport;
        this.idIntern = idIntern;
        this.idProject = idProject;
        this.idProfessor = idProfessor;
        this.reportType = reportType;
        this.period = period;
        this.documentPath = documentPath;
        this.status = status;
        this.submissionDate = submissionDate;
    }


    public Report() {
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public int getIdIntern() {
        return idIntern;
    }

    public void setIdIntern(int idIntern) {
        this.idIntern = idIntern;
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public int getIdProfessor() {
        return idProfessor;
    }

    public void setIdProfessor(int idProfessor) {
        this.idProfessor = idProfessor;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getSignedDocumentPath() {
        return signedDocumentPath;
    }

    public void setSignedDocumentPath(String signedDocumentPath) {
        this.signedDocumentPath = signedDocumentPath;
    }

    public int getReportedHours() {
        return reportedHours;
    }

    public void setReportedHours(int reportedHours) {
        this.reportedHours = reportedHours;
    }

    public String getProfessorObservations() {
        return professorObservations;
    }

    public void setProfessorObservations(String professorObservations) {
        this.professorObservations = professorObservations;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public boolean isLateDelivery() {
        return lateDelivery;
    }

    public void setLateDelivery(boolean lateDelivery) {
        this.lateDelivery = lateDelivery;
    }

    public String getDisplayStatus() {
        boolean isLateDelivery = lateDelivery && STATUS_SUBMITTED.equals(status);
        String statusLabel;
        if (isLateDelivery) {
            statusLabel = "Entrega tardía";
        } else {
            statusLabel = status;
        }
        return statusLabel;
    }

    public String getDateDisplay() {
        String dateText = "";
        if (submissionDate != null) {
            dateText = submissionDate.toString();
        }
        return dateText;
    }

    public String getIdReportDisplay() {
        String display = String.valueOf(idReport);
        return display;
    }

    public String getIdInternDisplay() {
        String display = String.valueOf(idIntern);
        return display;
    }

    public String getReportedHoursDisplay() {
        String display = String.valueOf(reportedHours);
        return display;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public LocalDate getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDate evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public String getGradeDisplay() {
        String display = "Sin calificar";
        if (grade != null) {
            display = String.format("%.2f", grade);
        }
        return display;
    }

    public String getEvaluationDateDisplay() {
        String display = "—";
        if (evaluationDate != null) {
            display = evaluationDate.toString();
        }
        return display;
    }

    public String getObservationsDisplay() {
        String display = "Sin observaciones";
        boolean hasObservations = professorObservations != null
                && !professorObservations.isBlank();
        if (hasObservations) {
            display = professorObservations;
        }
        return display;
    }

    public String getTypeWithMonth() {
        String typeDisplay = reportType != null ? reportType : "";
        boolean isMonthly = "Mensual".equals(reportType);
        boolean hasMonthName = monthName != null && !monthName.isBlank();
        boolean shouldAppendMonth = isMonthly && hasMonthName;
        if (shouldAppendMonth) {
            typeDisplay = typeDisplay + " - " + monthName;
        }
        return typeDisplay;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Report other = (Report) object;
        return idReport == other.idReport
                && idIntern == other.idIntern
                && idProject == other.idProject
                && idProfessor == other.idProfessor
                && Objects.equals(reportType, other.reportType)
                && Objects.equals(period, other.period)
                && Objects.equals(documentPath, other.documentPath)
                && Objects.equals(signedDocumentPath, other.signedDocumentPath)
                && Objects.equals(status, other.status)
                && reportedHours == other.reportedHours
                && Objects.equals(professorObservations, other.professorObservations)
                && Objects.equals(reviewDate, other.reviewDate)
                && Objects.equals(submissionDate, other.submissionDate)
                && Objects.equals(deadline, other.deadline)
                && lateDelivery == other.lateDelivery
                && Objects.equals(monthName, other.monthName)
                && Objects.equals(grade, other.grade)
                && Objects.equals(evaluationDate, other.evaluationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReport, idIntern, idProject, idProfessor, reportType, period, documentPath, signedDocumentPath, status, reportedHours, professorObservations, reviewDate, submissionDate, deadline, lateDelivery, monthName, grade, evaluationDate);
    }
}
