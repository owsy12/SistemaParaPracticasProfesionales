package Logic.DTOs;

public class Professor extends User{
    private String academicArea;


    public Professor() {
        super();
    }

    public Professor(int id, String matricula, String firstName,
                        String lastName, String secondLastName,
                        String password, String status,
                        String academicArea) {

        super(id, matricula, firstName, lastName, secondLastName, password, status);
        this.academicArea = academicArea;

    }

    public String getAcademicArea() {
        return academicArea; }

    public void setAcademicArea(String academicArea) {
        this.academicArea = academicArea; }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + matricula + ")";
    }

}
