package Logic.DTOs;

public class ReportDeliverable {
    private int idReporteEntregable;
    private int idReporte;
    private String resultado;
    private String descripcion;
    private int porcentajeAvance;
    private String observaciones;

    public ReportDeliverable() {
    }

    public int getIdReporteEntregable() {
        return idReporteEntregable;
    }

    public void setIdReporteEntregable(int idReporteEntregable) {
        this.idReporteEntregable = idReporteEntregable;
    }

    public int getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(int idReporte) {
        this.idReporte = idReporte;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public void setPorcentajeAvance(int porcentajeAvance) {
        this.porcentajeAvance = porcentajeAvance;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
