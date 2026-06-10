package GUI.Controller;

import GUI.SessionManager.SessionManager;
import GUI.Utils.AuditLog;
import GUI.Utils.EvaluationPrerequisiteChecker;
import Logic.DAO.InitialFormatDAO;
import Logic.DAO.InternDAO;
import Logic.DAO.OVEvaluationDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.ReportActivityDAO;
import Logic.DAO.ReportDAO;
import Logic.DAO.SelfEvaluationDAO;
import Logic.DTOs.InitialFormat;
import Logic.DTOs.Intern;
import Logic.DTOs.OVEvaluation;
import Logic.DTOs.Project;
import Logic.DTOs.Report;
import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportStatusUpdate;
import Logic.DTOs.SelfEvaluation;
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
import GUI.Utils.RestrictedTextField;
import javafx.stage.FileChooser;

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
    private static final java.util.regex.Pattern GRADE_PATTERN =
            java.util.regex.Pattern.compile("^(?:10|[0-9])(?:\\.[0-9]{1,2})?$");
    private static final java.util.regex.Pattern GRADE_INPUT_PATTERN =
            java.util.regex.Pattern.compile("^(?:10|[0-9])?(?:\\.[0-9]{0,2})?$");
    private static final int GRADE_MAX_LENGTH = 5;


    private static final Logger LOGGER = Logger.getLogger(EvaluateReportController.class.getName());

    @FXML
    private ComboBox<Project> projectComboBox;

    @FXML
    private ComboBox<Intern> internComboBox;

    @FXML
    private TableView<Report> reportsTableView;

    @FXML
    private TableColumn<Report, String> numberColumn;

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
    private TableColumn<ReportActivity, String> activityNameColumn;

    @FXML
    private TableColumn<ReportActivity, String> activityPeriodColumn;

    @FXML
    private TableColumn<ReportActivity, String> activityObservationColumn;

    @FXML
    private TableView<InitialFormat> initialFormatsTableView;

    @FXML
    private TableColumn<InitialFormat, String> initFormatTypeColumn;

    @FXML
    private TableColumn<InitialFormat, String> initStatusColumn;

    @FXML
    private RestrictedTextArea observationsTextArea;

    @FXML
    private RestrictedTextField gradeTextField;

    @FXML
    private Button markInReviewButton;

    private Report selectedReport;
    private int currentProfessorId;

    @FXML
    private void initialize() {
        currentProfessorId = SessionManager.getInstance().getUser().getId();
        applyTextAreaRestriction(observationsTextArea, 200);
        gradeTextField.setRestriction(GRADE_MAX_LENGTH, GRADE_INPUT_PATTERN);
        configureListeners();
        loadProjects();
    }

    @FXML
    public void evaluateReport (ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean isStatusInvalid = selectedReport != null && !"En revision".equals(selectedReport.getStatus());
        boolean isObservationEmpty = observationsTextArea.getText().isBlank();
        boolean isGradeInvalid = parseGrade(gradeTextField.getText().trim()) == null;

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
        } else if (isGradeInvalid) {
            showAlert("Calificación requerida",
                    "Ingrese la calificación del reporte (un número entre 0 y 10).",
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

    @FXML
    public void openInitialDocument(ActionEvent actionEvent) {
        InitialFormat selectedFormat = initialFormatsTableView.getSelectionModel().getSelectedItem();
        boolean isSelectionMissing = selectedFormat == null;
        boolean hasNoFile = selectedFormat != null && (selectedFormat.getFilePath() == null
                || selectedFormat.getFilePath().isBlank());

        if (isSelectionMissing) {
            showAlert("Sin selección", "Seleccione un documento inicial de la tabla.",
                    Alert.AlertType.WARNING);
        } else if (hasNoFile) {
            showAlert("Sin documento",
                    "Este documento inicial aún no ha sido entregado por el practicante.",
                    Alert.AlertType.INFORMATION);
        } else {
            tryOpenFile(selectedFormat.getFilePath());
        }
    }

    @FXML
    public void openSelfEvaluation(ActionEvent actionEvent) {
        Intern selectedIntern = internComboBox.getSelectionModel().getSelectedItem();
        if (selectedIntern == null) {
            showAlert("Sin selección", "Seleccione un practicante.", Alert.AlertType.WARNING);
        } else {
            tryOpenSelfEvaluation(selectedIntern.getId());
        }
    }

    private void tryOpenSelfEvaluation(int internId) {
        try {
            SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();
            SelfEvaluation selfEvaluation = selfEvaluationDAO.findByIdIntern(internId);
            boolean hasDocument = selfEvaluation != null && selfEvaluation.getDocumentPath() != null
                    && !selfEvaluation.getDocumentPath().isBlank();
            if (!hasDocument) {
                showAlert("Sin autoevaluación",
                        "El practicante no tiene una autoevaluación entregada.",
                        Alert.AlertType.INFORMATION);
            } else {
                tryOpenFile(selfEvaluation.getDocumentPath());
            }
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la autoevaluación del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo recuperar la autoevaluación.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void openOVEvaluation(ActionEvent actionEvent) {
        Intern selectedIntern = internComboBox.getSelectionModel().getSelectedItem();
        Project selectedProject = projectComboBox.getValue();
        boolean isSelectionMissing = selectedIntern == null || selectedProject == null;
        if (isSelectionMissing) {
            showAlert("Sin selección", "Seleccione un proyecto y un practicante.",
                    Alert.AlertType.WARNING);
        } else {
            tryOpenOVEvaluation(selectedIntern.getId(), selectedProject.getIdProject());
        }
    }

    private void tryOpenOVEvaluation(int internId, int projectId) {
        try {
            OVEvaluationDAO ovEvaluationDAO = new OVEvaluationDAO();
            OVEvaluation ovEvaluation = ovEvaluationDAO.findByInternAndProject(internId, projectId);
            boolean hasDocument = ovEvaluation != null && ovEvaluation.getDocumentPath() != null
                    && !ovEvaluation.getDocumentPath().isBlank();
            if (!hasDocument) {
                showAlert("Sin evaluación OV",
                        "El practicante no tiene una evaluación OV entregada para este proyecto.",
                        Alert.AlertType.INFORMATION);
            } else {
                tryOpenFile(ovEvaluation.getDocumentPath());
            }
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la evaluación OV del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo recuperar la evaluación OV.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
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
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName(sourceFile.getName());

            File destination = fileChooser.showSaveDialog(markInReviewButton.getScene().getWindow());
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
    public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
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
            List<Intern> interns = internDAO.findByProject(project.getIdProject());
            internComboBox.setItems(FXCollections.observableArrayList(interns));
            internComboBox.setDisable(false);
            reportsTableView.getItems().clear();
            clearForm();
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar practicantes del proyecto {0}: {1}",
                    new Object[]{project.getIdProject(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los practicantes. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadReportsForIntern(Intern intern) {
        try {
            Project selectedProject = projectComboBox.getValue();
            ReportDAO reportDAO = new ReportDAO();
            List<Report> reportList = reportDAO.getByInternAndProject(intern.getId(), selectedProject.getIdProject());

            reportsTableView.setItems(FXCollections.observableArrayList(reportList));
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
        int reportId = selectedReport.getIdReport();
        Double reportGrade = parseGrade(gradeTextField.getText().trim());
        Intern currentIntern = internComboBox.getValue();
        Project currentProject = projectComboBox.getValue();
        try {
            ReportDAO reportDAO = new ReportDAO();
            String observations = observationsTextArea.getText().trim();
            Date reviewDate = Date.valueOf(LocalDate.now());

            String observationsToSave = null;
            boolean hasObservations = !observations.isEmpty();
            if (hasObservations) {
                observationsToSave = observations;
            }

            boolean isEvaluated = STATUS_EVALUATED.equals(newStatus);
            if (isEvaluated && reportGrade != null) {
                saveReportGrade(reportId, reportGrade);
            }

            ReportStatusUpdate statusUpdate = new ReportStatusUpdate(newStatus, observationsToSave, reviewDate);
            boolean updated = reportDAO.updateStatus(reportId, statusUpdate);

            if (updated) {
                LOGGER.log(Level.INFO,
                        "Auditoria: profesor {0} evaluo el reporte {1}, nuevo estado ''{2}'', calificacion {3}",
                        new Object[]{currentProfessorId, reportId, newStatus, String.valueOf(reportGrade)});
                String message = buildStatusMessage(newStatus);
                showAlert("Estado actualizado", message, Alert.AlertType.INFORMATION);
                if (currentIntern != null) {
                    loadReportsForIntern(currentIntern);
                }
                clearForm();
                boolean hasContext = currentIntern != null && currentProject != null;
                if (isEvaluated && hasContext) {
                    tryConcludePractice(currentIntern.getId(), currentProject.getIdProject());
                }
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
                    new Object[]{reportId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo actualizar el reporte. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void tryConcludePractice(int internId, int projectId)
            throws ServiceException, ValidationException {
        if (EvaluationPrerequisiteChecker.isPracticeComplete(internId, projectId)) {
            ReportDAO reportDAO = new ReportDAO();
            Double practiceGrade = reportDAO.getAveragePracticeGrade(internId);
            PracticeDAO practiceDAO = new PracticeDAO();
            boolean concluded = practiceDAO.concludeActiveByIntern(internId, practiceGrade);
            if (concluded) {
            AuditLog.record("concluyó la práctica del practicante " + internId + " con calificación " + formatGrade(practiceGrade));
                showAlert("Práctica concluida",
                        "El practicante cumplió todos los requisitos. La práctica fue concluida "
                        + "con calificación " + formatGrade(practiceGrade) + ".",
                        Alert.AlertType.INFORMATION);
            }
        }
    }

    private void saveReportGrade(int reportId, Double grade)
            throws ServiceException, ValidationException {
        ReportDAO reportDAO = new ReportDAO();
        reportDAO.updateGrade(reportId, grade, LocalDate.now());
    }

    private String formatGrade(Double grade) {
        String text = "no disponible";
        if (grade != null) {
            text = String.format("%.2f", grade);
        }
        return text;
    }

    private Double parseGrade(String gradeText) {
        Double grade = null;
        boolean hasValidFormat = gradeText != null && GRADE_PATTERN.matcher(gradeText).matches();
        if (hasValidFormat) {
            double value = Double.parseDouble(gradeText);
            boolean isInRange = value >= 0 && value <= 10;
            if (isInRange) {
                grade = value;
            }
        }
        return grade;
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
        boolean hasSignedPath = report.getSignedDocumentPath() != null && !report.getSignedDocumentPath().isBlank();
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
        gradeTextField.clear();
        activitiesTableView.getItems().clear();
        initialFormatsTableView.getItems().clear();
        reportsTableView.getSelectionModel().clearSelection();
    }
}
