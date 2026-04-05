package Logic.DTOs;

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
}
