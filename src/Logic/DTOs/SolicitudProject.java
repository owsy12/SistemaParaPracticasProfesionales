package Logic.DTOs;

public class SolicitudProject {

    private int idSolicitud;
    private int idProject;
    private int preferenceOrder;

    public SolicitudProject() {}

    public SolicitudProject(int idProject, int preferenceOrder) {
        this.idProject = idProject;
        this.preferenceOrder = preferenceOrder;
    }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public int getIdProject() { return idProject; }
    public void setIdProject(int idProject) { this.idProject = idProject; }

    public int getPreferenceOrder() { return preferenceOrder; }
    public void setPreferenceOrder(int preferenceOrder) { this.preferenceOrder = preferenceOrder; }
}