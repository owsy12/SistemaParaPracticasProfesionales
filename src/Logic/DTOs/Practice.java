package Logic.DTOs;

import java.util.Objects;
import java.time.LocalDate;

public class Practice {
    private int idPractice;
    private String nrc;
    private String period;
    private int idIntern;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Double grade;

    public Practice() {
    }

    public int getIdPractice() {
        return idPractice;
    }

    public void setIdPractice(int idPractice) {
        this.idPractice = idPractice;
    }

    public String getNrc() {
        return nrc;
    }

    public void setNrc(String nrc) {
        this.nrc = nrc;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getIdIntern() {
        return idIntern;
    }

    public void setIdIntern(int idIntern) {
        this.idIntern = idIntern;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Practice other = (Practice) object;
        return idPractice == other.idPractice
                && Objects.equals(nrc, other.nrc)
                && Objects.equals(period, other.period)
                && idIntern == other.idIntern
                && Objects.equals(startDate, other.startDate)
                && Objects.equals(endDate, other.endDate)
                && Objects.equals(status, other.status)
                && Objects.equals(grade, other.grade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPractice, nrc, period, idIntern, startDate, endDate, status, grade);
    }
}
