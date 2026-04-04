package Logic.DTOs;

import java.time.LocalDateTime;

public class Solicitud {

    private int           idSolicitud;
    private int           idPracticante;
    private String        estado;
    private LocalDateTime fechaSolicitud;

    public Solicitud() {}

    public Solicitud(int idSolicitud, int idPracticante,
                     String estado, LocalDateTime fechaSolicitud) {
        this.idSolicitud    = idSolicitud;
        this.idPracticante  = idPracticante;
        this.estado         = estado;
        this.fechaSolicitud = fechaSolicitud;
    }

    public int           getIdSolicitud()                      { return idSolicitud; }
    public void          setIdSolicitud(int idSolicitud)       { this.idSolicitud = idSolicitud; }

    public int           getIdPracticante()                            { return idPracticante; }
    public void          setIdPracticante(int idPracticante)           { this.idPracticante = idPracticante; }

    public String        getEstado()                 { return estado; }
    public void          setEstado(String estado)    { this.estado = estado; }

    public LocalDateTime getFechaSolicitud()                               { return fechaSolicitud; }
    public void          setFechaSolicitud(LocalDateTime fechaSolicitud)   { this.fechaSolicitud = fechaSolicitud; }
}