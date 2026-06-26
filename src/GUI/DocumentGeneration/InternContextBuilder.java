package GUI.DocumentGeneration;

import Logic.DTOs.Activity;
import Logic.DTOs.Intern;
import Logic.DTOs.LinkedOrganization;
import Logic.DTOs.Professor;
import Logic.DTOs.Project;
import Logic.DTOs.TechnicalResponsible;

import java.util.ArrayList;
import java.util.List;

public class InternContextBuilder {

    private Intern intern = null;
    private Project project = null;
    private LinkedOrganization organization = null;
    private TechnicalResponsible supervisor = null;
    private Professor professor = null;
    private int approvedHours = 0;
    private List<Activity> activities = new ArrayList<>();
    private boolean hasAssignment = false;

    public InternContextBuilder intern(Intern value) {
        intern = value;
        return this;
    }

    public InternContextBuilder project(Project value) {
        project = value;
        return this;
    }

    public InternContextBuilder organization(LinkedOrganization value) {
        organization = value;
        return this;
    }

    public InternContextBuilder supervisor(TechnicalResponsible value) {
        supervisor = value;
        return this;
    }

    public InternContextBuilder professor(Professor value) {
        professor = value;
        return this;
    }

    public InternContextBuilder approvedHours(int value) {
        approvedHours = value;
        return this;
    }

    public InternContextBuilder activities(List<Activity> value) {
        activities = value;
        return this;
    }

    public InternContextBuilder hasAssignment(boolean value) {
        hasAssignment = value;
        return this;
    }

    public Intern getIntern() { return intern; }
    public Project getProject() { return project; }
    public LinkedOrganization getOrganization() { return organization; }
    public TechnicalResponsible getSupervisor() { return supervisor; }
    public Professor getProfessor() { return professor; }
    public int getApprovedHours() { return approvedHours; }
    public List<Activity> getActivities() { return activities; }
    public boolean isHasAssignment() { return hasAssignment; }

    public InternContext build() {
        InternContext context = new InternContext(this);
        return context;
    }
}
