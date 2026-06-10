package GUI.Controller;

import Logic.DAO.ApplicationDAO;
import GUI.Utils.AuditLog;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.InitialFormatDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ProjectApplicationDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Application;
import Logic.DTOs.Assignment;
import Logic.DTOs.InitialFormat;
import Logic.DTOs.Project;
import Logic.DTOs.ProjectApplication;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;

public class ViewInterProjectSelection {
    private static final String STATUS_ACCEPTED = "Aceptada";
    private static final String STATUS_PENDING = "Pendiente";


    private static final Logger LOGGER = Logger.getLogger(ViewInterProjectSelection.class.getName());
    private static final int INITIAL_DOCUMENTS_COUNT = 4;
    private static final List<String> INITIAL_DOCUMENT_TYPES = List.of(
            "Carta de Asignación", "Horario", "Certificado de Seguro", "Cronograma de Actividades");

    @FXML
    private AnchorPane anchorPane;

    @FXML
    public TableView<Project> projectsTableView;

    @FXML
    private TableColumn<Project, String> projetcNameColumn;

    @FXML
    private TableColumn<Project, String> organizationColumn;

    @FXML
    private TableColumn<Project, String> placesColumn;

    @FXML
    private TableColumn<Project, String> datesColumn;

    @FXML
    private TableColumn<Project, String> typeColumn;

    @FXML
    private Label internNameLabel;

    private static final String LABEL_SELECTED = "✓ Seleccionado";
    private static final String LABEL_NOT_SELECTED = "—";

    private User user;
    private int internId;
    private int applicationId;
    private final Set<Integer> internSelectedIds = new HashSet<>();

    @FXML
    private void initialize() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        String fullName = user.getFirstName() + " " + user.getLastName() + " " + user.getSecondLastName();
        internNameLabel.setText(fullName);
        loadProjectList();
    }

    @FXML
    public void assignProject(ActionEvent actionEvent) {
        Project selectedProject = projectsTableView.getSelectionModel().getSelectedItem();
        boolean isSelectionMissing = selectedProject == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un proyecto de la tabla para asignar.",
                    Alert.AlertType.WARNING);
        } else {
            handleAssignAction(selectedProject);
        }
    }

    private void loadProjectList() {
        try {
            ApplicationDAO applicationDAO = new ApplicationDAO();
            Application application = applicationDAO.findActiveApplicationByIntern(user.getId());

            boolean hasNoPendingApplication = application == null;
            if (hasNoPendingApplication) {
                showAlert("Sin solicitud pendiente",
                        "El practicante no cuenta con una solicitud pendiente.",
                        Alert.AlertType.WARNING);
            } else {
                applicationId = application.getIdApplication();
                internId = application.getIdIntern();

                ProjectApplicationDAO projectApplicationDAO = new ProjectApplicationDAO();
                List<ProjectApplication> projectApplications =
                        projectApplicationDAO.findByApplication(applicationId);

                for (ProjectApplication projectApplication : projectApplications) {
                    internSelectedIds.add(projectApplication.getIdProject());
                }

                ProjectDAO projectDAO = new ProjectDAO();
                List<Project> projectList = new ArrayList<>();

                for (ProjectApplication projectApplication : projectApplications) {
                    Project project = projectDAO.findById(projectApplication.getIdProject());
                    boolean isAvailable = project != null && project.getAvaliablePlaces() > 0;
                    if (isAvailable) {
                        project.setPreferenceLabel(LABEL_SELECTED);
                        projectList.add(project);
                    }
                }

                List<Project> allAvailable = projectDAO.findAllAvailable();
                for (Project project : allAvailable) {
                    boolean isAlreadyIncluded = internSelectedIds.contains(project.getIdProject());
                    if (!isAlreadyIncluded) {
                        project.setPreferenceLabel(LABEL_NOT_SELECTED);
                        projectList.add(project);
                    }
                }

                projectsTableView.getItems().setAll(projectList);
            }

        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar proyectos para asignación: {0}",
                    serviceException.getMessage());
            showAlert("Error", "No se pudieron cargar los proyectos. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleAssignAction(Project project) {
        boolean isOriginalSelection = internSelectedIds.contains(project.getIdProject());
        if (isOriginalSelection) {
            confirmAndAssign(project, null);
        } else {
            requestJustificationAndAssign(project);
        }
    }

    private void requestJustificationAndAssign(Project project) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Justificación requerida");
        dialog.setHeaderText("El practicante no seleccionó este proyecto originalmente.\n"
                + "Proyecto: " + project.getName());
        dialog.setContentText("Motivo de la asignación:");

        Optional<String> justificationResult = dialog.showAndWait();
        if (justificationResult.isPresent()) {
            String justification = justificationResult.get();
            boolean isJustificationBlank = justification.isBlank();
            if (isJustificationBlank) {
                showAlert("Campo requerido",
                        "Debe ingresar el motivo de la asignación para continuar.",
                        Alert.AlertType.WARNING);
            } else {
                confirmAndAssign(project, justification);
            }
        }
    }

    private void confirmAndAssign(Project project, String justification) {
        String confirmationMessage = "¿Seguro que desea asignar el proyecto \""
                + project.getName() + "\" a este practicante?";
        Optional<ButtonType> response = showAlertAndWait("Confirmación", confirmationMessage, Alert.AlertType.CONFIRMATION);

        boolean isUserConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isUserConfirmed) {
            assignProjectProcess(project, justification);
        }
    }

    private void assignProjectProcess(Project project, String justification) {
        boolean hasNoCapacity = project.getAvaliablePlaces() <= 0;
        if (hasNoCapacity) {
            showAlert("Sin cupo disponible",
                    "El proyecto seleccionado ya no cuenta con cupos disponibles.",
                    Alert.AlertType.WARNING);
        } else {
            try {
                Assignment assignment = new Assignment();
                assignment.setIdApplication(applicationId);
                assignment.setIdProject(project.getIdProject());
                assignment.setIdIntern(internId);
                assignment.setAssignmentDate(LocalDate.now(ZoneId.of("America/Mexico_City")));
                assignment.setAssignmentReason(justification);

                AssignmentDAO assignmentDAO = new AssignmentDAO();
                assignmentDAO.save(assignment);

                ProjectDAO projectDAO = new ProjectDAO();
                boolean cupoDecremented = projectDAO.decrementAvailableSlot(project.getIdProject());
                boolean cupoNotDecremented = !cupoDecremented;
                if (cupoNotDecremented) {
                    LOGGER.log(Level.WARNING,
                            "No se pudo decrementar cupo del proyecto {0}: sin cupo disponible",
                            project.getIdProject());
                }

                ApplicationDAO applicationDAO = new ApplicationDAO();
                applicationDAO.updateStatus(applicationId, STATUS_ACCEPTED);

                createInitialDocuments(project.getIdProject());
                createOrReactivatePractice(project);

                AuditLog.record("asignó un proyecto a un practicante");
                showAlert("Éxito", "El proyecto ha sido asignado correctamente.",
                        Alert.AlertType.INFORMATION);
                navigateBackToAssignProject();

            } catch (ServiceException serviceException) {
                LOGGER.log(Level.SEVERE, "Error al asignar proyecto: {0}",
                        serviceException.getMessage());
                showAlert("Error", "No se pudo procesar la asignación. Intente más tarde.",
                        Alert.AlertType.ERROR);
            } catch (ValidationException validationException) {
                showAlert("Error de validación", validationException.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    private void navigateBackToAssignProject() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/View/GUIAssignProject.fxml"));
            Parent vista = loader.load();
            AnchorPane parentPane = (AnchorPane) anchorPane.getParent();
            parentPane.getChildren().setAll(vista);
        } catch (IOException ioException) {
            showAlert("Error", "No se pudo regresar a la lista de asignación.",
                    Alert.AlertType.ERROR);
        }
    }

    private void createOrReactivatePractice(Project project) {
        String nrc = project.getNrc();
        boolean hasNrc = nrc != null && !nrc.isBlank();
        if (!hasNrc) {
            LOGGER.log(Level.WARNING,
                    "Proyecto {0} sin NRC: no se puede crear práctica.", project.getIdProject());
        } else {

            try {
                PracticeDAO practiceDAO = new PracticeDAO();
                practiceDAO.reactivateOrCreate(user.getId(), nrc, LocalDate.now(ZoneId.of("America/Mexico_City")));
            } catch (ServiceException serviceException) {
                LOGGER.log(Level.SEVERE, "Error al gestionar práctica para practicante {0}: {1}",
                        new Object[]{user.getId(), serviceException.getMessage()});
            } catch (ValidationException validationException) {
                LOGGER.log(Level.WARNING, "Validación al gestionar práctica: {0}",
                        validationException.getMessage());
            }
        }
    }

    private void createInitialDocuments(int idProject) throws ServiceException, ValidationException {
        for (int i = 0; i < INITIAL_DOCUMENTS_COUNT; i++) {
            InitialFormatDAO initialFormatDAO = new InitialFormatDAO();
            InitialFormat initialFormat = new InitialFormat();
            initialFormat.setFormatType(INITIAL_DOCUMENT_TYPES.get(i));
            initialFormat.setIdIntern(user.getId());
            initialFormat.setIdProject(idProject);
            initialFormat.setStatus(STATUS_PENDING);
            initialFormatDAO.save(initialFormat);
        }
    }

}
