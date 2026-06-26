package Logic.DTOs;

import java.util.Objects;


public class Administrator extends User {
    private int idAdministrator;

    public Administrator(int idAdministrator, int idUser, String name, String firstLastName,
                         String secondLastName, String registrationNumber, String password,
                         String state) {

        super(idUser, registrationNumber, name, firstLastName, secondLastName, password, state);
        this.idAdministrator = idAdministrator;
    }

    public Administrator() {
        super();
    }

    public Administrator(int idUser, String registrationNumber, String name, String firstLastName, String secondLastName, String password, String state) {
        super(idUser, registrationNumber, name, firstLastName, secondLastName, password, state);
    }

    public int getIdAdministrator() {
        return idAdministrator;
    }

    public void setIdAdministrator(int idAdministrator) {
        this.idAdministrator = idAdministrator;
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
        Administrator other = (Administrator) object;
        return idAdministrator == other.idAdministrator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idAdministrator);
    }
}
