package GUI.Controller;

import Logic.DAO.ActivityDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Activity;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;

public class ManageActivitiesController {

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
    private Label statusLabel;

    private Activity selectedActivity;

    @FXML
    private void initialize() {
        setTypeAndLength(nameTextField, "Text");
        semanaInicioSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 1));
        semanaFinSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 8));
        configureTable();
        configureListeners();
        loadProjects();
    }

    @FXML
    public void loadActivities(ActionEvent actionEvent) {
        Project selectedProject = projectComboBox.getValue();
        if (selectedProject == null) {
            showAlert("Sin selección", "Seleccione un proyecto.", Alert.AlertType.WARNING);
        } else {
            refreshActivities(selectedProject.getIdProyect());
        }
    }

    @FXML
    public void updateActivity(ActionEvent actionEvent) {
        boolean isActivityMissing = selectedActivity == null;
        boolean isNameEmpty = nameTextField.getText().isBlank();
        boolean areDatesWrong = areDatesInvalid();

        if (isActivityMissing) {
            showAlert("Sin selección",
                    "Seleccione una actividad de la tabla para editar.",
                    Alert.AlertType.WARNING);
        } else if (isNameEmpty) {
            showAlert("Campo requerido",
                    "El nombre de la actividad no puede estar vacío.",
                    Alert.AlertType.WARNING);
        } else if (areDatesWrong) {
            showAlert("Fechas inválidas",
                    "La fecha de entrega debe ser posterior a la fecha de inicio.",
                    Alert.AlertType.WARNING);
        } else {
            updateProcess();
        }
    }

    @FXML
    public void deleteActivity(ActionEvent actionEvent) {
        if (selectedActivity == null) {
            showAlert("Sin selección",
                    "Seleccione una actividad para eliminar.",
                    Alert.AlertType.WARNING);
        } else {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Eliminar la actividad \"" + selectedActivity.getName() + "\"?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> confirmationResult = confirmation.showAndWait();

            if (confirmationResult.isPresent() && confirmationResult.get() == ButtonType.YES) {
                deleteProcess();
            }
        }
    }

    @FXML
    public void clearSelection(ActionEvent actionEvent) {
        clearForm();
    }

    public void setProject(Project project) {
        for (Project projectItem : projectComboBox.getItems()) {
            if (projectItem.getIdProyect() == project.getIdProyect()) {
                projectComboBox.getSelectionModel().select(projectItem);
                refreshActivities(projectItem.getIdProyect());
            }
        }
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Activity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Activity, String> data) {
                return new SimpleStringProperty(data.getValue().getName());
            }
        });

        descriptionColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Activity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Activity, String> data) {
                return new SimpleStringProperty(data.getValue().getDescription());
            }
        });

        statusColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Activity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Activity, String> data) {
                return new SimpleStringProperty(data.getValue().getStatus());
            }
        });
    }

    private void configureListeners() {
        activitiesTableView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<Activity>() {
                    @Override
                    public void changed(ObservableValue<? extends Activity> observable,
                                        Activity oldValue, Activity newValue) {
                        if (newValue != null) {
                            selectedActivity = newValue;
                            populateForm(newValue);
                        }
                    }
                });
    }

    private void updateProcess() {
        try {
            selectedActivity.setName(nameTextField.getText().trim());
            selectedActivity.setDescription(descriptionTextArea.getText().trim());
            selectedActivity.setSemanaInicioPlan(semanaInicioSpinner.getValue());
            selectedActivity.setSemanaFinPlan(semanaFinSpinner.getValue());
            selectedActivity.setFechaInicio(fechaInicioPicker.getValue());
            selectedActivity.setFechaFin(fechaFinPicker.getValue());

            ActivityDAO activityDAO = new ActivityDAO();

            if (activityDAO.update(selectedActivity)) {
                showAlert("Actividad actualizada",
                        "La actividad fue actualizada correctamente.",
                        Alert.AlertType.INFORMATION);
                refreshActivities(projectComboBox.getValue().getIdProyect());
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
            ActivityDAO activityDAO = new ActivityDAO();

            if (activityDAO.delete(selectedActivity.getIdActivity())) {
                showAlert("Actividad eliminada",
                        "La actividad fue eliminada correctamente.",
                        Alert.AlertType.INFORMATION);
                refreshActivities(projectComboBox.getValue().getIdProyect());
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

    private boolean areDatesInvalid() {
        LocalDate inicio = fechaInicioPicker.getValue();
        LocalDate fin = fechaFinPicker.getValue();
        boolean bothProvided = inicio != null && fin != null;
        boolean invalid = bothProvided && !fin.isAfter(inicio);
        return invalid;
    }

    private void loadProjects() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projects = projectDAO.findAll();
            projectComboBox.getItems().setAll(projects);

            projectComboBox.setCellFactory(
                    new Callback<ListView<Project>, ListCell<Project>>() {
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
                    "No se pudieron cargar los proyectos.", Alert.AlertType.ERROR);
        }
    }

    private void refreshActivities(int idProject) {
        try {
            ActivityDAO activityDAO = new ActivityDAO();
            List<Activity> activities = activityDAO.findByProject(idProject);
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

        semanaInicioSpinner.getValueFactory().setValue(activity.getSemanaInicioPlan());
        semanaFinSpinner.getValueFactory().setValue(activity.getSemanaFinPlan());
        fechaInicioPicker.setValue(activity.getFechaInicio());
        fechaFinPicker.setValue(activity.getFechaFin());
        String statusText = "Estado: " + activity.getStatus();
        statusLabel.setText(statusText);
    }

    private void clearForm() {
        selectedActivity = null;
        nameTextField.clear();
        descriptionTextArea.clear();
        semanaInicioSpinner.getValueFactory().setValue(1);
        semanaFinSpinner.getValueFactory().setValue(8);
        fechaInicioPicker.setValue(null);
        fechaFinPicker.setValue(null);
        statusLabel.setText("");
        activitiesTableView.getSelectionModel().clearSelection();
    }

}
