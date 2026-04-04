package Logic.DTOs;

public class SolicitudProject {

    private int idSolicitudProyecto;
    private int idSolicitud;
    private int idProyecto;
    private int ordenPreferencia;

    public SolicitudProject() {}

    public SolicitudProject(int idSolicitudProyecto, int idSolicitud,
                            int idProyecto, int ordenPreferencia) {
        this.idSolicitudProyecto = idSolicitudProyecto;
        this.idSolicitud         = idSolicitud;
        this.idProyecto          = idProyecto;
        this.ordenPreferencia    = ordenPreferencia;
    }

    public int getIdSolicitudProyecto()                              { return idSolicitudProyecto; }
    public void setIdSolicitudProyecto(int idSolicitudProyecto)      { this.idSolicitudProyecto = idSolicitudProyecto; }

    public int getIdSolicitud()                        { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud)        { this.idSolicitud = idSolicitud; }

    public int getIdProyecto()                         { return idProyecto; }
    public void setIdProyecto(int idProyecto)          { this.idProyecto = idProyecto; }

    public int getOrdenPreferencia()                               { return ordenPreferencia; }
    public void setOrdenPreferencia(int ordenPreferencia)          { this.ordenPreferencia = ordenPreferencia; }
}