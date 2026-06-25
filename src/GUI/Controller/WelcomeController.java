package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.CoordinatorDAO;
import Logic.DAO.EducationalExperienceDAO;
import Logic.DAO.InternDAO;
import Logic.DAO.ProfessorDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.ReportDAO;
import Logic.DTOs.Assignment;
import Logic.DTOs.EducationalExperience;
import Logic.DTOs.Project;
import Logic.DTOs.Report;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WelcomeController {

    private static final Logger LOGGER = Logger.getLogger(WelcomeController.class.getName());
    private static final String STATUS_PENDING = "Pendiente";
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})");

    @FXML private Label welcomeUserGreetingLabel;
    @FXML private Label welcomeSubtitleLabel;

    @FXML private VBox practicanteContentVBox;
    @FXML private Label internPracticeStatusBadgeLabel;
    @FXML private Label internPracticeStatusValueLabel;
    @FXML private Label internPracticeStatusSubLabel;
    @FXML private Label internReportsBadgeLabel;
    @FXML private Label internPendingReportsCountLabel;
    @FXML private Label internReportDetailLabel;
    @FXML private Label internTutorEvalLabel;
    @FXML private Label internTutorEvalDetailLabel;

    @FXML private VBox profesorContentVBox;
    @FXML private Label profPendingCountLabel;
    @FXML private Label profPendingDetailLabel;
    @FXML private Label profTotalReportsLabel;
    @FXML private Label profInternsCountLabel;
    @FXML private Label profEvalDoneLabel;
    @FXML private Label profEducationalExperiencesCountLabel;
    @FXML private Label profEducationalExperiencesDetailLabel;

    @FXML private VBox coordinadorContentVBox;
    @FXML private Label coordProjectsCountLabel;
    @FXML private Label coordInternsCountLabel;
    @FXML private Label coordPendingReportsLabel;
    @FXML private Label coordSlotsCountLabel;

    @FXML private VBox adminContentVBox;
    @FXML private Label adminCoordCountLabel;
    @FXML private Label adminProfCountLabel;
    @FXML private Label adminInternsCountLabel;
    @FXML private Label adminProjectsCountLabel;

    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getUser();
        populateGreeting(user);
        loadDashboardByRoles(user);
    }

    private void populateGreeting(User user) {
        String firstName = (user != null && user.getFirstName() != null) ? user.getFirstName() : "Usuario";
        String greetingText = "¡Bienvenido, " + firstName + "!";
        welcomeUserGreetingLabel.setText(greetingText);
    }

    private void loadDashboardByRoles(User user) {
        if (user == null || user.getRoles() == null) {
            welcomeSubtitleLabel.setText("No se pudo determinar el rol del usuario.");
        } else {

            List<String> roles = user.getRoles();
            boolean sectionLoaded = false;

            for (String role : roles) {
                switch (role) {
                    case "Practicante":
                        loadPracticanteDashboard(user);
                        sectionLoaded = true;
                        break;
                    case "Profesor":
                        loadProfesorDashboard(user);
                        sectionLoaded = true;
                        break;
                    case "Coordinador":
                        loadCoordinadorDashboard();
                        sectionLoaded = true;
                        break;
                    case "Administrador":
                        loadAdministradorDashboard();
                        sectionLoaded = true;
                        break;
                }
            }

            if (!sectionLoaded) {
                welcomeSubtitleLabel.setText("Bienvenido al Sistema de Gestión de Prácticas Profesionales.");
            }
        }
    }

    private void loadPracticanteDashboard(User user) {
        showSection(practicanteContentVBox,
                "Aquí tienes un resumen de tus prácticas profesionales y oportunidades activas.");

        Assignment activeAssignment = loadActiveAssignment(user.getId());
        List<Report> myReports = loadReportsByIntern(user.getId());
        List<Report> myPendingReports = filterByStatus(myReports, STATUS_PENDING);

        populatePracticeStatusCard(activeAssignment);
        populatePendingReportsCard(myPendingReports);
        populateTutorEvalCard(myReports);
    }

    private Assignment loadActiveAssignment(int userId) {
        Assignment assignment = null;
        try {
            AssignmentDAO dao = new AssignmentDAO();
            assignment = dao.getActiveByIdIntern(userId);
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Error loading active assignment for intern {0}", userId);
        }
        return assignment;
    }

    private List<Report> loadReportsByIntern(int userId) {
        List<Report> filtered = new ArrayList<>();
        try {
            ReportDAO dao = new ReportDAO();
            List<Report> allReports = dao.getAll();
            for (Report report : allReports) {
                if (report.getIdIntern() == userId) {
                    filtered.add(report);
                }
            }
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Error loading reports for intern {0}", userId);
        }
        return filtered;
    }

    private void populatePracticeStatusCard(Assignment assignment) {
        if (assignment == null) {
            internPracticeStatusBadgeLabel.setText("BUSCANDO");
            internPracticeStatusBadgeLabel.getStyleClass().removeAll("statusActiveLabel");
            internPracticeStatusBadgeLabel.getStyleClass().add("statusPendingLabel");
            internPracticeStatusValueLabel.setText("Buscando Vacante");
            internPracticeStatusSubLabel.setText("Sin proyecto asignado aún");
        } else {
            internPracticeStatusBadgeLabel.setText("ACTIVO");
            internPracticeStatusBadgeLabel.getStyleClass().removeAll("statusPendingLabel");
            internPracticeStatusBadgeLabel.getStyleClass().add("statusActiveLabel");
            internPracticeStatusValueLabel.setText("Práctica Activa");
            internPracticeStatusSubLabel.setText(buildAssignmentDetail(assignment));
        }
    }

    private String buildAssignmentDetail(Assignment assignment) {
        String detail = "Proyecto asignado · ID: " + assignment.getIdProject();
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            Project project = projectDAO.findById(assignment.getIdProject());
            if (project != null) {
                detail = "Proyecto: " + project.getName()
                        + "   ·   EE: " + project.getNrc() + " (" + project.getPeriod() + ")"
                        + "   ·   Estado: " + assignment.getStatus()
                        + "\nSolo puedes estar asignado a un proyecto a la vez.";
            }
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.WARNING, "No se pudo cargar el proyecto asignado {0}: {1}",
                    new Object[]{assignment.getIdProject(), serviceException.getMessage()});
        } catch (ValidationException validationException) {
            LOGGER.log(Level.WARNING, "Validación al cargar el proyecto asignado: {0}",
                    validationException.getMessage());
        }
        return detail;
    }

    private void populatePendingReportsCard(List<Report> pendingReports) {
        int count = pendingReports.size();
        String countText = count == 1 ? "1 reporte" : count + " reportes";
        internPendingReportsCountLabel.setText(countText);

        if (count == 0) {
            internReportsBadgeLabel.setText("AL DÍA");
            internReportsBadgeLabel.getStyleClass().removeAll("statusPendingLabel");
            internReportsBadgeLabel.getStyleClass().add("statusActiveLabel");
            internReportDetailLabel.setText("Sin reportes pendientes por el momento");
        } else {
            internReportsBadgeLabel.setText("PENDIENTE");
            String pendingReportsText = count + " reporte(s) en espera de revisión";
            internReportDetailLabel.setText(pendingReportsText);
        }
    }

    private void populateTutorEvalCard(List<Report> myReports) {
        if (myReports.isEmpty()) {
            internTutorEvalLabel.setText("—");
            internTutorEvalDetailLabel.setText("Disponible al tener reportes enviados");
        } else {
            String sentReportsText = myReports.size() + " enviados";
            internTutorEvalLabel.setText(sentReportsText);
            internTutorEvalDetailLabel.setText("En espera de evaluación del tutor");
        }
    }

    private void loadProfesorDashboard(User user) {
        showSection(profesorContentVBox, "Revisa los reportes de tus practicantes y registra tus evaluaciones.");

        List<Report> allPendingReports = loadAllPendingReports();
        List<Report> myPendingReports = filterByProfessor(allPendingReports, user.getId());
        List<Report> allReports = loadAllReports();
        List<Report> myTotalReports = filterByProfessor(allReports, user.getId());
        populateProfesorCards(myPendingReports, myTotalReports);
        populateProfesorEducationalExperiences(user.getId());
    }

    private void populateProfesorEducationalExperiences(int professorId) {
        try {
            EducationalExperienceDAO educationalExperienceDAO = new EducationalExperienceDAO();
            List<EducationalExperience> experiences =
                    educationalExperienceDAO.findByProfessor(professorId);

            int currentYear = LocalDate.now().getYear();
            List<String> currentExperiences = new ArrayList<>();
            for (EducationalExperience experience : experiences) {
                boolean isHistorical = isFromPreviousPeriod(experience.getPeriod(), currentYear);
                if (!isHistorical) {
                    currentExperiences.add(experience.getNrc() + " - " + experience.getName()
                            + " (" + experience.getPeriod() + ")");
                }
            }

            profEducationalExperiencesCountLabel.setText(String.valueOf(currentExperiences.size()));
            String detail = "No tienes experiencias educativas en el período actual.";
            if (!currentExperiences.isEmpty()) {
                detail = String.join("\n", currentExperiences);
            }
            profEducationalExperiencesDetailLabel.setText(detail);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar experiencias educativas del profesor {0}: {1}",
                    new Object[]{professorId, serviceException.getMessage()});
        } catch (ValidationException validationException) {
            LOGGER.log(Level.WARNING, "Validación al cargar experiencias educativas: {0}",
                    validationException.getMessage());
        }
    }

    private boolean isFromPreviousPeriod(String period, int currentYear) {
        int periodYear = 0;
        if (period != null) {
            Matcher matcher = YEAR_PATTERN.matcher(period);
            while (matcher.find()) {
                periodYear = Integer.parseInt(matcher.group(1));
            }
        }
        return periodYear > 0 && periodYear < currentYear;
    }

    private List<Report> loadAllPendingReports() {
        List<Report> result = new ArrayList<>();
        try {
            ReportDAO reportDAO = new ReportDAO();
            result = reportDAO.getByStatusPending();
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Error loading pending reports");
        }
        return result;
    }

    private List<Report> loadAllReports() {
        List<Report> result = new ArrayList<>();
        try {
            ReportDAO reportDao = new ReportDAO();
            result = reportDao.getAll();
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Error loading all reports");
        }
        return result;
    }

    private List<Report> filterByProfessor(List<Report> reports, int professorId) {
        List<Report> filtered = new ArrayList<>();
        for (Report report : reports) {
            if (report.getIdProfessor() == professorId) {
                filtered.add(report);
            }
        }
        return filtered;
    }

    private Set<Integer> extractUniqueInterns(List<Report> reports) {
        Set<Integer> ids = new HashSet<>();
        for (Report report : reports) {
            ids.add(report.getIdIntern());
        }
        return ids;
    }

    private int countEvaluationsForReports(List<Report> reports) {
        int count = 0;
        for (Report report : reports) {
            boolean isEvaluated = report.getGrade() != null;
            if (isEvaluated) {
                count++;
            }
        }
        return count;
    }

    private void populateProfesorCards(List<Report> pending, List<Report> total) {
        int pendingCount = pending.size();
        String pendingCountText = pendingCount + " reportes";
        if (pendingCount == 1) {
            pendingCountText = "1 reporte";
        }
        profPendingCountLabel.setText(pendingCountText);
        String pendingDetailText = pendingCount + " esperando revisión";
        if (pendingCount == 0) {
            pendingDetailText = "Sin reportes pendientes";
        }
        profPendingDetailLabel.setText(pendingDetailText);
        profTotalReportsLabel.setText(String.valueOf(total.size()));
        int internsCount = extractUniqueInterns(total).size();
        profInternsCountLabel.setText(String.valueOf(internsCount));
        int evalsDone = countEvaluationsForReports(total);
        profEvalDoneLabel.setText(String.valueOf(evalsDone));
    }

    private void loadCoordinadorDashboard() {
        showSection(coordinadorContentVBox,
                "Resumen del estado actual de las prácticas profesionales.");

        coordProjectsCountLabel.setText(String.valueOf(countProjects()));
        coordInternsCountLabel.setText(String.valueOf(countActiveInterns()));
        coordPendingReportsLabel.setText(String.valueOf(countPendingReports()));
        coordSlotsCountLabel.setText(String.valueOf(countAvailableSlots()));
    }

    private void loadAdministradorDashboard() {
        showSection(adminContentVBox,
                "Vista general de todos los usuarios y recursos del sistema.");

        adminCoordCountLabel.setText(String.valueOf(countActiveCoordinators()));
        adminProfCountLabel.setText(String.valueOf(countActiveProfessors()));
        adminInternsCountLabel.setText(String.valueOf(countActiveInterns()));
        adminProjectsCountLabel.setText(String.valueOf(countProjects()));
    }

    private int countProjects() {
        int count = 0;
        try {
            ProjectDAO projectDao = new ProjectDAO();
            count = projectDao.findAll().size();
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Error counting projects");
        }
        return count;
    }

    private int countActiveInterns() {
        int count = 0;
        try {
            InternDAO internDao = new InternDAO();
            count = internDao.findAllActiveinterns().size();
        } catch (ServiceException | ValidationException e) {
            LOGGER.log(Level.SEVERE, "Error counting active interns");
        }
        return count;
    }

    private int countPendingReports() {
        int count = 0;
        try {
            ReportDAO reportDao = new ReportDAO();
            count = reportDao.getByStatusPending().size();
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Error counting pending reports");
        }
        return count;
    }

    private int countAvailableSlots() {
        int count = 0;
        try {
            ProjectDAO projectDao = new ProjectDAO();
            count = projectDao.findAllAvailable().size();
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Error counting available project slots");
        }
        return count;
    }

    private int countActiveCoordinators() {
        int count = 0;
        try {
            CoordinatorDAO coordinatorDao = new CoordinatorDAO();
            count = coordinatorDao.findActiveCoordinators().size();
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Error counting active coordinators");
        }
        return count;
    }

    private int countActiveProfessors() {
        int count = 0;
        try {
            ProfessorDAO professorDao = new ProfessorDAO();
            count = professorDao.findActiveProfessors().size();
        } catch (ServiceException | ValidationException e) {
            LOGGER.log(Level.SEVERE, "Error counting active professors");
        }
        return count;
    }

    private List<Report> filterByStatus(List<Report> reports, String status) {
        List<Report> filtered = new ArrayList<>();
        for (Report report : reports) {
            if (status.equals(report.getStatus())) {
                filtered.add(report);
            }
        }
        return filtered;
    }

    private void showSection(VBox section, String subtitle) {
        section.setVisible(true);
        section.setManaged(true);
        welcomeSubtitleLabel.setText(subtitle);
    }
}