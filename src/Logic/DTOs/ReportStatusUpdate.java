package Logic.DTOs;

import java.sql.Date;

public class ReportStatusUpdate {

    private final String status;
    private final String professorObservations;
    private final Date reviewDate;

    public ReportStatusUpdate(String status, String professorObservations, Date reviewDate) {
        this.status = status;
        this.professorObservations = professorObservations;
        this.reviewDate = reviewDate;
    }

    public String getStatus() {
        return status;
    }

    public String getProfessorObservations() {
        return professorObservations;
    }

    public Date getReviewDate() {
        return reviewDate;
    }
}
