package Logic.DTOs;

import java.util.Objects;

public class ReportDeliverable {
    private int idReportDeliverable;
    private int idReport;
    private String result;
    private String description;
    private int advancePercentage;
    private String observations;

    public ReportDeliverable() {
    }

    public int getIdReportDeliverable() {
        return idReportDeliverable;
    }

    public void setIdReportDeliverable(int idReportDeliverable) {
        this.idReportDeliverable = idReportDeliverable;
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAdvancePercentage() {
        return advancePercentage;
    }

    public void setAdvancePercentage(int advancePercentage) {
        this.advancePercentage = advancePercentage;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getAdvanceDisplay() {
        String display = advancePercentage + "%";
        return display;
    }

    public String getDescriptionDisplay() {
        String display = "";
        if (description != null) {
            display = description;
        }
        return display;
    }

    public String getObservationsDisplay() {
        String display = "";
        if (observations != null) {
            display = observations;
        }
        return display;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ReportDeliverable other = (ReportDeliverable) object;
        return idReportDeliverable == other.idReportDeliverable
                && idReport == other.idReport
                && Objects.equals(result, other.result)
                && Objects.equals(description, other.description)
                && advancePercentage == other.advancePercentage
                && Objects.equals(observations, other.observations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReportDeliverable, idReport, result, description, advancePercentage, observations);
    }
}
