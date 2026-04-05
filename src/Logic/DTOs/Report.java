package Logic.DTOs;

import java.util.Date;

public class Report {
    private int idReport;
    private int idIntern;
    private int idProyect;
    private int idProfessor;
    private String reportType;
    private String period;
    private String documentPath;
    private String status;
    private Date sumissionDate;

    public Report(int idReport, int idIntern, int idProyect, int idProfessor, String reportType, String period, String documentPath, String status, int grade, String feedback, Date sumissionDate, Date evaluationDate, int reportNumber, int coveredHours, String methodology, String obtainedResults, String observations, String month, int year, String block, String section) {
        this.idReport = idReport;
        this.idIntern = idIntern;
        this.idProyect = idProyect;
        this.idProfessor = idProfessor;
        this.reportType = reportType;
        this.period = period;
        this.documentPath = documentPath;
        this.status = status;
        this.sumissionDate = sumissionDate;
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

    public int getIdProyect() {
        return idProyect;
    }

    public void setIdProyect(int idProyect) {
        this.idProyect = idProyect;
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

    public Date getSumissionDate() {
        return sumissionDate;
    }

    public void setSumissionDate(Date sumissionDate) {
        this.sumissionDate = sumissionDate;
    }

}
