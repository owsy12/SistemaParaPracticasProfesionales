package GUI.DocumentGeneration;

import Logic.DTOs.Activity;
import Logic.DTOs.Intern;
import Logic.DTOs.LinkedOrganization;
import Logic.DTOs.Professor;
import Logic.DTOs.Project;
import Logic.DTOs.TechnicalResponsible;

import java.util.List;

public class InternContext {

    private final Intern intern;
    private final Project project;
    private final LinkedOrganization organization;
    private final TechnicalResponsible supervisor;
    private final Professor professor;
    private final int approvedHours;
    private final List<Activity> activities;
    private final boolean hasAssignment;

    InternContext(InternContextBuilder builder) {
        this.intern = builder.getIntern();
        this.project = builder.getProject();
        this.organization = builder.getOrganization();
        this.supervisor = builder.getSupervisor();
        this.professor = builder.getProfessor();
        this.approvedHours = builder.getApprovedHours();
        this.activities = builder.getActivities();
        this.hasAssignment = builder.isHasAssignment();
    }

    public Intern getIntern() {
        return intern;
    }

    public Project getProject() {
        return project;
    }

    public LinkedOrganization getOrganization() {
        return organization;
    }

    public TechnicalResponsible getSupervisor() {
        return supervisor;
    }

    public Professor getProfessor() {
        return professor;
    }

    public int getApprovedHours() {
        return approvedHours;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public boolean hasAssignment() {
        return hasAssignment;
    }
}
