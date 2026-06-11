package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ActivityDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Activity;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import GUI.Utils.RestrictedTextField;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;

public class ManageActivitiesController implements ChangeListener<Activity> {
    private static final String STATUS_AVAILABLE = "Disponible";


    private static final Logger LOGGER = Logger.getLogger(ManageActivitiesController.class.getName());

    @FXML
    private ComboBox<Project> projectComboBox;

    @FXML
    private TableView<Activity> activitiesTableView;

    @FXML
    private TableColumn<Activity, String> nameColumn;

    @FXML
    private TableColumn<Activity, String> descriptionColumn;

    @FXML
    private TableColumn<Activity, String> statusColumn;

    @FXML
    private RestrictedTextField nameTextField;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label statusLabel;

    private Activity selectedActivity;

    @FXML
    private void initialize() {
        setTypeAndLength(nameTextField, "Text");
        configureListeners();
        loadProjects();
    }

    @FXML
    public void loadActivities(ActionEvent actionEvent) {
        Project selectedProject = projectComboBox.getValue();
        if (selectedProject == null) {
            showAlert("Sin selección", "Seleccione un proyecto.", Alert.AlertType.WARNING);
        } else {
            refreshActivities(selectedProject.getIdProject());
        }
    }

    @FXML
    public void updateActivity(ActionEvent actionEvent) {
        boolean isActivityMissing = selectedActivity == null;
        boolean isProjectNotAvailable = projectComboBox.getValue() == null
                || !STATUS_AVAILABLE.equals(projectComboBox.getValue().getStatus());
        boolean isExpired = selectedActivity != null && isActivityExpired();
        boolean isNameEmpty = nameTextField.getText().isBlank();
        boolean areDatesWrong = areDatesInvalid();
        boolean areDatesOutsideProject = areDatesOutsideProjectRange();

        if (isActivityMissing) {
            showAlert("Sin selección",
                    "Seleccione una actividad de la tabla para editar.",
                    Alert.AlertType.WARNING);
        } else if (isProjectNotAvailable) {
            showAlert("Proyecto no disponible",
                    "Solo puede modificar actividades de proyectos en estado Disponible.",
                    Alert.AlertType.WARNING);
        } else if (isExpired) {
            showAlert("Actividad vencida",
                    "Esta actividad ya venció. Para modificarla debe otorgar una prórroga.",
                    Alert.AlertType.WARNING);
        } else if (isNameEmpty) {
            showAlert("Campo requerido",
                    "El nombre de la actividad no puede estar vacío.",
                    Alert.AlertType.WARNING);
        } else if (areDatesWrong) {
            showAlert("Fechas inválidas",
                    "La fecha de entrega debe ser posterior a la fecha de inicio.",
                    Alert.AlertType.WARNING);
        } else if (areDatesOutsideProject) {
            showAlert("Fechas fuera del rango del proyecto",
                    "Las fechas de la actividad deben estar dentro del período del proyecto.",
                    Alert.AlertType.WARNING);
        } else {
            updateProcess();
        }
    }

    @FXML
    public void deleteActivity(ActionEvent actionEvent) {
        boolean isProjectNotAvailable = projectComboBox.getValue() == null
                || !STATUS_AVAILABLE.equals(projectComboBox.getValue().getStatus());
        if (selectedActivity == null) {
            showAlert("Sin selección",
                    "Seleccione una actividad para eliminar.",
                    Alert.AlertType.WARNING);
        } else if (isProjectNotAvailable) {
            showAlert("Proyecto no disponible",
                    "Solo puede eliminar actividades de proyectos en estado Disponible.",
                    Alert.AlertType.WARNING);
        } else {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar la actividad \"" + selectedActivity.getName() + "\"?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> confirmationResult = confirmation.showAndWait();

            boolean isDeletionConfirmed = confirmationResult.isPresent()
                    && confirmationResult.get() == ButtonType.YES;
            if (isDeletionConfirmed) {
                deleteProcess();
            }
        }
    }

    @FXML
    public void grantExtension(ActionEvent actionEvent) {
        boolean isActivityMissing = selectedActivity == null;
        boolean isProjectMissing = projectComboBox.getValue() == null;
        boolean isDateMissing = selectedActivity != null && selectedActivity.getEndDate() == null;

        if (isActivityMissing) {
            showAlert("Sin selección",
                    "Seleccione una actividad para otorgar la prórroga.",
                    Alert.AlertType.WARNING);
        } else if (isProjectMissing) {
            showAlert("Sin proyecto",
                    "Seleccione un proyecto primero.",
                    Alert.AlertType.WARNING);
        } else if (isDateMissing) {
            showAlert("Fecha inválida",
                    "La actividad seleccionada no tiene fecha de fin registrada.",
                    Alert.AlertType.WARNING);
        } else {
            processExtensionDialog();
        }
    }

    @FXML
    public void clearSelection(ActionEvent actionEvent) {
        clearForm();
    }

    public void setProject(Project project) {
        for (Project projectItem : projectComboBox.getItems()) {
            if (projectItem.getIdProject() == project.getIdProject()) {
                projectComboBox.getSelectionModel().select(projectItem);
                refreshActivities(projectItem.getIdProject());
            }
        }
    }

    private void configureListeners() {
        activitiesTableView.getSelectionModel().selectedItemProperty().addListener(this);
    }

    private void processExtensionDialog() {
        Optional<LocalDate> dialogResult = showExtensionDialog();
        boolean dateProvided = dialogResult.isPresent();
        if (dateProvided) {
            saveExtensionProcess(dialogResult.get());
        }
    }

    private Optional<LocalDate> showExtensionDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Otorgar Prórroga");
        dialog.setHeaderText("Actividad: " + selectedActivity.getName());

        ButtonType confirmType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker newDatePicker = new DatePicker();
        grid.add(new Label("Nueva fecha límite:"), 0, 0);
        grid.add(newDatePicker, 1, 0);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> response = dialog.showAndWait();
        Optional<LocalDate> result = Optional.empty();
        boolean isConfirmed = response.isPresent() && response.get() == confirmType;
        if (isConfirmed) {
            result = Optional.ofNullable(newDatePicker.getValue());
        }
        return result;
    }

    private void saveExtensionProcess(LocalDate newEndDate) {
        boolean isNewDateNull = newEndDate == null;

        Project project = projectComboBox.getValue();
        boolean hasProjectEnd = project != null && project.getEndDate() != null;
        boolean isNewDateBeyondProject = hasProjectEnd && newEndDate != null
                && newEndDate.isAfter(project.getEndDate());
        boolean isNewDateNotFuture = newEndDate != null
                && !newEndDate.isAfter(LocalDate.now());

        if (isNewDateNull) {
            showAlert("Fecha requerida",
                    "Seleccione la nueva fecha límite para la prórroga.",
                    Alert.AlertType.WARNING);
        } else if (isNewDateNotFuture) {
            showAlert("Fecha inválida",
                    "La nueva fecha límite debe ser una fecha futura.",
                    Alert.AlertType.WARNING);
        } else if (isNewDateBeyondProject) {
            showAlert("Fecha fuera del rango del proyecto",
                    "La nueva fecha límite no puede superar la fecha de fin del proyecto.",
                    Alert.AlertType.WARNING);
        } else {
            executeExtension(newEndDate);
        }
    }

    private void executeExtension(LocalDate newEndDate) {
        try {
            selectedActivity.setEndDate(newEndDate);
            ActivityDAO activityDAO = new ActivityDAO();
            boolean updated = activityDAO.update(selectedActivity);

            if (updated) {
                LOGGER.log(Level.INFO,
                        "Usuario {0} otorgó prórroga a la actividad {1} con nueva fecha fin {2}",
                        new Object[]{SessionManager.getInstance().getUser().getId(),
                                selectedActivity.getIdActivity(), String.valueOf(newEndDate)});
                showAlert("Prórroga otorgada",
                        "La nueva fecha límite de la actividad fue registrada correctamente.",
                        Alert.AlertType.INFORMATION);
                refreshActivities(projectComboBox.getValue().getIdProject());
                clearForm();
            } else {
                showAlert("Error",
                        "No se pudo otorgar la prórroga. Intente nuevamente.",
                        Alert.AlertType.ERROR);
            }
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al otorgar prórroga para actividad {0}: {1}",
                    new Object[]{selectedActivity.getIdActivity(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo otorgar la prórroga. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void updateProcess() {
        try {
            selectedActivity.setName(nameTextField.getText().trim());
            selectedActivity.setDescription(descriptionTextArea.getText().trim());
            selectedActivity.setStartDate(startDatePicker.getValue());
            selectedActivity.setEndDate(endDatePicker.getValue());

            ActivityDAO activityDAO = new ActivityDAO();

            if (activityDAO.update(selectedActivity)) {
                LOGGER.log(Level.INFO,
                        "Usuario {0} actualizó la actividad {1}",
                        new Object[]{SessionManager.getInstance().getUser().getId(),
                                selectedActivity.getIdActivity()});
                showAlert("Actividad actualizada",
                        "La actividad fue actualizada correctamente.",
                        Alert.AlertType.INFORMATION);
                refreshActivities(projectComboBox.getValue().getIdProject());
                clearForm();
            } else {
                showAlert("Error",
                        "No se pudo actualizar la actividad.",
                        Alert.AlertType.ERROR);
            }
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar actividad {0}: {1}",
                    new Object[]{selectedActivity.getIdActivity(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo actualizar la actividad. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void deleteProcess() {
        try {
            ActivityDAO activityDao = new ActivityDAO();

            if (activityDao.delete(selectedActivity.getIdActivity())) {
                LOGGER.log(Level.INFO,
                        "Usuario {0} eliminó la actividad {1}",
                        new Object[]{SessionManager.getInstance().getUser().getId(),
                                selectedActivity.getIdActivity()});
                showAlert("Actividad eliminada",
                        "La actividad fue eliminada correctamente.",
                        Alert.AlertType.INFORMATION);
                refreshActivities(projectComboBox.getValue().getIdProject());
                clearForm();
            } else {
                showAlert("Error",
                        "No se pudo eliminar la actividad.",
                        Alert.AlertType.ERROR);
            }
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar actividad {0}: {1}",
                    new Object[]{selectedActivity.getIdActivity(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo eliminar la actividad. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean isActivityExpired() {
        LocalDate endDate = selectedActivity.getEndDate();
        boolean hasDeadline = endDate != null;
        boolean isExpired = hasDeadline && endDate.isBefore(LocalDate.now());
        return isExpired;
    }

    private boolean areDatesInvalid() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        boolean bothProvided = startDate != null && endDate != null;
        boolean invalid = bothProvided && !endDate.isAfter(startDate);
        return invalid;
    }

    private boolean areDatesOutsideProjectRange() {
        Project project = projectComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        boolean hasProjectStart = project != null && project.getStartDate() != null;
        boolean hasProjectEnd = project != null && project.getEndDate() != null;

        boolean startBeforeProject = hasProjectStart && startDate != null
                && startDate.isBefore(project.getStartDate());
        boolean endAfterProject = hasProjectEnd && endDate != null
                && endDate.isAfter(project.getEndDate());

        boolean isOutOfRange = startBeforeProject || endAfterProject;
        return isOutOfRange;
    }

    private void loadProjects() {
        try {
            int professorId = SessionManager.getInstance().getUser().getId();
            ProjectDAO projectDao = new ProjectDAO();
            List<Project> projectList = projectDao.findByProfessorAvailable(professorId);
            projectComboBox.getItems().setAll(projectList);

        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar proyectos del profesor: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los proyectos.", Alert.AlertType.ERROR);
        }
    }

    private void refreshActivities(int idProject) {
        try {
            ActivityDAO activityDao = new ActivityDAO();
            List<Activity> activities = activityDao.findByProject(idProject);
            activitiesTableView.setItems(FXCollections.observableArrayList(activities));
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar actividades del proyecto {0}: {1}",
                    new Object[]{idProject, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar las actividades.", Alert.AlertType.ERROR);
        }
    }

    private void populateForm(Activity activity) {
        nameTextField.setText(activity.getName());

        String description = "";
        if (activity.getDescription() != null) {
            description = activity.getDescription();
        }
        descriptionTextArea.setText(description);

        startDatePicker.setValue(activity.getStartDate());
        endDatePicker.setValue(activity.getEndDate());
        String statusText = "Estado: " + activity.getStatus();
        statusLabel.setText(statusText);
    }

    private void clearForm() {
        selectedActivity = null;
        nameTextField.clear();
        descriptionTextArea.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        statusLabel.setText("");
        activitiesTableView.getSelectionModel().clearSelection();
    }

    @Override
    public void changed(ObservableValue<? extends Activity> observable, Activity oldValue, Activity newValue) {
        if (newValue != null) {
            selectedActivity = newValue;
            populateForm(newValue);
        }
    }

}
