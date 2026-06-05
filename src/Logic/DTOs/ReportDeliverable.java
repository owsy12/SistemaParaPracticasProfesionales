package Logic.DTOs;

public class ReportDeliverable {
    private int idReporteEntregable;
    private int idReport;
    private String resultado;
    private String descripcion;
    private int advancePercentage;
    private String observaciones;

    public ReportDeliverable() {
    }

    public int getIdReporteEntregable() {
        return idReporteEntregable;
    }

    public void setIdReporteEntregable(int idReporteEntregable) {
        this.idReporteEntregable = idReporteEntregable;
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
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

    public int getAdvancePercentage() {
        return advancePercentage;
    }

    public void setAdvancePercentage(int advancePercentage) {
        this.advancePercentage = advancePercentage;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getAdvanceDisplay() {
        String display = advancePercentage + "%";
        return display;
    }

    public String getDescripcionDisplay() {
        String display = "";
        if (descripcion != null) {
            display = descripcion;
        }
        return display;
    }

    public String getObservacionesDisplay() {
        String display = "";
        if (observaciones != null) {
            display = observaciones;
        }
        return display;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ReportDeliverable other = (ReportDeliverable) object;
        return idReporteEntregable == other.idReporteEntregable
                && idReport == other.idReport
                && java.util.Objects.equals(resultado, other.resultado)
                && java.util.Objects.equals(descripcion, other.descripcion)
                && advancePercentage == other.advancePercentage
                && java.util.Objects.equals(observaciones, other.observaciones);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idReporteEntregable, idReport, resultado, descripcion, advancePercentage, observaciones);
    }
}
