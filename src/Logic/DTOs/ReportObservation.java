package Logic.DTOs;

import java.util.Objects;
import java.time.LocalDateTime;

public class ReportObservation {
    private int idObservation;
    private int idReport;
    private int idProfessor;
    private String comment;
    private LocalDateTime observationDate;

    public ReportObservation() {
    }

    public int getIdObservation() {
        return idObservation;
    }

    public void setIdObservation(int idObservation) {
        this.idObservation = idObservation;
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public int getIdProfessor() {
        return idProfessor;
    }

    public void setIdProfessor(int idProfessor) {
        this.idProfessor = idProfessor;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(LocalDateTime observationDate) {
        this.observationDate = observationDate;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ReportObservation other = (ReportObservation) object;
        return idObservation == other.idObservation
                && idReport == other.idReport
                && idProfessor == other.idProfessor
                && Objects.equals(comment, other.comment)
                && Objects.equals(observationDate, other.observationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idObservation, idReport, idProfessor, comment, observationDate);
    }
}
