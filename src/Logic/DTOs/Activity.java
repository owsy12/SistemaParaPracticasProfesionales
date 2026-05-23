package Logic.DTOs;

import java.time.LocalDate;

public class Activity {
    private int idActivity;
    private int idProject;
    private String name;
    private String description;
    private LocalDate creationDate;
    private String status;
    private String projectName;
    private int semanaInicioPlan;
    private int semanaFinPlan;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public Activity(int idActivity, int idProject, String name, String description,
                    LocalDate creationDate, String status) {
        this.idActivity    = idActivity;
        this.idProject     = idProject;
        this.name          = name;
        this.description   = description;
        this.creationDate  = creationDate;
        this.status        = status;
    }

    public Activity() {
    }

    public int getIdActivity() {
        return idActivity;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
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

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getSemanaInicioPlan() {
        return semanaInicioPlan;
    }

    public void setSemanaInicioPlan(int semanaInicioPlan) {
        this.semanaInicioPlan = semanaInicioPlan;
    }

    public int getSemanaFinPlan() {
        return semanaFinPlan;
    }

    public void setSemanaFinPlan(int semanaFinPlan) {
        this.semanaFinPlan = semanaFinPlan;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    @Override
    public String toString() {
        return name;
    }
}
