package Logic.DTOs;

import java.time.LocalDate;

public class Activity {
    private int idActivity;
    private int idProject;
    private String name;
    private String description;
    private LocalDate creationDate;
    private String status;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;

    public Activity(int idActivity, int idProject, String name, String description,
                    LocalDate creationDate, String status) {
        this.idActivity = idActivity;
        this.idProject = idProject;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.status = status;
    }

    public Activity() {
    }

    public int getIdActivity() {
        return idActivity;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getFechaInicioDisplay() {
        String display = "";
        if (startDate != null) {
            display = startDate.toString();
        }
        return display;
    }

    public String getFechaFinDisplay() {
        String display = "";
        if (endDate != null) {
            display = endDate.toString();
        }
        return display;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Activity other = (Activity) object;
        return idActivity == other.idActivity
                && idProject == other.idProject
                && java.util.Objects.equals(name, other.name)
                && java.util.Objects.equals(description, other.description)
                && java.util.Objects.equals(creationDate, other.creationDate)
                && java.util.Objects.equals(status, other.status)
                && java.util.Objects.equals(projectName, other.projectName)
                && java.util.Objects.equals(startDate, other.startDate)
                && java.util.Objects.equals(endDate, other.endDate);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idActivity, idProject, name, description, creationDate, status, projectName, startDate, endDate);
    }
}
