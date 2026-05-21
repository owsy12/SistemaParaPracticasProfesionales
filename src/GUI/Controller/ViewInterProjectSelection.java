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
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
    private TableColumn<Project, Void> actionColumn;
    @FXML
    private Label internNameLabel;

    private User user;
    private int internId;
    private int applicationId;
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
        internNameLabel.setText(user.getFirstName() + " " + user.getLastName()
                + " " + user.getSecondLastName());
        loadProjectList();
        configureDataColumns();
        addAssignButtonToRow();
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

            for (Integer id : selectedIds) {
                Project project = projectDAO.findById(id);
                if (project != null) {
                    result.add(project);
                }
            }

            List<Project> allAvailable = projectDAO.findAllAvailable();
            for (Project project : allAvailable) {
                if (!originalProjectIds.contains(project.getIdProyect())) {
                    result.add(project);
                }
            }

            projectsTableView.getItems().setAll(result);

        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar proyectos para asignación: {0}", serviceException.getMessage());
            showAlert("Error", "No se pudieron cargar los proyectos. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void configureDataColumns() {
        projetcNameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getName()));

        organizationColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getOrganizationName()));

        placesColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getAvaliablePlaces())));

        datesColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getStartDate() + " - " + c.getValue().getEndDate()));

        typeColumn.setCellValueFactory(c -> {
            Integer preferenceOrder = preferenceOrderMap.get(c.getValue().getIdProyect());
            String label;
            if (preferenceOrder == null) {
                label = "Disponible";
            } else if (preferenceOrder == 1) {
                label = "Primera opción";
            } else if (preferenceOrder == 2) {
                label = "Segunda opción";
            } else {
                label = "Tercera opción";
            }
            return new SimpleStringProperty(label);
        });
    }

    private void addAssignButtonToRow() {
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button button = new Button("Asignar");

            {
                button.setOnAction(event -> {
                    Project project = getTableView().getItems().get(getIndex());
                    handleAssignAction(project);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        });
    }

    private void handleAssignAction(Project project) {
        if (originalProjectIds.contains(project.getIdProyect())) {
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

        dialog.showAndWait().ifPresent(justification -> {
            if (justification.isBlank()) {
                showAlert("Campo requerido",
                        "Debe ingresar el motivo de la asignación para continuar.",
                        Alert.AlertType.WARNING);
            } else {
                confirmAndAssign(project, justification);
            }
        });
    }

    private void confirmAndAssign(Project project, String justification) {
        showAlertAndWait("Confirmación",
                "¿Seguro que desea asignar el proyecto \"" + project.getName()
                        + "\" a este practicante?",
                Alert.AlertType.CONFIRMATION)
                .ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        assignProjectProcess(project, justification);
                    }
                });
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
