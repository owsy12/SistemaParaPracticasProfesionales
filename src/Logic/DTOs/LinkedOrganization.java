package Logic.DTOs;

public class LinkedOrganization {
    private int idLinkedOrganization;
    private String name;
    private String sector;
    private String city;
    private String department;

    public LinkedOrganization(int idLinkedOrganization, String name, String sector, String city, String department) {
        this.idLinkedOrganization = idLinkedOrganization;
        this.name = name;
        this.sector = sector;
        this.city = city;
        this.department = department;
    }

    public LinkedOrganization() {
    }

    public int getIdLinkedOrganization() {
        return idLinkedOrganization;
    }

    public String getName() {
        return name;
    }

    public String getSector() {
        return sector;
    }

    public String getCity() {
        return city;
    }

    public String getDepartment() {
        return department;
    }

    public void setIdLinkedOrganization(int idLinkedOrganization) {
        this.idLinkedOrganization = idLinkedOrganization;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
