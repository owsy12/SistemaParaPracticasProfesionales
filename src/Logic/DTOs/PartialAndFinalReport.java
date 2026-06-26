package Logic.DTOs;

import java.util.Objects;

public class PartialAndFinalReport extends Report{
    private int idPartialAndFinalReport;
    private int reportNumber;
    private int coveredHours;
    private String methodology;
    private String obtainedResults;
    private String observations;
    private String generalObjective;

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

    public String getGeneralObjective() {
        return generalObjective;
    }

    public void setGeneralObjective(String generalObjective) {
        this.generalObjective = generalObjective;
    }

    public int getIdPartialAndFinalReport() {
        return idPartialAndFinalReport;
    }

    public void setIdPartialAndFinalReport(int idPartialAndFinalReport) {
        this.idPartialAndFinalReport = idPartialAndFinalReport;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        PartialAndFinalReport other = (PartialAndFinalReport) object;
        return idPartialAndFinalReport == other.idPartialAndFinalReport
                && reportNumber == other.reportNumber
                && coveredHours == other.coveredHours
                && Objects.equals(methodology, other.methodology)
                && Objects.equals(obtainedResults, other.obtainedResults)
                && Objects.equals(observations, other.observations)
                && Objects.equals(generalObjective, other.generalObjective);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idPartialAndFinalReport, reportNumber, coveredHours, methodology, obtainedResults, observations, generalObjective);
    }
}
