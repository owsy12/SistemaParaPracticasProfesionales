package GUI.DocumentGeneration;

import javafx.stage.Window;

public class ReportGenerationContext {

    private final String internFullName;
    private final String registrationNumber;
    private final String nrc;
    private final String organizationName;
    private final String organizationDepartment;
    private final String professorName;
    private final String technicianName;
    private final String technicianPosition;
    private final String projectName;
    private final int totalApprovedHours;
    private final Window ownerWindow;

    ReportGenerationContext(ReportGenerationContextBuilder builder) {
        this.internFullName = builder.getInternFullName();
        this.registrationNumber = builder.getRegistrationNumber();
        this.nrc = builder.getNrc();
        this.organizationName = builder.getOrganizationName();
        this.organizationDepartment = builder.getOrganizationDepartment();
        this.professorName = builder.getProfessorName();
        this.technicianName = builder.getTechnicianName();
        this.technicianPosition = builder.getTechnicianPosition();
        this.projectName = builder.getProjectName();
        this.totalApprovedHours = builder.getTotalApprovedHours();
        this.ownerWindow = builder.getOwnerWindow();
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
}
