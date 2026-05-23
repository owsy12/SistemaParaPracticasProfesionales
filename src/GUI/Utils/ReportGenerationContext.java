package GUI.Utils;

import javafx.stage.Window;

public class ReportGenerationContext {

    private final String internFullName;
    private final String matricula;
    private final String organizationName;
    private final String organizationDepartment;
    private final String professorName;
    private final String technicianName;
    private final String technicianPosition;
    private final String projectName;
    private final int totalApprovedHours;
    private final Window ownerWindow;

    private ReportGenerationContext(Builder builder) {
        this.internFullName = builder.internFullName;
        this.matricula = builder.matricula;
        this.organizationName = builder.organizationName;
        this.organizationDepartment = builder.organizationDepartment;
        this.professorName = builder.professorName;
        this.technicianName = builder.technicianName;
        this.technicianPosition = builder.technicianPosition;
        this.projectName = builder.projectName;
        this.totalApprovedHours = builder.totalApprovedHours;
        this.ownerWindow = builder.ownerWindow;
    }

    public String getInternFullName() { return internFullName; }
    public String getMatricula() { return matricula; }
    public String getOrganizationName() { return organizationName; }
    public String getOrganizationDepartment() { return organizationDepartment; }
    public String getProfessorName() { return professorName; }
    public String getTechnicianName() { return technicianName; }
    public String getTechnicianPosition() { return technicianPosition; }
    public String getProjectName() { return projectName; }
    public int getTotalApprovedHours() { return totalApprovedHours; }
    public Window getOwnerWindow() { return ownerWindow; }

    public static class Builder {

        private String internFullName = "";
        private String matricula = "";
        private String organizationName = "";
        private String organizationDepartment = "";
        private String professorName = "";
        private String technicianName = "";
        private String technicianPosition = "";
        private String projectName = "";
        private int totalApprovedHours = 0;
        private Window ownerWindow = null;

        public Builder internFullName(String value) { internFullName = value; return this; }
        public Builder matricula(String value) { matricula = value; return this; }
        public Builder organizationName(String value) { organizationName = value; return this; }
        public Builder organizationDepartment(String value) { organizationDepartment = value; return this; }
        public Builder professorName(String value) { professorName = value; return this; }
        public Builder technicianName(String value) { technicianName = value; return this; }
        public Builder technicianPosition(String value) { technicianPosition = value; return this; }
        public Builder projectName(String value) { projectName = value; return this; }
        public Builder totalApprovedHours(int value) { totalApprovedHours = value; return this; }
        public Builder ownerWindow(Window value) { ownerWindow = value; return this; }

        public ReportGenerationContext build() {
            return new ReportGenerationContext(this);
        }
    }
}
