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

    private int grade;
    private String feedback;
    private Date sumissionDate;
    private Date evaluationDate;

    private int reportNumber;
    private int coveredHours;
    private String methodology;
    private String obtainedResults;
    private String observations;

    private String month;
    private int year;
    private String block;
    private String section;


    public Report(int idReport, int idIntern, int idProyect, int idProfessor, String reportType, String period, String documentPath, String status, int grade, String feedback, Date sumissionDate, Date evaluationDate, int reportNumber, int coveredHours, String methodology, String obtainedResults, String observations, String month, int year, String block, String section) {
        this.idReport = idReport;
        this.idIntern = idIntern;
        this.idProyect = idProyect;
        this.idProfessor = idProfessor;
        this.reportType = reportType;
        this.period = period;
        this.documentPath = documentPath;
        this.status = status;
        this.grade = grade;
        this.feedback = feedback;
        this.sumissionDate = sumissionDate;
        this.evaluationDate = evaluationDate;
        this.reportNumber = reportNumber;
        this.coveredHours = coveredHours;
        this.methodology = methodology;
        this.obtainedResults = obtainedResults;
        this.observations = observations;
        this.month = month;
        this.year = year;
        this.block = block;
        this.section = section;
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

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Date getSumissionDate() {
        return sumissionDate;
    }

    public void setSumissionDate(Date sumissionDate) {
        this.sumissionDate = sumissionDate;
    }

    public Date getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(Date evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public int getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(int reportNumber) {
        this.reportNumber = reportNumber;
    }

    public int getCoveredHours() {
        return coveredHours;
    }

    public void setCoveredHours(int coveredHours) {
        this.coveredHours = coveredHours;
    }

    public String getMethodology() {
        return methodology;
    }

    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    public String getObtainedResults() {
        return obtainedResults;
    }

    public void setObtainedResults(String obtainedResults) {
        this.obtainedResults = obtainedResults;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
}
