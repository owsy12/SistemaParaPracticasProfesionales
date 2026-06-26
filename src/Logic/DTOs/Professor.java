package Logic.DTOs;

import java.util.Objects;

public class Professor extends User{
    private String academicArea;


    public Professor() {
        super();
    }

    public Professor(int id, String registrationNumber, String firstName,
                        String lastName, String secondLastName,
                        String password, String status,
                        String academicArea) {

        super(id, registrationNumber, firstName, lastName, secondLastName, password, status);
        this.academicArea = academicArea;

    }

    public String getAcademicArea() {
        return academicArea; }

    public void setAcademicArea(String academicArea) {
        this.academicArea = academicArea; }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + registrationNumber + ")";
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
        Professor other = (Professor) object;
        return Objects.equals(academicArea, other.academicArea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), academicArea);
    }
}
