package Logic.DTOs;

import java.util.Date;

public class Project {
    private int idProyect;
    private int idOrganization;
    private int idTechnicalSupervisor;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private int avaliablePlaces;
    private int maximumPlaces;


    public Project(int idProyect, int idOrganization, int idTechnicalSupervisor, String name, String description, Date startDate, Date endDate, int avaliablePlaces, int maximumPlaces) {
        this.idProyect = idProyect;
        this.idOrganization = idOrganization;
        this.idTechnicalSupervisor = idTechnicalSupervisor;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.avaliablePlaces = avaliablePlaces;
        this.maximumPlaces = maximumPlaces;
    }

    public Project() {
    }

    public int getIdProyect() {
        return idProyect;
    }

    public void setIdProyect(int idProyect) {
        this.idProyect = idProyect;
    }

    public int getIdOrganization() {
        return idOrganization;
    }

    public void setIdOrganization(int idOrganization) {
        this.idOrganization = idOrganization;
    }

    public int getIdTechnicalSupervisor() {
        return idTechnicalSupervisor;
    }

    public void setIdTechnicalSupervisor(int idTechnicalSupervisor) {
        this.idTechnicalSupervisor = idTechnicalSupervisor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getAvaliablePlaces() {
        return avaliablePlaces;
    }

    public void setAvaliablePlaces(int avaliablePlaces) {
        this.avaliablePlaces = avaliablePlaces;
    }

    public int getMaximumPlaces() {
        return maximumPlaces;
    }

    public void setMaximumPlaces(int maximumPlaces) {
        this.maximumPlaces = maximumPlaces;
    }
}
