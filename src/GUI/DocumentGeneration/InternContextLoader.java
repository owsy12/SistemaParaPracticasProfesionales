package GUI.DocumentGeneration;

import Logic.DAO.ActivityDAO;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.InternDAO;
import Logic.DAO.LinkedOrganizationDAO;
import Logic.DAO.ProfessorDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.ReportDAO;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.Activity;
import Logic.DTOs.Assignment;
import Logic.DTOs.Intern;
import Logic.DTOs.LinkedOrganization;
import Logic.DTOs.Professor;
import Logic.DTOs.Project;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InternContextLoader {

    private static final Logger LOGGER = Logger.getLogger(InternContextLoader.class.getName());

    private InternContextLoader() {
    }

    public static InternContext load(int internId) throws ValidationException, ServiceException {
        InternDAO internDAO = new InternDAO();
        Intern intern = internDAO.findById(internId);

        AssignmentDAO assignmentDAO = new AssignmentDAO();
        Assignment assignment = assignmentDAO.getActiveByIdIntern(internId);

        boolean hasAssignment = assignment != null;
        InternContext context;
        if (hasAssignment) {
            context = loadWithProject(intern, assignment, internId);
        } else {
            context = buildContextWithoutProject(intern);
        }
        return context;
    }

    private static InternContext buildContextWithoutProject(Intern intern) {
        InternContext context = new InternContext(intern, null, null, null,
                null, 0, new ArrayList<>(), false);
        return context;
    }

    private static InternContext loadWithProject(Intern intern, Assignment assignment, int internId)
            throws ValidationException, ServiceException {
        ProjectDAO projectDAO = new ProjectDAO();
        Project project = projectDAO.findById(assignment.getIdProject());

        LinkedOrganizationDAO orgDAO = new LinkedOrganizationDAO();
        LinkedOrganization organization = orgDAO.findById(project.getIdOrganization());

        TechnicalResponsibleDAO techDAO = new TechnicalResponsibleDAO();
        TechnicalSupervisor supervisor = techDAO.findById(project.getIdTechnicalSupervisor());

        ProfessorDAO professorDAO = new ProfessorDAO();
        Professor professor = professorDAO.findById(project.getIdProfessor());

        ReportDAO reportDAO = new ReportDAO();
        int approvedHours = reportDAO.getTotalApprovedHoursByIntern(internId);

        List<Activity> activities = loadActivities(project.getIdProject());

        InternContext context = new InternContext(intern, project, organization, supervisor, professor, approvedHours, activities, true);
        return context;
    }

    private static List<Activity> loadActivities(int projectId) {
        List<Activity> activities = new ArrayList<>();
        try {
            ActivityDAO activityDAO = new ActivityDAO();
            activities = activityDAO.findByProject(projectId);
        } catch (ValidationException | ServiceException persistenceException) {
            LOGGER.log(Level.WARNING,
                    "No se pudieron cargar actividades del proyecto {0}: {1}",
                    new Object[]{projectId, persistenceException.getMessage()});
        }
        return activities;
    }
}
