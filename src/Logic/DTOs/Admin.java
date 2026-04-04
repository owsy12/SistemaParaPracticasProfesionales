package Logic.DTOs;

public class Admin extends User {

    public Admin() {}

    public Admin(int id, String matricula, String firstName, String lastName,
                 String secondLastName, String password, String status) {
        super(id, matricula, firstName, lastName, secondLastName, password, status);
    }
}