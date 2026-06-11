package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ActivityDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Activity;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import GUI.Utils.RestrictedTextArea;
import GUI.Utils.RestrictedTextField;
import javafx.scene.layout.AnchorPane;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ValidationUtils.limitTextArea;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ViewsUtils.openWelcomePage;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AddActivityController {

    private static final Logger LOGGER = Logger.getLogger(AddActivityController.class.getName());

    public AnchorPane anchorPane;

    @FXML
    private ComboBox<Project> projectComboBox;

    @FXML
    private RestrictedTextField nameTextField;

    @FXML
    private RestrictedTextArea descriptionTextArea;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private void initialize() {
        setTypeAndLength(nameTextField, "Name");
        limitTextArea(descriptionTextArea, 255);
        loadProjects();
    }

    @FXML
    public void saveActivity(ActionEvent actionEvent) {
        boolean isInvalid = isInputInvalid();
        boolean areDatesWrong = areDatesInvalid();
        boolean areDatesOutsideProject = areDatesOutsideProjectRange();

        if (isInvalid) {
            showAlert("Campos incompletos",
                    "Complete todos los campos requeridos antes de guardar.",
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
            saveProcess();
        }
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmar cancelación",
                "¿Desea salir? Los datos ingresados no se guardarán.",
                Alert.AlertType.CONFIRMATION);
        boolean isConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isConfirmed) {
            clearForm();
            openWelcomePage(anchorPane);
        }
    }

    public void setProject(Project project) {
        for (Project projectItem : projectComboBox.getItems()) {
            if (projectItem.getIdProject() == project.getIdProject()) {
                projectComboBox.getSelectionModel().select(projectItem);
            }
        }
    }

    private void saveProcess() {
        try {
            Activity activity = buildActivity();
            ActivityDAO activityDAO = new ActivityDAO();
            int generatedId = activityDAO.save(activity);

            if (generatedId > 0) {
                LOGGER.log(Level.INFO,
                        "Usuario {0} registró la actividad {1} en el proyecto {2}",
                        new Object[]{SessionManager.getInstance().getUser().getId(),
                                generatedId, activity.getIdProject()});
                showAlert("Actividad registrada",
                        "La actividad fue registrada exitosamente.",
                        Alert.AlertType.INFORMATION);
                clearForm();
            } else {
                showAlert("Error",
                        "No se pudo registrar la actividad. Intente nuevamente.",
                        Alert.AlertType.ERROR);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al guardar actividad: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudo registrar la actividad. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private Activity buildActivity() {
        Activity activity = new Activity();
        activity.setIdProject(projectComboBox.getValue().getIdProject());
        activity.setName(nameTextField.getText().trim());
        activity.setDescription(descriptionTextArea.getText().trim());
        activity.setStartDate(startDatePicker.getValue());
        activity.setEndDate(endDatePicker.getValue());
        activity.setCreationDate(LocalDate.now());
        activity.setStatus("Activa");
        return activity;
    }

    private void loadProjects() {
        try {
            int professorId = SessionManager.getInstance().getUser().getId();
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projects = projectDAO.findByProfessorAvailable(professorId);
            projectComboBox.getItems().setAll(projects);

        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar proyectos del profesor: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los proyectos. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean isInputInvalid() {
        boolean isProjectMissing = projectComboBox.getValue() == null;
        boolean isNameEmpty = nameTextField.getText().isBlank();
        boolean hasInvalidInput = isProjectMissing || isNameEmpty;
        return hasInvalidInput;
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

    private void clearForm() {
        nameTextField.clear();
        descriptionTextArea.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

}
