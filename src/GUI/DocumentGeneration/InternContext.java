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

    public InternContext(Intern intern, Project project, LinkedOrganization organization,
                         TechnicalResponsible supervisor, Professor professor,
                         int approvedHours, List<Activity> activities, boolean hasAssignment) {
        this.intern = intern;
        this.project = project;
        this.organization = organization;
        this.supervisor = supervisor;
        this.professor = professor;
        this.approvedHours = approvedHours;
        this.activities = activities;
        this.hasAssignment = hasAssignment;
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
