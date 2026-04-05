package Logic.DTOs;

import java.util.Date;

public class ReportEvaluation{
    private int idReportEvaluation;
    private int idReport;
    private int grade;
    private String feedback;
    private Date evaluationDate;


    public ReportEvaluation(int idReportEvaluation, int idReport, int grade, String feedback, Date evaluationDate) {
        this.idReportEvaluation = idReportEvaluation;
        this.idReport = idReport;
        this.grade = grade;
        this.feedback = feedback;
        this.evaluationDate = evaluationDate;
    }

    public ReportEvaluation() {
    }

    public int getIdReportEvaluation() {
        return idReportEvaluation;
    }

    public void setIdReportEvaluation(int idReportEvaluation) {
        this.idReportEvaluation = idReportEvaluation;
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
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

    public Date getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(Date evaluationDate) {
        this.evaluationDate = evaluationDate;
    }
}
