package Logic.DTOs;


import java.util.Date;

public class Admin extends User {
    private int idAdmin;

    public Admin(int idAdmin, int idUser, String name, String firstLastName,
                 String secondLastName, String matricula, String password,
                 String state) {

        super(idUser, name, firstLastName, secondLastName, matricula, password, state);
        this.idAdmin = idAdmin;
    }

    public Admin() {
        super();
    }

    public Admin(int idUsuario, String matricula, String nombre, String apellidoPaterno, String apellidoMaterno, String contrasenia, String estado) {
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }
}
