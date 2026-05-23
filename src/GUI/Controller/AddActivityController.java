package GUI.Controller;

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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class AddActivityController {

    private static final Logger LOGGER = Logger.getLogger(AddActivityController.class.getName());

    public AnchorPane anchorPane;

    @FXML
    private ComboBox<Project> projectComboBox;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private Spinner<Integer> semanaInicioSpinner;

    @FXML
    private Spinner<Integer> semanaFinSpinner;

    @FXML
    private DatePicker fechaInicioPicker;

    @FXML
    private DatePicker fechaFinPicker;

    @FXML
    private void initialize() {
        setTypeAndLength(nameTextField, "Text");
        semanaInicioSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 1));
        semanaFinSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 8));
        loadProjects();
    }

    @FXML
    public void saveActivity(ActionEvent actionEvent) {
        if (isInputInvalid()) {
            showAlert("Campos incompletos",
                    "Complete todos los campos requeridos antes de guardar.",
                    Alert.AlertType.WARNING);
        } else if (areDatesInvalid()) {
            showAlert("Fechas inválidas",
                    "La fecha de entrega debe ser posterior a la fecha de inicio.",
                    Alert.AlertType.WARNING);
        } else {
            saveProcess();
        }
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        openWelcomePage(anchorPane);
    }

    public void setProject(Project project) {
        for (Project projectItem : projectComboBox.getItems()) {
            if (projectItem.getIdProyect() == project.getIdProyect()) {
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
        activity.setIdProject(projectComboBox.getValue().getIdProyect());
        activity.setName(nameTextField.getText().trim());
        activity.setDescription(descriptionTextArea.getText().trim());
        activity.setSemanaInicioPlan(semanaInicioSpinner.getValue());
        activity.setSemanaFinPlan(semanaFinSpinner.getValue());
        activity.setFechaInicio(fechaInicioPicker.getValue());
        activity.setFechaFin(fechaFinPicker.getValue());
        activity.setCreationDate(LocalDate.now());
        activity.setStatus("Activa");
        return activity;
    }

    private void loadProjects() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projects = projectDAO.findAll();

            projectComboBox.getItems().setAll(projects);

            projectComboBox.setCellFactory(new Callback<ListView<Project>, ListCell<Project>>() {
                @Override
                public ListCell<Project> call(ListView<Project> listView) {
                    return new ListCell<Project>() {
                        @Override
                        protected void updateItem(Project item, boolean empty) {
                            super.updateItem(item, empty);
                            String displayText = null;
                            if (!empty && item != null) {
                                displayText = item.getName();
                            }
                            setText(displayText);
                        }
                    };
                }
            });

            projectComboBox.setButtonCell(new ListCell<Project>() {
                @Override
                protected void updateItem(Project item, boolean empty) {
                    super.updateItem(item, empty);
                    String displayText = null;
                    if (!empty && item != null) {
                        displayText = item.getName();
                    }
                    setText(displayText);
                }
            });

        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar proyectos: {0}",
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
        LocalDate inicio = fechaInicioPicker.getValue();
        LocalDate fin = fechaFinPicker.getValue();
        boolean bothProvided = inicio != null && fin != null;
        boolean invalid = bothProvided && !fin.isAfter(inicio);
        return invalid;
    }

    private void clearForm() {
        nameTextField.clear();
        descriptionTextArea.clear();
        semanaInicioSpinner.getValueFactory().setValue(1);
        semanaFinSpinner.getValueFactory().setValue(8);
        fechaInicioPicker.setValue(null);
        fechaFinPicker.setValue(null);
    }

}
