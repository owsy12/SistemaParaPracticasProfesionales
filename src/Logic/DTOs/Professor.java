package Logic.DTOs;

public class Professor extends User {

    private String academica;

    public Professor() {}

    public Professor(int id, String matricula, String firstName, String lastName,
                     String secondLastName, String password, String status, String academica) {
        super(id, matricula, firstName, lastName, secondLastName, password, status);
        this.academica = academica;
    }

    public String getAcademica()               { return academica; }
    public void setAcademica(String academica) { this.academica = academica; }
}