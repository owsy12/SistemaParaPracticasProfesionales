package Logic.DTOs;

import java.time.LocalDateTime;
import java.util.List;

public class Solicitud {

    private int idSolicitud;
    private int idIntern;
    private String state;
    private LocalDateTime requestDate;
    private List<SolicitudProject> projectOptions;

    public Solicitud() {}

    public Solicitud(int idIntern, List<SolicitudProject> projectOptions) {
        this.idIntern = idIntern;
        this.projectOptions = projectOptions;
        this.state = "Pendiente";
    }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public int getIdIntern() { return idIntern; }
    public void setIdIntern(int idIntern) { this.idIntern = idIntern; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public List<SolicitudProject> getProjectOptions() { return projectOptions; }
    public void setProjectOptions(List<SolicitudProject> projectOptions) { this.projectOptions = projectOptions; }
}