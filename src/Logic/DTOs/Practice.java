package Logic.DTOs;

import java.time.LocalDate;

public class Practice {
    private int idPractice;
    private String nrc;
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
                && java.util.Objects.equals(nrc, other.nrc)
                && idIntern == other.idIntern
                && java.util.Objects.equals(startDate, other.startDate)
                && java.util.Objects.equals(endDate, other.endDate)
                && java.util.Objects.equals(status, other.status)
                && java.util.Objects.equals(grade, other.grade);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idPractice, nrc, idIntern, startDate, endDate, status, grade);
    }
}
