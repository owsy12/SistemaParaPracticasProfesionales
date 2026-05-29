package Logic.DTOs;

import java.time.LocalDate;

public class Prorroga {

    private int idProrroga;
    private int idActividad;
    private LocalDate fechaFinOriginal;
    private LocalDate fechaFinNueva;
    private String motivo;

    public Prorroga() {
    }

    public int getIdProrroga() {
        return idProrroga;
    }

    public void setIdProrroga(int idProrroga) {
        this.idProrroga = idProrroga;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }

    public LocalDate getFechaFinOriginal() {
        return fechaFinOriginal;
    }

    public void setFechaFinOriginal(LocalDate fechaFinOriginal) {
        this.fechaFinOriginal = fechaFinOriginal;
    }

    public LocalDate getFechaFinNueva() {
        return fechaFinNueva;
    }

    public void setFechaFinNueva(LocalDate fechaFinNueva) {
        this.fechaFinNueva = fechaFinNueva;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
