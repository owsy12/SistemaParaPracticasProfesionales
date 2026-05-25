package GUI.Controller;

import Logic.DAO.ApplicationDAO;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.InitialFormatDAO;
import Logic.DAO.ProjectApplicationDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Application;
import Logic.DTOs.Assignment;
import Logic.DTOs.InitialFormat;
import Logic.DTOs.Project;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;

public class ViewInterProjectSelection {

    private static final Logger LOGGER = Logger.getLogger(ViewInterProjectSelection.class.getName());
    private static final int INITIAL_DOCUMENTS_COUNT = 4;
    private static final List<String> INITIAL_DOCUMENT_TYPES = List.of(
            "Carta de Asignación", "Horario", "Certificado de Seguro", "Cronograma de Actividades");

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

    private User user;
    private int internId;
    private int applicationId;
    private Project selectedProject;
    private final Set<Integer> originalProjectIds = new HashSet<>();
    private final Map<Integer, Integer> preferenceOrderMap = new HashMap<>();

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
        configureListeners();
        loadProjectList();
    }

    @FXML
    public void assignProject(ActionEvent actionEvent) {
        boolean isSelectionMissing = selectedProject == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un proyecto de la tabla para asignar.",
                    Alert.AlertType.WARNING);
            return;
        }
        handleAssignAction(selectedProject);
    }

    private void configureListeners() {
        projectsTableView.getSelectionModel().selectedItemProperty()
                .addListener(new ProjectSelectionListener());
    }

    private final class ProjectSelectionListener implements ChangeListener<Project> {
        @Override
        public void changed(ObservableValue<? extends Project> observable,
                            Project oldValue, Project newValue) {
            selectedProject = newValue;
        }
    }

    private void loadProjectList() {
        try {
            ApplicationDAO applicationDAO = new ApplicationDAO();
            Application application = applicationDAO.findByIntern(user.getId());
            applicationId = application.getIdApplication();
            internId = application.getIdIntern();

            ProjectApplicationDAO projectApplicationDAO = new ProjectApplicationDAO();
            List<Integer> selectedIds = projectApplicationDAO.findProjectIdsByIntern(user.getId());
            originalProjectIds.addAll(selectedIds);

            for (int index = 0; index < selectedIds.size(); index++) {
                preferenceOrderMap.put(selectedIds.get(index), index + 1);
            }

            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> result = new ArrayList<>();

            for (Integer projectId : selectedIds) {
                Project project = projectDAO.findById(projectId);
                if (project != null) {
                    result.add(project);
                }
            }

            List<Project> allAvailable = projectDAO.findAllAvailable();
            for (Project project : allAvailable) {
                boolean isAlreadyIncluded = originalProjectIds.contains(project.getIdProyect());
                if (!isAlreadyIncluded) {
                    result.add(project);
                }
            }

            projectsTableView.getItems().setAll(result);

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
        boolean isOriginalSelection = originalProjectIds.contains(project.getIdProyect());
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
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmación", confirmationMessage, Alert.AlertType.CONFIRMATION);

        boolean isUserConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isUserConfirmed) {
            assignProjectProcess(project, justification);
        }
    }

    private void assignProjectProcess(Project project, String justification) {
        try {
            Assignment assignment = new Assignment();
            assignment.setIdApplication(applicationId);
            assignment.setIdProyect(project.getIdProyect());
            assignment.setIdIntern(internId);
            assignment.setAssignmentDate(LocalDate.now(ZoneId.of("America/Mexico_City")));
            assignment.setRazonAsignacion(justification);

            AssignmentDAO assignmentDAO = new AssignmentDAO();
            assignmentDAO.save(assignment);

            ApplicationDAO applicationDAO = new ApplicationDAO();
            applicationDAO.updateStatus(applicationId, "Aceptada");

            createInitialDocuments(project.getIdProyect());

            showAlert("Éxito", "El proyecto ha sido asignado correctamente.",
                    Alert.AlertType.INFORMATION);
            projectsTableView.getItems().clear();
            selectedProject = null;

        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al asignar proyecto: {0}", serviceException.getMessage());
            showAlert("Error", "No se pudo procesar la asignación. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void createInitialDocuments(int idProject) throws ServiceException, ValidationException {
        for (int i = 0; i < INITIAL_DOCUMENTS_COUNT; i++) {
            InitialFormatDAO initialFormatDAO = new InitialFormatDAO();
            InitialFormat initialFormat = new InitialFormat();
            initialFormat.setFormatType(INITIAL_DOCUMENT_TYPES.get(i));
            initialFormat.setIdIntern(user.getId());
            initialFormat.setIdProject(idProject);
            initialFormat.setStatus("Pendiente");
            initialFormatDAO.save(initialFormat);
        }
    }

}
