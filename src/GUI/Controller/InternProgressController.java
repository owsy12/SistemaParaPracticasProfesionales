package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ActivityDAO;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.InternActivityDAO;
import Logic.DAO.ReportDAO;
import Logic.DTOs.Activity;
import Logic.DTOs.Assignment;
import Logic.DTOs.InternActivity;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;

public class InternProgressController {

    private static final Logger LOGGER = Logger.getLogger(InternProgressController.class.getName());

    private static final int PARTIAL_MIN_HOURS = 210;
    private static final int FINAL_MIN_HOURS = 420;

    @FXML
    private Label projectLabel;

    @FXML
    private Label totalHoursLabel;

    @FXML
    private Label approvedHoursLabel;

    @FXML
    private Label partialStatusLabel;

    @FXML
    private Label finalStatusLabel;

    @FXML
    private TableView<InternActivity> activitiesTableView;

    @FXML
    private TableColumn<InternActivity, String> activityNameColumn;

    @FXML
    private TableColumn<InternActivity, String> hoursColumn;

    @FXML
    private TableColumn<InternActivity, String> statusColumn;

    @FXML
    private TableColumn<InternActivity, String> observationsColumn;

    @FXML
    private ComboBox<Activity> activityComboBox;

    @FXML
    private TextField hoursTextField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextArea observationsTextArea;

    private int currentInternId;
    private int currentProjectId;
    private InternActivity selectedInternActivity;

    @FXML
    private void initialize() {
        setTypeAndLength(hoursTextField, "Number");
        statusComboBox.getItems().setAll("Pendiente", "En Progreso", "Completada");
        configureListeners();
        loadInternContext();
    }

    @FXML
    public void saveProgress(ActionEvent actionEvent) {
        boolean isActivityMissing = activityComboBox.getValue() == null;
        boolean isHoursEmpty = hoursTextField.getText().isBlank();
        boolean isStatusMissing = statusComboBox.getValue() == null;

        if (isActivityMissing) {
            showAlert("Actividad requerida",
                    "Seleccione una actividad del proyecto.", Alert.AlertType.WARNING);
        } else if (isHoursEmpty) {
            showAlert("Horas requeridas",
                    "Ingrese las horas dedicadas a la actividad.", Alert.AlertType.WARNING);
        } else if (isStatusMissing) {
            showAlert("Estado requerido",
                    "Seleccione el estado de la actividad.", Alert.AlertType.WARNING);
        } else {
            saveProgressProcess();
        }
    }

    @FXML
    public void updateProgress(ActionEvent actionEvent) {
        if (selectedInternActivity == null) {
            showAlert("Sin selección",
                    "Seleccione un registro de la tabla para actualizar.",
                    Alert.AlertType.WARNING);
        } else {
            updateProgressProcess();
        }
    }

    @FXML
    public void clearSelection(ActionEvent actionEvent) {
        clearForm();
    }

    private void configureListeners() {
        activitiesTableView.getSelectionModel().selectedItemProperty()
                .addListener(new InternActivitySelectionListener());
    }

    private final class InternActivitySelectionListener implements ChangeListener<InternActivity> {
        @Override
        public void changed(ObservableValue<? extends InternActivity> observable,
                            InternActivity oldValue, InternActivity newValue) {
            if (newValue != null) {
                selectedInternActivity = newValue;
                populateForm(newValue);
            }
        }
    }

    private void loadInternContext() {
        currentInternId = SessionManager.getInstance().getUsuario().getId();

        try {
            AssignmentDAO assignmentDAO = new AssignmentDAO();
            Assignment activeAssignment = assignmentDAO.getActiveByIdIntern(currentInternId);

            if (activeAssignment == null) {
                showAlert("Sin proyecto asignado",
                        "No tiene un proyecto activo asignado.",
                        Alert.AlertType.WARNING);
                disableAll();
            } else {
                currentProjectId = activeAssignment.getIdProyect();
                String projectIdText = "Proyecto ID: " + currentProjectId;
                projectLabel.setText(projectIdText);
                loadActivitiesForProject();
                refreshInternActivities();
                refreshHoursSummary();
            }

        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar contexto del practicante {0}: {1}",
                    new Object[]{currentInternId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo cargar la información del proyecto. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void saveProgressProcess() {
        try {
            InternActivityDAO internActivityDAO = new InternActivityDAO();
            Activity selectedActivity = activityComboBox.getValue();

            InternActivity existing = internActivityDAO.findByActivityAndIntern(
                    selectedActivity.getIdActivity(), currentInternId);

            if (existing != null) {
                existing.setDedicatedHours(Integer.parseInt(hoursTextField.getText()));
                existing.setStatus(statusComboBox.getValue());
                existing.setObservations(observationsTextArea.getText().trim());

                boolean isCompleted = "Completada".equals(statusComboBox.getValue());
                LocalDate completionDate = null;
                if (isCompleted) {
                    completionDate = LocalDate.now();
                }
                existing.setCompletionDate(completionDate);

                if (internActivityDAO.update(existing)) {
                    showAlert("Progreso actualizado",
                            "El avance fue actualizado correctamente.",
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "No se pudo actualizar el avance.",
                            Alert.AlertType.ERROR);
                }
            } else {
                InternActivity internActivity = buildInternActivity(selectedActivity);
                int savedId = internActivityDAO.save(internActivity);

                if (savedId > 0) {
                    showAlert("Progreso registrado",
                            "El avance fue registrado correctamente.",
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error", "No se pudo registrar el avance.",
                            Alert.AlertType.ERROR);
                }
            }

            refreshInternActivities();
            refreshHoursSummary();
            clearForm();

        } catch (NumberFormatException numberFormatException) {
            showAlert("Horas inválidas",
                    "Ingrese un número válido de horas.", Alert.AlertType.WARNING);
        } catch (DuplicateEntryException duplicateEntryException) {
            showAlert("Registro duplicado",
                    "Ya existe un registro para esta actividad. Use 'Actualizar' en su lugar.",
                    Alert.AlertType.WARNING);
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al guardar progreso: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudo guardar el progreso. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void updateProgressProcess() {
        try {
            selectedInternActivity.setDedicatedHours(
                    Integer.parseInt(hoursTextField.getText()));
            selectedInternActivity.setStatus(statusComboBox.getValue());
            selectedInternActivity.setObservations(observationsTextArea.getText().trim());

            boolean isCompleted = "Completada".equals(statusComboBox.getValue());
            if (isCompleted) {
                selectedInternActivity.setCompletionDate(LocalDate.now());
            }

            InternActivityDAO internActivityDAO = new InternActivityDAO();

            if (internActivityDAO.update(selectedInternActivity)) {
                showAlert("Progreso actualizado",
                        "El avance fue actualizado correctamente.",
                        Alert.AlertType.INFORMATION);
                refreshInternActivities();
                refreshHoursSummary();
                clearForm();
            } else {
                showAlert("Error", "No se pudo actualizar el avance.",
                        Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException numberFormatException) {
            showAlert("Horas inválidas",
                    "Ingrese un número válido de horas.", Alert.AlertType.WARNING);
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar progreso {0}: {1}",
                    new Object[]{selectedInternActivity.getIdInternActivity(),
                                 serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo actualizar el progreso. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadActivitiesForProject() {
        try {
            ActivityDAO activityDAO = new ActivityDAO();
            List<Activity> activities = activityDAO.findByProject(currentProjectId);
            activityComboBox.getItems().setAll(activities);

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar actividades del proyecto {0}: {1}",
                    new Object[]{currentProjectId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar las actividades.", Alert.AlertType.ERROR);
        }
    }

    private void refreshInternActivities() {
        try {
            InternActivityDAO internActivityDAO = new InternActivityDAO();
            List<InternActivity> internActivities =
                    internActivityDAO.findByInternAndProject(currentInternId, currentProjectId);
            activitiesTableView.setItems(FXCollections.observableArrayList(internActivities));
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar avances del practicante {0}: {1}",
                    new Object[]{currentInternId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los avances registrados.", Alert.AlertType.ERROR);
        }
    }

    private void refreshHoursSummary() {
        try {
            InternActivityDAO internActivityDAO = new InternActivityDAO();
            int totalHours = internActivityDAO.getTotalHoursByIntern(currentInternId);
            String totalHoursText = "Horas registradas: " + totalHours;
            totalHoursLabel.setText(totalHoursText);

            ReportDAO reportDAO = new ReportDAO();
            int approvedHoursCount = reportDAO.getTotalApprovedHoursByIntern(currentInternId);
            String approvedHoursText = "Horas validadas: " + approvedHoursCount;
            approvedHoursLabel.setText(approvedHoursText);

            boolean hasPartialHours = approvedHoursCount >= PARTIAL_MIN_HOURS;
            if (hasPartialHours) {
                String partialAvailableText = "Reporte parcial: DISPONIBLE ("
                        + approvedHoursCount + "/" + PARTIAL_MIN_HOURS + "h)";
                partialStatusLabel.setText(partialAvailableText);
            } else {
                String partialProgressText = "Reporte parcial: "
                        + approvedHoursCount + "/" + PARTIAL_MIN_HOURS + "h";
                partialStatusLabel.setText(partialProgressText);
            }

            boolean hasFinalHours = approvedHoursCount >= FINAL_MIN_HOURS;
            if (hasFinalHours) {
                finalStatusLabel.setText(
                        "Reporte final: DISPONIBLE (" + approvedHoursCount
                        + "/" + FINAL_MIN_HOURS + "h)");
            } else {
                finalStatusLabel.setText(
                        "Reporte final: " + approvedHoursCount + "/" + FINAL_MIN_HOURS + "h");
            }

        } catch (ValidationException validationException) {
            LOGGER.log(Level.SEVERE, "Error de validación al calcular horas: {0}",
                    validationException.getMessage());
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al calcular resumen de horas: {0}",
                    serviceException.getMessage());
        }
    }

    private InternActivity buildInternActivity(Activity activity) {
        InternActivity internActivity = new InternActivity();
        internActivity.setIdActivity(activity.getIdActivity());
        internActivity.setIdIntern(currentInternId);
        internActivity.setDedicatedHours(Integer.parseInt(hoursTextField.getText()));
        internActivity.setStatus(statusComboBox.getValue());
        internActivity.setObservations(observationsTextArea.getText().trim());

        boolean isCompleted = "Completada".equals(statusComboBox.getValue());
        if (isCompleted) {
            internActivity.setCompletionDate(LocalDate.now());
        }

        return internActivity;
    }

    private void populateForm(InternActivity internActivity) {
        hoursTextField.setText(String.valueOf(internActivity.getDedicatedHours()));
        statusComboBox.setValue(internActivity.getStatus());

        String observations = "";
        if (internActivity.getObservations() != null) {
            observations = internActivity.getObservations();
        }
        observationsTextArea.setText(observations);
    }

    private void disableAll() {
        activityComboBox.setDisable(true);
        hoursTextField.setDisable(true);
        statusComboBox.setDisable(true);
        observationsTextArea.setDisable(true);
    }

    private void clearForm() {
        selectedInternActivity = null;
        activityComboBox.getSelectionModel().clearSelection();
        hoursTextField.clear();
        statusComboBox.getSelectionModel().clearSelection();
        observationsTextArea.clear();
        activitiesTableView.getSelectionModel().clearSelection();
    }

}
