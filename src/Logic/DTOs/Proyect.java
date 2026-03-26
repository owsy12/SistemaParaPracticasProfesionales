package Logic.DTOs;

import java.time.LocalDate;

public class Proyect {

    private int idProject;
    private int idOrganization;
    private int idTechnician;
    private int idCoordinator;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int maximumCapacity;
    private int availableCapacity;
    private String state;

    public Proyect() {}

    public Proyect(int idProject, int idOrganization, int idTechnician, int idCoordinator,
                   String name, String description,
                   LocalDate startDate, LocalDate endDate,
                   int maximumCapacity, int availableCapacity, String state) {
        this.idProject = idProject;
        this.idOrganization = idOrganization;
        this.idTechnician = idTechnician;
        this.idCoordinator = idCoordinator;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maximumCapacity = maximumCapacity;
        this.availableCapacity = availableCapacity;
        this.state = state;
    }

    public int getIdProject() { return idProject; }
    public void setIdProject(int idProject) { this.idProject = idProject; }

    public int getIdOrganization() { return idOrganization; }
    public void setIdOrganization(int idOrganization) { this.idOrganization = idOrganization; }

    public int getIdTechnician() { return idTechnician; }
    public void setIdTechnician(int idTechnician) { this.idTechnician = idTechnician; }

    public int getIdCoordinator() { return idCoordinator; }
    public void setIdCoordinator(int idCoordinator) { this.idCoordinator = idCoordinator; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getMaximumCapacity() { return maximumCapacity; }
    public void setMaximumCapacity(int maximumCapacity) { this.maximumCapacity = maximumCapacity; }

    public int getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(int availableCapacity) { this.availableCapacity = availableCapacity; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}