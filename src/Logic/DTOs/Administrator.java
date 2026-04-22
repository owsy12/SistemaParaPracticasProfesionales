package Logic.DTOs;


public class Administrator extends User {
    private int idAdmin;

    public Administrator(int idAdmin, int idUser, String name, String firstLastName,
                         String secondLastName, String matricula, String password,
                         String state) {

        super(idUser, matricula, name, firstLastName, secondLastName, password, state);
        this.idAdmin = idAdmin;
    }

    public Administrator() {
        super();
    }

    public Administrator(int idUsuario, String matricula, String nombre, String apellidoPaterno, String apellidoMaterno, String contrasenia, String estado) {
        super(idUsuario, matricula, nombre, apellidoPaterno, apellidoMaterno, contrasenia, estado);
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }
}
