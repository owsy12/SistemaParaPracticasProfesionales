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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ReportStatusUpdate other = (ReportStatusUpdate) object;
        return java.util.Objects.equals(status, other.status)
                && java.util.Objects.equals(professorObservations, other.professorObservations)
                && java.util.Objects.equals(reviewDate, other.reviewDate);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(status, professorObservations, reviewDate);
    }
}
