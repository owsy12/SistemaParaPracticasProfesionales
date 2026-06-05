package Logic.DTOs;

import java.time.LocalDate;

public class Prorroga {

    private int idProrroga;
    private int idActivity;
    private LocalDate originalEndDate;
    private LocalDate newEndDate;
    private String motivo;

    public Prorroga() {
    }

    public int getIdProrroga() {
        return idProrroga;
    }

    public void setIdProrroga(int idProrroga) {
        this.idProrroga = idProrroga;
    }

    public int getIdActivity() {
        return idActivity;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public LocalDate getOriginalEndDate() {
        return originalEndDate;
    }

    public void setOriginalEndDate(LocalDate originalEndDate) {
        this.originalEndDate = originalEndDate;
    }

    public LocalDate getNewEndDate() {
        return newEndDate;
    }

    public void setNewEndDate(LocalDate newEndDate) {
        this.newEndDate = newEndDate;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Prorroga other = (Prorroga) object;
        return idProrroga == other.idProrroga
                && idActivity == other.idActivity
                && java.util.Objects.equals(originalEndDate, other.originalEndDate)
                && java.util.Objects.equals(newEndDate, other.newEndDate)
                && java.util.Objects.equals(motivo, other.motivo);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idProrroga, idActivity, originalEndDate, newEndDate, motivo);
    }
}
