package Logic.DTOs;

public class TechnicalSupervisor {
    private int idTechnicalSupervisor;
    private int idOrganization;
    private String name;
    private String lastName;
    private String secondLastName;
    private String eMail;
    private String position;


    public TechnicalSupervisor(int idTechnicalSupervisor, int idOrganization, String name, String lastName, String secondLastName, String eMail, String position) {
        this.idTechnicalSupervisor = idTechnicalSupervisor;
        this.idOrganization = idOrganization;
        this.name = name;
        this.lastName = lastName;
        this.secondLastName = secondLastName;
        this.eMail = eMail;
        this.position = position;
    }


    public TechnicalSupervisor() {
    }

    public int getIdTechnicalSupervisor() {
        return idTechnicalSupervisor;
    }

    public void setIdTechnicalSupervisor(int idTechnicalSupervisor) {
        this.idTechnicalSupervisor = idTechnicalSupervisor;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
