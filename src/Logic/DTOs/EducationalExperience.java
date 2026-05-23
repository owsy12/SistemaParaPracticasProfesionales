package Logic.DTOs;

public class EducationalExperience {
    private String nrc;
    private String name;
    private int idProfessor;

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

    @Override
    public String toString() {
        return nrc + " - " + name;
    }
}
