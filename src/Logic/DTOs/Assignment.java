package Logic.DTOs;

import java.time.LocalDateTime;

public class Assignment {

    private int           idAsignacion;
    private int           idPracticante;
    private int           idProyecto;
    private int           idSolicitud;
    private LocalDateTime fechaAsignacion;

    public Assignment() {}

    public Assignment(int idAsignacion, int idPracticante, int idProyecto,
                      int idSolicitud, LocalDateTime fechaAsignacion) {
        this.idAsignacion   = idAsignacion;
        this.idPracticante  = idPracticante;
        this.idProyecto     = idProyecto;
        this.idSolicitud    = idSolicitud;
        this.fechaAsignacion = fechaAsignacion;
    }

    public int           getIdAsignacion()                         { return idAsignacion; }
    public void          setIdAsignacion(int idAsignacion)         { this.idAsignacion = idAsignacion; }

    public int           getIdPracticante()                            { return idPracticante; }
    public void          setIdPracticante(int idPracticante)           { this.idPracticante = idPracticante; }

    public int           getIdProyecto()                           { return idProyecto; }
    public void          setIdProyecto(int idProyecto)             { this.idProyecto = idProyecto; }

    public int           getIdSolicitud()                          { return idSolicitud; }
    public void          setIdSolicitud(int idSolicitud)           { this.idSolicitud = idSolicitud; }

    public LocalDateTime getFechaAsignacion()                                  { return fechaAsignacion; }
    public void          setFechaAsignacion(LocalDateTime fechaAsignacion)     { this.fechaAsignacion = fechaAsignacion; }
}