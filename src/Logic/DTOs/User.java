package Logic.DTOs;

import java.util.List;

public class User{

    protected int id;
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

    public User(int id, String registrationNumber, String firstName, String lastName,
                   String secondLastName, String password, String status) {
        this.id = id;
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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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
        return id == other.id
                && java.util.Objects.equals(registrationNumber, other.registrationNumber)
                && java.util.Objects.equals(firstName, other.firstName)
                && java.util.Objects.equals(lastName, other.lastName)
                && java.util.Objects.equals(secondLastName, other.secondLastName)
                && java.util.Objects.equals(password, other.password)
                && java.util.Objects.equals(status, other.status)
                && java.util.Objects.equals(role, other.role)
                && java.util.Objects.equals(roles, other.roles)
                && java.util.Objects.equals(email, other.email);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, registrationNumber, firstName, lastName, secondLastName, password, status, role, roles, email);
    }
}