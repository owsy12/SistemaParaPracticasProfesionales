package Logic.DTOs;

public class EducationalExperience {
    private String nrc;
    private String name;
    private int idProfessor;
    private String period;

    public EducationalExperience() {
    }

    public EducationalExperience(String nrc, String name, int idProfessor) {
        this.nrc = nrc;
        this.name = name;
        this.idProfessor = idProfessor;
    }

    public String getNrc() {
        return nrc;
    }

    public void setNrc(String nrc) {
        this.nrc = nrc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIdProfessor() {
        return idProfessor;
    }

    public void setIdProfessor(int idProfessor) {
        this.idProfessor = idProfessor;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    @Override
    public String toString() {
        return nrc + " - " + name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        EducationalExperience other = (EducationalExperience) object;
        return java.util.Objects.equals(nrc, other.nrc)
                && java.util.Objects.equals(name, other.name)
                && idProfessor == other.idProfessor;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(nrc, name, idProfessor);
    }
}
