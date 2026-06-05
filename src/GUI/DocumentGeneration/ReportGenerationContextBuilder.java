package GUI.DocumentGeneration;

import javafx.stage.Window;

public class ReportGenerationContextBuilder {

    private String internFullName = "";
    private String registrationNumber = "";
    private String nrc = "";
    private String organizationName = "";
    private String organizationDepartment = "";
    private String professorName = "";
    private String technicianName = "";
    private String technicianPosition = "";
    private String projectName = "";
    private int totalApprovedHours = 0;
    private Window ownerWindow = null;

    public ReportGenerationContextBuilder internFullName(String value) {
        internFullName = value;
        return this;
    }

    public ReportGenerationContextBuilder registrationNumber(String value) {
        registrationNumber = value;
        return this;
    }

    public ReportGenerationContextBuilder nrc(String value) {
        nrc = value;
        return this;
    }

    public ReportGenerationContextBuilder organizationName(String value) {
        organizationName = value;
        return this;
    }

    public ReportGenerationContextBuilder organizationDepartment(String value) {
        organizationDepartment = value;
        return this;
    }

    public ReportGenerationContextBuilder professorName(String value) {
        professorName = value;
        return this;
    }

    public ReportGenerationContextBuilder technicianName(String value) {
        technicianName = value;
        return this;
    }

    public ReportGenerationContextBuilder technicianPosition(String value) {
        technicianPosition = value;
        return this;
    }

    public ReportGenerationContextBuilder projectName(String value) {
        projectName = value;
        return this;
    }

    public ReportGenerationContextBuilder totalApprovedHours(int value) {
        totalApprovedHours = value;
        return this;
    }

    public ReportGenerationContextBuilder ownerWindow(Window value) {
        ownerWindow = value;
        return this;
    }

    public String getInternFullName() { return internFullName; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getNrc() { return nrc; }
    public String getOrganizationName() { return organizationName; }
    public String getOrganizationDepartment() { return organizationDepartment; }
    public String getProfessorName() { return professorName; }
    public String getTechnicianName() { return technicianName; }
    public String getTechnicianPosition() { return technicianPosition; }
    public String getProjectName() { return projectName; }
    public int getTotalApprovedHours() { return totalApprovedHours; }
    public Window getOwnerWindow() { return ownerWindow; }

    public ReportGenerationContext build() {
        ReportGenerationContext context = new ReportGenerationContext(this);
        return context;
    }
}
