package Logic.DTOs;

public class Coordinator extends User {

    public Coordinator() {}

    public Coordinator(int id, String matricula, String firstName, String lastName,
                       String secondLastName, String password, String status) {
        super(id, matricula, firstName, lastName, secondLastName, password, status);
    }
}