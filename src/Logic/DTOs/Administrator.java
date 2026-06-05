package Logic.DTOs;


public class Administrator extends User {
    private int idAdmin;

    public Administrator(int idAdmin, int idUser, String name, String firstLastName,
                         String secondLastName, String registrationNumber, String password,
                         String state) {

        super(idUser, registrationNumber, name, firstLastName, secondLastName, password, state);
        this.idAdmin = idAdmin;
    }

    public Administrator() {
        super();
    }

    public Administrator(int idUsuario, String registrationNumber, String nombre, String apellidoPaterno, String apellidoMaterno, String contrasenia, String estado) {
        super(idUsuario, registrationNumber, nombre, apellidoPaterno, apellidoMaterno, contrasenia, estado);
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
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
        return idAdmin == other.idAdmin;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), idAdmin);
    }
}
