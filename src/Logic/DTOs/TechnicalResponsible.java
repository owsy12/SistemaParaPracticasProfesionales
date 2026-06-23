package Logic.DTOs;

public class TechnicalResponsible {
    private int idTechnicalResponsible;
    private int idOrganization;
    private String name;
    private String lastName;
    private String secondLastName;
    private String eMail;
    private String position;


    public TechnicalResponsible(int idTechnicalResponsible, int idOrganization, String name, String lastName, String secondLastName, String eMail, String position) {
        this.idTechnicalResponsible = idTechnicalResponsible;
        this.idOrganization = idOrganization;
        this.name = name;
        this.lastName = lastName;
        this.secondLastName = secondLastName;
        this.eMail = eMail;
        this.position = position;
    }


    public TechnicalResponsible() {
    }

    public int getIdTechnicalResponsible() {
        return idTechnicalResponsible;
    }

    public void setIdTechnicalResponsible(int idTechnicalResponsible) {
        this.idTechnicalResponsible = idTechnicalResponsible;
    }

    public int getIdOrganization() {
        return idOrganization;
    }

    public void setIdOrganization(int idOrganization) {
        this.idOrganization = idOrganization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSecondLastName() {
        return secondLastName;
    }

    public void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getEmail() {
        return eMail;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
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
        TechnicalResponsible other = (TechnicalResponsible) object;
        return idTechnicalResponsible == other.idTechnicalResponsible
                && idOrganization == other.idOrganization
                && java.util.Objects.equals(name, other.name)
                && java.util.Objects.equals(lastName, other.lastName)
                && java.util.Objects.equals(secondLastName, other.secondLastName)
                && java.util.Objects.equals(eMail, other.eMail)
                && java.util.Objects.equals(position, other.position);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idTechnicalResponsible, idOrganization, name, lastName, secondLastName, eMail, position);
    }
}
