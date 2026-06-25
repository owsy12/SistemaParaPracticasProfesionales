package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.InitialFormatDAO;
import Logic.DAO.OVEvaluationDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ReportActivityDAO;
import Logic.DAO.ReportDAO;
import Logic.DAO.SelfEvaluationDAO;
import Logic.DTOs.EducationalExperience;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.applyTextAreaRestriction;
import static GUI.Utils.ViewsUtils.findContentPane;
import static GUI.Utils.ViewsUtils.openWelcomePage;
import static GUI.Utils.ViewsUtils.wrapInScrollableContent;

public class EvaluateReportController implements ChangeListener<Report> {
    private static final String STATUS_APPROVED = "Aprobado";
    private static final String STATUS_EVALUATED = "Evaluado";
    private static final String STATUS_REJECTED = "Rechazado";
    private static final String STATUS_DOCUMENT_EVALUATED = "Evaluada";
    private static final java.util.regex.Pattern GRADE_PATTERN =
            java.util.regex.Pattern.compile("^(?:10|[0-9])(?:\\.[0-9]{1,2})?$");
    private static final java.util.regex.Pattern GRADE_INPUT_PATTERN =
            java.util.regex.Pattern.compile("^(?:10|[0-9])?(?:\\.[0-9]{0,2})?$");
    private static final int GRADE_MAX_LENGTH = 5;


    private static final Logger LOGGER = Logger.getLogger(EvaluateReportController.class.getName());

    @FXML
    private AnchorPane anchorPane;

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

    @FXML
    private Label experienceContextLabel;

    @FXML
    private Label projectContextLabel;

    @FXML
    private Label internContextLabel;

    @FXML
    private Label selfEvaluationStatusLabel;

    @FXML
    private Label ovEvaluationStatusLabel;

    private Report selectedReport;
    private EducationalExperience currentExperience;
    private Project currentProject;
    private Intern currentIntern;
    private int currentProfessorId;

    @FXML
    private void initialize() {
        currentProfessorId = SessionManager.getInstance().getUser().getId();
        applyTextAreaRestriction(observationsTextArea, 200);
        gradeTextField.setRestriction(GRADE_MAX_LENGTH, GRADE_INPUT_PATTERN);
        configureListeners();
    }

    public void setReviewContext(EducationalExperience experience, Project project, Intern intern) {
        currentExperience = experience;
        currentProject = project;
        currentIntern = intern;
        experienceContextLabel.setText("EE: " + experience.toString());
        projectContextLabel.setText("Proyecto: " + project.getName());
        internContextLabel.setText("Practicante: " + intern.getFullName());
        loadReportsForIntern(intern);
        loadInitialFormatsForIntern(intern);
        refreshSelfEvaluationStatus();
        refreshOVEvaluationStatus();
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
    public void rejectReport(ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean isStatusInvalid = selectedReport != null
                && !"En revision".equals(selectedReport.getStatus());
        boolean isObservationEmpty = observationsTextArea.getText().isBlank();

        if (isReportMissing) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else if (isStatusInvalid) {
            showAlert("Estado inválido",
                    "Solo puede rechazar reportes en estado En revision.",
                    Alert.AlertType.WARNING);
        } else if (isObservationEmpty) {
            showAlert("Motivo requerido",
                    "Debe indicar en las observaciones el motivo del rechazo.",
                    Alert.AlertType.WARNING);
        } else {
            updateReportStatus(STATUS_REJECTED);
        }
    }

    @FXML
    public void clearSelection(ActionEvent actionEvent) {
        clearForm();
    }

    @FXML
    public void goHome(ActionEvent actionEvent) {
        openWelcomePage(anchorPane);
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/View/GUISelectionInternProject.fxml"));
            Parent view = loader.load();

            SelectionInternProjectController controller = loader.getController();
            controller.setReviewContext(currentExperience);

            Pane contentPane = findContentPane(reportsTableView);
            if (contentPane == null) {
                showAlert("Error de navegación",
                        "No se pudo regresar a la lista de practicantes.", Alert.AlertType.ERROR);
            } else {
                contentPane.getChildren().setAll(wrapInScrollableContent(view));
            }
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al regresar a la lista de practicantes: {0}",
                    ioException.getMessage());
            showAlert("Error de navegación",
                    "No se pudo regresar a la lista de practicantes.", Alert.AlertType.ERROR);
        }
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
        Intern selectedIntern = currentIntern;
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
        Intern selectedIntern = currentIntern;
        Project selectedProject = currentProject;
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

    @FXML
    public void evaluateSelfEvaluation(ActionEvent actionEvent) {
        Intern selectedIntern = currentIntern;
        if (selectedIntern == null) {
            showAlert("Sin selección", "Seleccione un practicante.", Alert.AlertType.WARNING);
        } else {
            tryEvaluateSelfEvaluation(selectedIntern.getId());
        }
    }

    private void tryEvaluateSelfEvaluation(int internId) {
        try {
            SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();
            SelfEvaluation selfEvaluation = selfEvaluationDAO.findByIdIntern(internId);
            boolean isMissing = selfEvaluation == null
                    || selfEvaluation.getDocumentPath() == null
                    || selfEvaluation.getDocumentPath().isBlank();
            boolean isAlreadyEvaluated = !isMissing
                    && STATUS_DOCUMENT_EVALUATED.equals(selfEvaluation.getStatus());

            if (isMissing) {
                showAlert("Sin autoevaluación",
                        "El practicante no tiene una autoevaluación entregada.",
                        Alert.AlertType.INFORMATION);
            } else if (isAlreadyEvaluated) {
                showAlert("Autoevaluación ya evaluada",
                        "La autoevaluación del practicante ya fue marcada como evaluada.",
                        Alert.AlertType.INFORMATION);
            } else {
                selfEvaluationDAO.updateStatus(
                        selfEvaluation.getIdSelfEvalation(), STATUS_DOCUMENT_EVALUATED);
                LOGGER.log(Level.INFO,
                        "Usuario {0} marcó como evaluada la autoevaluación del practicante {1}",
                        new Object[]{currentProfessorId, internId});
                showAlert("Autoevaluación evaluada",
                        "La autoevaluación fue marcada como evaluada.",
                        Alert.AlertType.INFORMATION);
                refreshSelfEvaluationStatus();
            }
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al evaluar la autoevaluación del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo evaluar la autoevaluación.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void evaluateOVEvaluation(ActionEvent actionEvent) {
        Intern selectedIntern = currentIntern;
        Project selectedProject = currentProject;
        boolean isSelectionMissing = selectedIntern == null || selectedProject == null;
        if (isSelectionMissing) {
            showAlert("Sin selección", "Seleccione un proyecto y un practicante.",
                    Alert.AlertType.WARNING);
        } else {
            tryEvaluateOVEvaluation(selectedIntern.getId(), selectedProject.getIdProject());
        }
    }

    private void tryEvaluateOVEvaluation(int internId, int projectId) {
        try {
            OVEvaluationDAO ovEvaluationDAO = new OVEvaluationDAO();
            OVEvaluation ovEvaluation = ovEvaluationDAO.findByInternAndProject(internId, projectId);
            boolean isMissing = ovEvaluation == null
                    || ovEvaluation.getDocumentPath() == null
                    || ovEvaluation.getDocumentPath().isBlank();
            boolean isAlreadyEvaluated = !isMissing
                    && STATUS_DOCUMENT_EVALUATED.equals(ovEvaluation.getStatus());

            if (isMissing) {
                showAlert("Sin evaluación OV",
                        "El practicante no tiene una evaluación OV entregada para este proyecto.",
                        Alert.AlertType.INFORMATION);
            } else if (isAlreadyEvaluated) {
                showAlert("Evaluación OV ya evaluada",
                        "La evaluación OV del practicante ya fue marcada como evaluada.",
                        Alert.AlertType.INFORMATION);
            } else {
                ovEvaluationDAO.updateStatus(
                        ovEvaluation.getIdOVEvaluation(), STATUS_DOCUMENT_EVALUATED);
                LOGGER.log(Level.INFO,
                        "Usuario {0} marcó como evaluada la evaluación OV del practicante {1}",
                        new Object[]{currentProfessorId, internId});
                showAlert("Evaluación OV evaluada",
                        "La evaluación OV fue marcada como evaluada.",
                        Alert.AlertType.INFORMATION);
                refreshOVEvaluationStatus();
            }
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al evaluar la evaluación OV del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo evaluar la evaluación OV.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshSelfEvaluationStatus() {
        String statusText = "—";
        try {
            SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();
            SelfEvaluation selfEvaluation = selfEvaluationDAO.findByIdIntern(currentIntern.getId());
            String documentPath = null;
            String status = null;
            if (selfEvaluation != null) {
                documentPath = selfEvaluation.getDocumentPath();
                status = selfEvaluation.getStatus();
            }
            statusText = describeDocumentStatus(documentPath, status);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al consultar el estado de la autoevaluación: {0}",
                    serviceException.getMessage());
        } catch (ValidationException validationException) {
            LOGGER.log(Level.SEVERE,
                    "Error de validación al consultar la autoevaluación: {0}",
                    validationException.getMessage());
        }
        selfEvaluationStatusLabel.setText(statusText);
    }

    private void refreshOVEvaluationStatus() {
        String statusText = "—";
        try {
            OVEvaluationDAO ovEvaluationDAO = new OVEvaluationDAO();
            OVEvaluation ovEvaluation = ovEvaluationDAO.findByInternAndProject(
                    currentIntern.getId(), currentProject.getIdProject());
            String documentPath = null;
            String status = null;
            if (ovEvaluation != null) {
                documentPath = ovEvaluation.getDocumentPath();
                status = ovEvaluation.getStatus();
            }
            statusText = describeDocumentStatus(documentPath, status);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al consultar el estado de la evaluación OV: {0}",
                    serviceException.getMessage());
        } catch (ValidationException validationException) {
            LOGGER.log(Level.SEVERE,
                    "Error de validación al consultar la evaluación OV: {0}",
                    validationException.getMessage());
        }
        ovEvaluationStatusLabel.setText(statusText);
    }

    private String describeDocumentStatus(String documentPath, String status) {
        String description;
        boolean isDelivered = documentPath != null && !documentPath.isBlank();
        if (!isDelivered) {
            description = "No entregada";
        } else if (STATUS_DOCUMENT_EVALUATED.equals(status)) {
            description = "Evaluada";
        } else {
            description = "Entregada (por evaluar)";
        }
        return description;
    }

    @FXML
    public void openClosureRecord(ActionEvent actionEvent) {
        Intern selectedIntern = currentIntern;
        if (selectedIntern == null) {
            showAlert("Sin selección", "Seleccione un practicante.", Alert.AlertType.WARNING);
        } else {
            tryOpenClosureRecord(selectedIntern.getId());
        }
    }

    private void tryOpenClosureRecord(int internId) {
        try {
            PracticeDAO practiceDAO = new PracticeDAO();
            String documentPath = practiceDAO.findClosureRecordPath(internId);
            boolean hasDocument = documentPath != null && !documentPath.isBlank();
            if (!hasDocument) {
                showAlert("Sin acta de cierre",
                        "El practicante aún no ha subido el acta de cierre.",
                        Alert.AlertType.INFORMATION);
            } else {
                tryOpenFile(documentPath);
            }
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar el acta de cierre del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo recuperar el acta de cierre.", Alert.AlertType.ERROR);
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
        reportsTableView.getSelectionModel().selectedItemProperty().addListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends Report> observable, Report oldValue, Report newValue) {
        if (newValue != null) {
            selectedReport = newValue;
            populateReportDetail(newValue);
            loadActivitiesForReport(newValue);
        }
    }

    private void loadReportsForIntern(Intern intern) {
        try {
            ReportDAO reportDAO = new ReportDAO();
            List<Report> reportList = reportDAO.getByInternAndProject(intern.getId(), currentProject.getIdProject());

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
                        "Usuario {0} evaluó el reporte {1} con estado {2} y calificación {3}",
                        new Object[]{currentProfessorId, reportId, newStatus, String.valueOf(reportGrade)});
                String message = buildStatusMessage(newStatus);
                showAlert("Estado actualizado", message, Alert.AlertType.INFORMATION);
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
                    new Object[]{reportId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo actualizar el reporte. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void saveReportGrade(int reportId, Double grade)
            throws ServiceException, ValidationException {
        ReportDAO reportDAO = new ReportDAO();
        reportDAO.updateGrade(reportId, grade, LocalDate.now());
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
            case STATUS_REJECTED:
                message = "Reporte rechazado. El practicante podrá subir nuevamente el documento.";
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
        reportsTableView.getSelectionModel().clearSelection();
    }
}
