package Logic.DTOs;

public class UserRole {

    private int    idUsuario;
    private String role;

    public UserRole() {}

    public UserRole(int idUsuario, String role) {
        this.idUsuario = idUsuario;
        this.role      = role;
    }

    public int    getIdUsuario()             { return idUsuario; }
    public void   setIdUsuario(int idUsuario){ this.idUsuario = idUsuario; }

    public String getRole()                  { return role; }
    public void   setRole(String role)       { this.role = role; }
}