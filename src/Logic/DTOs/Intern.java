package Logic.DTOs;

public class Intern extends User {

    private int credits;

    public Intern() {
        super();
    }

    public Intern(int id, String matricula, String firstName,
                     String lastName, String secondLastName,
                     String password, String status,
                     int credits) {

        super(id, matricula, firstName, lastName, secondLastName, password, status);
        this.credits = credits;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }
}