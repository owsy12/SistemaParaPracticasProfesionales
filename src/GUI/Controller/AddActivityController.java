package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ActivityDAO;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Activity;
import Logic.DTOs.Assignment;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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
    private Label projectNameLabel;

    private Project assignedProject;

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
        boolean isConfirmed = false;
        if (response.isPresent()) {
            if (response.get() == ButtonType.OK) {
                isConfirmed = true;
            }
        }
        if (isConfirmed) {
            clearForm();
            openWelcomePage(anchorPane);
        }
    }

    private void saveProcess() {
        try {
            Activity activity = buildActivity();
            ActivityDAO activityDAO = new ActivityDAO();
            int generatedId = activityDAO.save(activity);

            if (generatedId > 0) {
                LOGGER.log(Level.INFO,
                        "User {0} registered activity {1} in project {2}",
                        new Object[]{SessionManager.getInstance().getUser().getIdUser(),
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
            LOGGER.log(Level.SEVERE, "Error saving activity: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudo registrar la actividad. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private Activity buildActivity() {
        Activity activity = new Activity();
        activity.setIdProject(assignedProject.getIdProject());
        activity.setIdIntern(SessionManager.getInstance().getUser().getIdUser());
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
            int internId = SessionManager.getInstance().getUser().getIdUser();
            AssignmentDAO assignmentDAO = new AssignmentDAO();
            Assignment assignment = assignmentDAO.getActiveByIdIntern(internId);
            boolean hasAssignment = assignment != null;
            PracticeDAO practiceDAO = new PracticeDAO();
            boolean isPracticeConcluded = false;
            if (hasAssignment) {
                if (practiceDAO.hasConcludedPractice(internId)) {
                    isPracticeConcluded = true;
                }
            }
            if (!hasAssignment) {
                showAlert("Sin proyecto asignado",
                        "No tiene un proyecto asignado. No puede registrar actividades.",
                        Alert.AlertType.WARNING);
                openWelcomePage(anchorPane);
            } else if (isPracticeConcluded) {
                showAlert("Práctica concluida",
                        "Tu práctica ya fue concluida. No puedes registrar actividades.",
                        Alert.AlertType.WARNING);
                openWelcomePage(anchorPane);
            } else {
                ProjectDAO projectDAO = new ProjectDAO();
                Project project = projectDAO.findById(assignment.getIdProject());
                boolean hasProject = project != null;
                if (hasProject) {
                    assignedProject = project;
                    projectNameLabel.setText(project.getName());
                }
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error loading intern project: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudo cargar su proyecto. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean isInputInvalid() {
        boolean isProjectMissing = assignedProject == null;
        boolean isNameEmpty = nameTextField.getText().isBlank();
        boolean isInvalid = false;
        if (isProjectMissing) {
            isInvalid = true;
        } else if (isNameEmpty) {
            isInvalid = true;
        }
        return isInvalid;
    }

    private boolean areDatesInvalid() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        boolean hasInvalidOrder = hasInvalidDateOrder(startDate, endDate);
        return hasInvalidOrder;
    }

    private boolean hasInvalidDateOrder(LocalDate startDate, LocalDate endDate) {
        boolean hasInvalidOrder = false;
        if (startDate != null) {
            if (endDate != null) {
                if (!endDate.isAfter(startDate)) {
                    hasInvalidOrder = true;
                }
            }
        }
        return hasInvalidOrder;
    }

    private boolean areDatesOutsideProjectRange() {
        Project project = assignedProject;
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        boolean isOutOfRange = false;
        if (isStartBeforeProjectStart(project, startDate)) {
            isOutOfRange = true;
        } else if (isEndAfterProjectEnd(project, endDate)) {
            isOutOfRange = true;
        }
        return isOutOfRange;
    }

    private boolean isProjectStartPresent(Project project) {
        boolean isPresent = false;
        if (project != null) {
            if (project.getStartDate() != null) {
                isPresent = true;
            }
        }
        return isPresent;
    }

    private boolean isProjectEndPresent(Project project) {
        boolean isPresent = false;
        if (project != null) {
            if (project.getEndDate() != null) {
                isPresent = true;
            }
        }
        return isPresent;
    }

    private boolean isStartBeforeProjectStart(Project project, LocalDate startDate) {
        boolean isBefore = false;
        if (isProjectStartPresent(project)) {
            if (startDate != null) {
                if (startDate.isBefore(project.getStartDate())) {
                    isBefore = true;
                }
            }
        }
        return isBefore;
    }

    private boolean isEndAfterProjectEnd(Project project, LocalDate endDate) {
        boolean isAfter = false;
        if (isProjectEndPresent(project)) {
            if (endDate != null) {
                if (endDate.isAfter(project.getEndDate())) {
                    isAfter = true;
                }
            }
        }
        return isAfter;
    }

    private void clearForm() {
        nameTextField.clear();
        descriptionTextArea.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

}
