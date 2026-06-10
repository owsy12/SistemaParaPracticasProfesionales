package Logic.DTOs;

public class InternReportFeedback {
    private int idReport;
    private String type;
    private String period;
    private String status;
    private String grade;
    private String evaluationDate;
    private String observations;
    private String documentPath;
    private String signedDocumentPath;

    public InternReportFeedback() {
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(String evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public String getSignedDocumentPath() {
        return signedDocumentPath;
    }

    public void setSignedDocumentPath(String signedDocumentPath) {
        this.signedDocumentPath = signedDocumentPath;
    }
}