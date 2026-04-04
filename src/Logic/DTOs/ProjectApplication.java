package Logic.DTOs;

public class ProjectApplication {
    private int idProyectApplication;
    private int idApplication;
    private int idProyect;
    private int preferenceOrder;


    public ProjectApplication(int idProyectApplication, int idApplication, int idProyect, int preferenceOrder) {
        this.idProyectApplication = idProyectApplication;
        this.idApplication = idApplication;
        this.idProyect = idProyect;
        this.preferenceOrder = preferenceOrder;
    }

    public ProjectApplication() {
    }

    public int getIdProyectApplication() {
        return idProyectApplication;
    }

    public void setIdProyectApplication(int idProyectApplication) {
        this.idProyectApplication = idProyectApplication;
    }

    public int getIdApplication() {
        return idApplication;
    }

    public void setIdApplication(int idApplication) {
        this.idApplication = idApplication;
    }

    public int getIdProyect() {
        return idProyect;
    }

    public void setIdProyect(int idProyect) {
        this.idProyect = idProyect;
    }

    public int getPreferenceOrder() {
        return preferenceOrder;
    }

    public void setPreferenceOrder(int preferenceOrder) {
        this.preferenceOrder = preferenceOrder;
    }
}
