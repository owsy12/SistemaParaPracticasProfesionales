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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        Coordinator other = (Coordinator) object;
        return isProfessor == other.isProfessor;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), isProfessor);
    }
}