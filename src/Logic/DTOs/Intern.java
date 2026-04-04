package Logic.DTOs;

public class Intern extends User {

    private int creditos;

    public Intern() {}

    public Intern(int id, String matricula, String firstName, String lastName,
                  String secondLastName, String password, String status, int creditos) {
        super(id, matricula, firstName, lastName, secondLastName, password, status);
        this.creditos = creditos;
    }

    public int getCreditos()           { return creditos; }
    public void setCreditos(int creditos) { this.creditos = creditos; }
}