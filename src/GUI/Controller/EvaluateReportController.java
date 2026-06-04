package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.InitialFormatDAO;
import Logic.DAO.InternDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.ReportActivityDAO;
import Logic.DAO.ReportDAO;
import Logic.DTOs.InitialFormat;
import Logic.DTOs.Intern;
import Logic.DTOs.Project;
import Logic.DTOs.Report;
import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportStatusUpdate;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import GUI.Utils.RestrictedTextArea;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.applyTextAreaRestriction;

public class EvaluateReportController implements ChangeListener<Object> {
    private static final String STATUS_APPROVED = "Aprobado";
    private static final String STATUS_EVALUATED = "Evaluado";
    private static final String STATUS_REJECTED = "Rechazado";


    private static final Logger LOGGER = Logger.getLogger(EvaluateReportController.class.getName());

    @FXML
    private ComboBox<Project> projectComboBox;

    @FXML
    private ComboBox<Intern> internComboBox;

    @FXML
    private TableView<Report> reportsTableView;

    @FXML
    private TableColumn<Report, String> numColumn;

    @FXML
    private TableColumn<Report, String> typeColumn;

    @FXML
    private TableColumn<Report, String> periodColumn;

    @FXML
    private TableColumn<Report, String> hoursColumn;

    @FXML
    private TableColumn<Report, String> statusColumn;

    @FXML
    private TableColumn<Report, String> dateColumn;

    @FXML
    private Label reportDetailLabel;

    @FXML
    private Label documentPathLabel;

    @FXML
    private Label signedPathLabel;

    @FXML
    private TableView<ReportActivity> activitiesTableView;

    @FXML
    private TableColumn<ReportActivity, String> actNameColumn;

    @FXML
    private TableColumn<ReportActivity, String> actPeriodColumn;

    @FXML
    private TableColumn<ReportActivity, String> actObsColumn;

    @FXML
    private TableView<InitialFormat> initialFormatsTableView;

    @FXML
    private TableColumn<InitialFormat, String> initFormatTypeColumn;

    @FXML
    private TableColumn<InitialFormat, String> initStatusColumn;

    @FXML
    private RestrictedTextArea observationsTextArea;

    @FXML
    private Button rejectButton;

    @FXML
    private Button markInReviewButton;

    private Report selectedReport;
    private int currentProfessorId;

    @FXML
    private void initialize() {
        currentProfessorId = SessionManager.getInstance().getUsuario().getId();
        applyTextAreaRestriction(observationsTextArea, 200);
        configureListeners();
        loadProjects();
    }

    @FXML
    public void rejectReport(ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean isStatusInvalid = selectedReport != null && !"En revision".equals(selectedReport.getStatus());
        boolean isObservationEmpty = observationsTextArea.getText().isBlank();

        if (isReportMissing) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else if (isStatusInvalid) {
            showAlert("Estado inválido", "Solo puede rechazar reportes en estado En revision.",
                    Alert.AlertType.WARNING);
        } else if (isObservationEmpty) {
            showAlert("Observación requerida",
                    "Ingrese el motivo del rechazo en el campo de observaciones.",
                    Alert.AlertType.WARNING);
        } else {
            updateReportStatus(STATUS_REJECTED);
        }
    }

    @FXML
    public void evaluateReport (ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean isStatusInvalid = selectedReport != null && !"En revision".equals(selectedReport.getStatus());
        boolean isObservationEmpty = observationsTextArea.getText().isBlank();

        if (isReportMissing) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else if (isStatusInvalid) {
            showAlert("Estado inválido",
                    "Solo puede evaluar reportes en estado En revision.",
                    Alert.AlertType.WARNING);
        } else if (isObservationEmpty) {
            showAlert("Observación requerida",
                    "Debe ingresar observaciones para evaluar el reporte.",
                    Alert.AlertType.WARNING);
        } else {
            updateReportStatus(STATUS_EVALUATED);
        }
    }

    @FXML
    public void clearSelection(ActionEvent actionEvent) {
        clearForm();
    }

    @FXML
    public void openDocument(ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean hasNoDocument = selectedReport != null && (selectedReport.getDocumentPath() == null
                    || selectedReport.getDocumentPath().isBlank());

        if (isReportMissing) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else if (hasNoDocument) {
            showAlert("Sin documento",
                    "Este reporte no tiene un documento generado.", Alert.AlertType.WARNING);
        } else {
            tryOpenFile(selectedReport.getDocumentPath());
        }
    }

    @FXML
    public void saveDocumentCopy(ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean hasNoDocument = selectedReport != null && (selectedReport.getDocumentPath() == null
                    || selectedReport.getDocumentPath().isBlank());

        if (isReportMissing) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else if (hasNoDocument) {
            showAlert("Sin documento",
                    "Este reporte no tiene un documento generado.", Alert.AlertType.WARNING);
        } else {
            trySaveFileCopy(selectedReport.getDocumentPath());
        }
    }

    @FXML
    public void openSignedDocument(ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean hasNoSigned = selectedReport != null && (selectedReport.getSignedDocumentPath() == null
                    || selectedReport.getSignedDocumentPath().isBlank());

        if (isReportMissing) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else if (hasNoSigned) {
            showAlert("Sin documento firmado",
                    "Este reporte no tiene un documento firmado.", Alert.AlertType.WARNING);
        } else {
            tryOpenFile(selectedReport.getSignedDocumentPath());
        }
    }

    @FXML
    public void saveSignedCopy(ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean hasNoSigned = selectedReport != null && (selectedReport.getSignedDocumentPath() == null
                    || selectedReport.getSignedDocumentPath().isBlank());

        if (isReportMissing) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else if (hasNoSigned) {
            showAlert("Sin documento firmado",
                    "Este reporte no tiene un documento firmado.", Alert.AlertType.WARNING);
        } else {
            trySaveFileCopy(selectedReport.getSignedDocumentPath());
        }
    }

    private void tryOpenFile(String filePath) {
        File file = new File(filePath);
        boolean fileExists = file.exists();
        if (!fileExists) {
            showAlert("Archivo no encontrado", "El documento no fue encontrado en: " + filePath,
                    Alert.AlertType.WARNING);
        } else {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, "Error al abrir documento: {0}", ioException.getMessage());
                showAlert("Error al abrir", "No se pudo abrir el documento con el visor predeterminado.",
                        Alert.AlertType.ERROR);
            }
        }
    }

    private void trySaveFileCopy(String filePath) {
        File sourceFile = new File(filePath);
        boolean fileExists = sourceFile.exists();
        if (!fileExists) {
            showAlert("Archivo no encontrado",
                    "El documento no fue encontrado en: " + filePath,
                    Alert.AlertType.WARNING);
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar copia del reporte");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName(sourceFile.getName());

            File destination = fileChooser.showSaveDialog(rejectButton.getScene().getWindow());
            boolean destinationSelected = destination != null;
            if (destinationSelected) {
                try {
                    Files.copy(sourceFile.toPath(), destination.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    showAlert("Guardado",
                            "Documento guardado correctamente en: " + destination.getAbsolutePath(),
                            Alert.AlertType.INFORMATION);
                } catch (IOException ioException) {
                    LOGGER.log(Level.SEVERE, "Error al guardar copia: {0}",
                            ioException.getMessage());
                    showAlert("Error al guardar",
                            "No se pudo guardar la copia del documento.", Alert.AlertType.ERROR);
                }
            }
        }
    }

    private void configureListeners() {
        projectComboBox.getSelectionModel().selectedItemProperty().addListener(this);
        internComboBox.getSelectionModel().selectedItemProperty().addListener(this);
        reportsTableView.getSelectionModel().selectedItemProperty().addListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends Object> observable,
                        Object oldValue, Object newValue) {
        if (newValue != null) {
            if (observable == projectComboBox.getSelectionModel().selectedItemProperty()) {
                loadInternsForProject((Project) newValue);
            } else if (observable == internComboBox.getSelectionModel().selectedItemProperty()) {
                loadReportsForIntern((Intern) newValue);
                loadInitialFormatsForIntern((Intern) newValue);
            } else if (observable == reportsTableView.getSelectionModel().selectedItemProperty()) {
                selectedReport = (Report) newValue;
                populateReportDetail((Report) newValue);
                loadActivitiesForReport((Report) newValue);
            }
        }
    }

    private void loadProjects() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> allProjects = projectDAO.findAll();
            List<Project> professorProjects = new ArrayList<>();

            for (Project project : allProjects) {
                if (project.getIdProfessor() == currentProfessorId) {
                    professorProjects.add(project);
                }
            }

            projectComboBox.setItems(FXCollections.observableArrayList(professorProjects));
            internComboBox.setDisable(true);

        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar proyectos del profesor {0}: {1}",
                    new Object[]{currentProfessorId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los proyectos. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadInternsForProject(Project project) {
        try {
            InternDAO internDAO = new InternDAO();
            List<Intern> interns = internDAO.findByProject(project.getIdProyect());
            internComboBox.setItems(FXCollections.observableArrayList(interns));
            internComboBox.setDisable(false);
            reportsTableView.getItems().clear();
            clearForm();
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar practicantes del proyecto {0}: {1}",
                    new Object[]{project.getIdProyect(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los practicantes. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadReportsForIntern(Intern intern) {
        try {
            Project selectedProject = projectComboBox.getValue();
            ReportDAO reportDAO = new ReportDAO();
            List<Report> reports = reportDAO.getByInternAndProject(
                    intern.getId(), selectedProject.getIdProyect());

            reportsTableView.setItems(FXCollections.observableArrayList(reports));
            clearForm();
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar reportes del practicante {0}: {1}",
                    new Object[]{intern.getId(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los reportes. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadInitialFormatsForIntern(Intern intern) {
        try {
            InitialFormatDAO initialFormatDAO = new InitialFormatDAO();
            List<InitialFormat> formats = initialFormatDAO.getByIdIntern(intern.getId());
            initialFormatsTableView.setItems(FXCollections.observableArrayList(formats));
        } catch (ValidationException validationException) {
            LOGGER.log(Level.SEVERE, "Error de validación al cargar documentos iniciales: {0}",
                    validationException.getMessage());
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al cargar documentos iniciales del practicante {0}: {1}",
                    new Object[]{intern.getId(), serviceException.getMessage()});
        }
    }

    private void loadActivitiesForReport(Report report) {
        try {
            ReportActivityDAO reportActivityDAO = new ReportActivityDAO();
            List<ReportActivity> activities = reportActivityDAO.findByReport(report.getIdReport());
            activitiesTableView.setItems(FXCollections.observableArrayList(activities));
        } catch (ValidationException validationException) {
            LOGGER.log(Level.SEVERE, "Error de validación al cargar actividades: {0}",
                    validationException.getMessage());
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al cargar actividades del reporte {0}: {1}",
                    new Object[]{report.getIdReport(), serviceException.getMessage()});
        }
    }

    private void updateReportStatus(String newStatus) {
        try {
            ReportDAO reportDAO = new ReportDAO();
            String observations = observationsTextArea.getText().trim();
            Date reviewDate = Date.valueOf(LocalDate.now());

            String observationsToSave = null;
            boolean hasObservations = !observations.isEmpty();
            if (hasObservations) {
                observationsToSave = observations;
            }

            ReportStatusUpdate statusUpdate = new ReportStatusUpdate(newStatus, observationsToSave, reviewDate);
            boolean updated = reportDAO.updateStatus(selectedReport.getIdReport(), statusUpdate);

            if (updated) {
                String message = buildStatusMessage(newStatus);
                showAlert("Estado actualizado", message, Alert.AlertType.INFORMATION);
                Intern currentIntern = internComboBox.getValue();
                if (currentIntern != null) {
                    loadReportsForIntern(currentIntern);
                }
                clearForm();
            } else {
                showAlert("Error", "No se pudo actualizar el estado del reporte.",
                        Alert.AlertType.ERROR);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al actualizar estado del reporte {0}: {1}",
                    new Object[]{selectedReport.getIdReport(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo actualizar el reporte. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void populateReportDetail(Report report) {
        String tardyIndicator = "";
        boolean isTardy = report.isEntregaTardia();
        if (isTardy) {
            tardyIndicator = "  ENTREGA TARDÍA";
        }

        String detailText = "Tipo: " + report.getReportType() + "  |  Período: " + report.getPeriod()
                + "  |  Horas: " + report.getReportedHours() + "  |  Estado: " + report.getStatus()
                + tardyIndicator;
        reportDetailLabel.setText(detailText);

        String documentPathText = "—";
        boolean hasDocumentPath = report.getDocumentPath() != null
                && !report.getDocumentPath().isBlank();
        if (hasDocumentPath) {
            documentPathText = "Documento disponible";
        }
        documentPathLabel.setText(documentPathText);

        String signedPathText = "—";
        boolean hasSignedPath = report.getSignedDocumentPath() != null
                && !report.getSignedDocumentPath().isBlank();
        if (hasSignedPath) {
            signedPathText = "Documento disponible";
        }
        signedPathLabel.setText(signedPathText);

        boolean hasProfessorObservations = report.getProfessorObservations() != null;
        if (hasProfessorObservations) {
            observationsTextArea.setText(report.getProfessorObservations());
        }
    }

    private String buildStatusMessage(String status) {
        String message;
        switch (status) {
            case STATUS_APPROVED:
                message = "Reporte aprobado. Las horas han sido validadas.";
                break;
            case STATUS_REJECTED:
                message = "Reporte rechazado.";
                break;
            case STATUS_EVALUATED:
                message = "Reporte evaluado. Las horas han sido contabilizadas.";
                break;
            default:
                message = "Estado actualizado a: " + status;
        }
        return message;
    }

    private void clearForm() {
        selectedReport = null;
        reportDetailLabel.setText("");
        documentPathLabel.setText("—");
        signedPathLabel.setText("—");
        observationsTextArea.clear();
        activitiesTableView.getItems().clear();
        initialFormatsTableView.getItems().clear();
        reportsTableView.getSelectionModel().clearSelection();
    }

    private final class ProjectSelectionListener implements ChangeListener<Project> {
        @Override
        public void changed(ObservableValue<? extends Project> observable,
                            Project oldValue, Project newValue) {
            if (newValue != null) {
                loadInternsForProject(newValue);
            }
        }
    }

    private final class InternSelectionListener implements ChangeListener<Intern> {
        @Override
        public void changed(ObservableValue<? extends Intern> observable,
                            Intern oldValue, Intern newValue) {
            if (newValue != null) {
                loadReportsForIntern(newValue);
                loadInitialFormatsForIntern(newValue);
            }
        }
    }

    private final class ReportSelectionListener implements ChangeListener<Report> {
        @Override
        public void changed(ObservableValue<? extends Report> observable,
                            Report oldValue, Report newValue) {
            if (newValue != null) {
                selectedReport = newValue;
                populateReportDetail(newValue);
                loadActivitiesForReport(newValue);
            }
        }
    }

    private final class InternStringConverter extends StringConverter<Intern> {
        @Override
        public String toString(Intern intern) {
            String result = "";
            boolean hasIntern = intern != null;
            if (hasIntern) {
                result = intern.getFirstName() + " " + intern.getLastName() + " " + intern.getSecondLastName();
            }
            return result;
        }

        @Override
        public Intern fromString(String string) {
            Intern result = null;
            return result;
        }
    }

}
