package Logic.DTOs;

public class Coordinator extends User {
    private int isProfessor;

    public Coordinator() {
        super();
    }

    public Coordinator(int id, String registrationNumber, String firstName,
                          String lastName, String secondLastName,
                          String password, String status) {
        super(id, registrationNumber, firstName, lastName, secondLastName, password, status);
    }

    public int getIsProfessor() {
        return isProfessor;
    }

    public void setIsProfessor(int isProfessor) {
        this.isProfessor = isProfessor;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + registrationNumber + ")";
    }
}