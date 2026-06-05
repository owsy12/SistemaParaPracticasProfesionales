package Logic.DTOs;

public class LinkedOrganization {
    private int idLinkedOrganization;
    private String name;
    private String sector;
    private String address;
    private String email;
    private String status;

    public LinkedOrganization(int idLinkedOrganization, String name, String sector, String address, String department) {
        this.idLinkedOrganization = idLinkedOrganization;
        this.name = name;
        this.sector = sector;
        this.address = address;
        this.email = department;
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

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
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

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        LinkedOrganization other = (LinkedOrganization) object;
        return idLinkedOrganization == other.idLinkedOrganization
                && java.util.Objects.equals(name, other.name)
                && java.util.Objects.equals(sector, other.sector)
                && java.util.Objects.equals(address, other.address)
                && java.util.Objects.equals(email, other.email)
                && java.util.Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idLinkedOrganization, name, sector, address, email, status);
    }
}
