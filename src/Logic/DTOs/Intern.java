package Logic.DTOs;

public class Intern extends User {

    private int credits;

    public Intern() {
        super();
    }

    public Intern(int id, String registrationNumber, String firstName,
                     String lastName, String secondLastName,
                     String password, String status,
                     int credits) {

        super(id, registrationNumber, firstName, lastName, secondLastName, password, status);
        this.credits = credits;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        String result = "";
        boolean hasName = getFirstName() != null;
        if (hasName) {
            result = getFirstName() + " " + getLastName() + " " + getSecondLastName();
        }
        return result;
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
        Intern other = (Intern) object;
        return credits == other.credits;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), credits);
    }
}