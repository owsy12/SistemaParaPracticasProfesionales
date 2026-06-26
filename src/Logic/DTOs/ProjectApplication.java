package Logic.DTOs;

import java.util.Objects;

public class ProjectApplication {
    private int idProjectApplication;
    private int idApplication;
    private int idProject;
    private int preferenceOrder;


    public ProjectApplication(int idProjectApplication, int idApplication, int idProject, int preferenceOrder) {
        this.idProjectApplication = idProjectApplication;
        this.idApplication = idApplication;
        this.idProject = idProject;
        this.preferenceOrder = preferenceOrder;
    }

    public ProjectApplication() {
    }

    public int getIdProjectApplication() {
        return idProjectApplication;
    }

    public void setIdProjectApplication(int idProjectApplication) {
        this.idProjectApplication = idProjectApplication;
    }

    public int getIdApplication() {
        return idApplication;
    }

    public void setIdApplication(int idApplication) {
        this.idApplication = idApplication;
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public int getPreferenceOrder() {
        return preferenceOrder;
    }

    public void setPreferenceOrder(int preferenceOrder) {
        this.preferenceOrder = preferenceOrder;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ProjectApplication other = (ProjectApplication) object;
        return idProjectApplication == other.idProjectApplication
                && idApplication == other.idApplication
                && idProject == other.idProject
                && preferenceOrder == other.preferenceOrder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProjectApplication, idApplication, idProject, preferenceOrder);
    }
}
