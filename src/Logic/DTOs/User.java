package Logic.DTOs;

import java.util.Objects;
import java.util.List;

public class User{

    protected int idUser;
    protected String registrationNumber;
    protected String firstName;
    protected String lastName;
    protected String secondLastName;
    protected String password;
    protected String status;
    protected String role;
    protected List<String> roles;
    protected String email;

    public User() {}

    public User(int idUser, String registrationNumber, String firstName, String lastName,
                   String secondLastName, String password, String status) {
        this.idUser = idUser;
        this.registrationNumber = registrationNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.secondLastName = secondLastName;
        this.password = password;
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSecondLastName() { return secondLastName; }
    public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        String second = "";
        if (secondLastName != null && !secondLastName.isEmpty()) {
            second = " " + secondLastName;
        }
        String fullName = firstName + " " + lastName + second;
        return fullName;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        User other = (User) object;
        return idUser == other.idUser
                && Objects.equals(registrationNumber, other.registrationNumber)
                && Objects.equals(firstName, other.firstName)
                && Objects.equals(lastName, other.lastName)
                && Objects.equals(secondLastName, other.secondLastName)
                && Objects.equals(password, other.password)
                && Objects.equals(status, other.status)
                && Objects.equals(role, other.role)
                && Objects.equals(roles, other.roles)
                && Objects.equals(email, other.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser, registrationNumber, firstName, lastName, secondLastName, password, status, role, roles, email);
    }
}