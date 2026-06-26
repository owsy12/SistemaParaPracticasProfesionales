package Logic.DTOs;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Project {
    private int idProject;
    private int idOrganization;
    private int idTechnicalResponsible;
    private int IdProfessor;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int availablePlaces;
    private int maximumPlaces;
    private String organizationName;
    private String status;
    private String objetivo;
    private String nrc;
    private String period;


    public Project(int idProject, int idOrganization, int idTechnicalResponsible, String name, String description, LocalDate startDate, LocalDate endDate, int availablePlaces, int maximumPlaces) {
        this.idProject = idProject;
        this.idOrganization = idOrganization;
        this.idTechnicalResponsible = idTechnicalResponsible;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.availablePlaces = availablePlaces;
        this.maximumPlaces = maximumPlaces;
    }

    public Project() {
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public int getIdOrganization() {
        return idOrganization;
    }

    public void setIdOrganization(int idOrganization) {
        this.idOrganization = idOrganization;
    }

    public int getIdTechnicalResponsible() {
        return idTechnicalResponsible;
    }

    public void setIdTechnicalResponsible(int idTechnicalResponsible) {
        this.idTechnicalResponsible = idTechnicalResponsible;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getAvailablePlaces() {
        return availablePlaces;
    }

    public void setAvailablePlaces(int availablePlaces) {
        this.availablePlaces = availablePlaces;
    }

    public int getMaximumPlaces() {
        return maximumPlaces;
    }

    public void setMaximumPlaces(int maximumPlaces) {
        this.maximumPlaces = maximumPlaces;
    }
    public int getIdProfessor() {
        return IdProfessor;
    }

    public void setIdProfessor(int idProfessor) {
        IdProfessor = idProfessor;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getNrc() {
        return nrc;
    }

    public void setNrc(String nrc) {
        this.nrc = nrc;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private String preferenceLabel = "";

    public String getPreferenceLabel() {
        return preferenceLabel;
    }

    public void setPreferenceLabel(String preferenceLabel) {
        this.preferenceLabel = preferenceLabel;
    }

    private int selectionOrder = 0;

    public int getSelectionOrder() {
        return selectionOrder;
    }

    public void setSelectionOrder(int selectionOrder) {
        this.selectionOrder = selectionOrder;
    }

    public String getSelectionOrderDisplay() {
        String display = "";
        if (selectionOrder > 0) {
            display = String.valueOf(selectionOrder);
        }
        return display;
    }

    public String getStartDateDisplay() {
        String display = "";
        if (startDate != null) {
            display = startDate.toString();
        }
        return display;
    }

    public String getEndDateDisplay() {
        String display = "";
        if (endDate != null) {
            display = endDate.toString();
        }
        return display;
    }

    public String getStartDateFormatted() {
        String display = "";
        if (startDate != null) {
            display = startDate.format(DISPLAY_DATE_FORMAT);
        }
        return display;
    }

    public String getEndDateFormatted() {
        String display = "";
        if (endDate != null) {
            display = endDate.format(DISPLAY_DATE_FORMAT);
        }
        return display;
    }

    public String getPeriodDisplay() {
        String period = "";
        if (startDate != null && endDate != null) {
            period = startDate.format(DISPLAY_DATE_FORMAT)
                    + " - " + endDate.format(DISPLAY_DATE_FORMAT);
        }
        return period;
    }

    public String getPeriodIsoDisplay() {
        String period = "";
        if (startDate != null && endDate != null) {
            period = startDate.toString() + " - " + endDate.toString();
        }
        return period;
    }

    public String getMaximumPlacesDisplay() {
        String display = String.valueOf(maximumPlaces);
        return display;
    }

    public String getAvailablePlacesDisplay() {
        String display = String.valueOf(availablePlaces);
        return display;
    }

    public String getOrganizationNameDisplay() {
        String display = "";
        if (organizationName != null) {
            display = organizationName;
        }
        return display;
    }

    public String getIdProjectDisplay() {
        String display = String.valueOf(idProject);
        return display;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Project otherProject = (Project) other;
        boolean isEqual = idProject == otherProject.idProject
                && idOrganization == otherProject.idOrganization
                && idTechnicalResponsible == otherProject.idTechnicalResponsible
                && IdProfessor == otherProject.IdProfessor
                && availablePlaces == otherProject.availablePlaces
                && maximumPlaces == otherProject.maximumPlaces
                && Objects.equals(name, otherProject.name)
                && Objects.equals(description, otherProject.description)
                && Objects.equals(startDate, otherProject.startDate)
                && Objects.equals(endDate, otherProject.endDate)
                && Objects.equals(organizationName, otherProject.organizationName)
                && Objects.equals(status, otherProject.status)
                && Objects.equals(objetivo, otherProject.objetivo)
                && Objects.equals(nrc, otherProject.nrc);
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProject, idOrganization, idTechnicalResponsible,
                IdProfessor, availablePlaces, maximumPlaces, name, description,
                startDate, endDate, organizationName, status, objetivo, nrc);
    }

    @Override
    public String toString() {
        return name;
    }
}
