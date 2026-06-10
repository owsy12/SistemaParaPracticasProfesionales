package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ReportDAO;
import Logic.DTOs.Assignment;
import Logic.DTOs.InternReportFeedback;
import Logic.DTOs.Practice;
import Logic.DTOs.Report;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class InternFeedbackController {

    private static final Logger LOGGER = Logger.getLogger(InternFeedbackController.class.getName());
    private static final String NO_ACTIVE_ASSIGNMENT =
            "No tienes una asignación de proyecto registrada.";
    private static final String NO_ASSIGNMENT_REASON =
            "Fuiste asignado a un proyecto que seleccionaste; no se registró un motivo especial.";
    private static final String NO_FINAL_GRADE =
            "La práctica aún no ha concluido; todavía no hay una calificación final registrada.";
    private static final String NOT_GRADED = "Sin calificar";
    private static final String NO_OBSERVATIONS = "Sin observaciones";
    private static final String NO_DATE = "—";

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Label assignmentReasonLabel;

    @FXML
    private Label finalGradeLabel;

    @FXML
    private TableView<InternReportFeedback> reportsTableView;

    @FXML
    private TableColumn<InternReportFeedback, String> typeColumn;

    @FXML
    private TableColumn<InternReportFeedback, String> periodColumn;

    @FXML
    private TableColumn<InternReportFeedback, String> statusColumn;

    @FXML
    private TableColumn<InternReportFeedback, String> gradeColumn;

    @FXML
    private TableColumn<InternReportFeedback, String> evaluationDateColumn;

    @FXML
    private TableColumn<InternReportFeedback, String> observationsColumn;

    @FXML
    private void initialize() {
        loadFeedback();
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        openWelcomePage(anchorPane);
    }

    @FXML
    public void viewReport(ActionEvent actionEvent) {
        InternReportFeedback selected = reportsTableView.getSelectionModel().getSelectedItem();
        boolean hasSelection = selected != null;
        boolean hasDocument = hasSelection && selected.getDocumentPath() != null
                && !selected.getDocumentPath().isBlank();
        if (!hasSelection) {
            showAlert("Selecciona un reporte",
                    "Selecciona un reporte de la tabla para verlo.", Alert.AlertType.WARNING);
        } else if (!hasDocument) {
            showAlert("Sin documento",
                    "El reporte seleccionado no tiene un documento disponible.",
                    Alert.AlertType.WARNING);
        } else {
            tryOpenFile(selected.getDocumentPath());
        }
    }

    @FXML
    public void downloadReport(ActionEvent actionEvent) {
        InternReportFeedback selected = reportsTableView.getSelectionModel().getSelectedItem();
        boolean hasSelection = selected != null;
        boolean hasDocument = hasSelection && selected.getDocumentPath() != null
                && !selected.getDocumentPath().isBlank();
        if (!hasSelection) {
            showAlert("Selecciona un reporte",
                    "Selecciona un reporte de la tabla para descargarlo.", Alert.AlertType.WARNING);
        } else if (!hasDocument) {
            showAlert("Sin documento",
                    "El reporte seleccionado no tiene un documento disponible.",
                    Alert.AlertType.WARNING);
        } else {
            trySaveFileCopy(selected.getDocumentPath());
        }
    }

    @FXML
    public void viewSignedDocument(ActionEvent actionEvent) {
        InternReportFeedback selected = reportsTableView.getSelectionModel().getSelectedItem();
        boolean hasSelection = selected != null;
        boolean hasSigned = hasSelection && selected.getSignedDocumentPath() != null
                && !selected.getSignedDocumentPath().isBlank();
        if (!hasSelection) {
            showAlert("Selecciona un reporte",
                    "Selecciona un reporte de la tabla para ver su documento firmado.",
                    Alert.AlertType.WARNING);
        } else if (!hasSigned) {
            showAlert("Sin documento firmado",
                    "El reporte seleccionado no tiene un documento firmado asociado.",
                    Alert.AlertType.WARNING);
        } else {
            tryOpenFile(selected.getSignedDocumentPath());
        }
    }

    private void loadFeedback() {
        boolean hasSession = SessionManager.getInstance().getUser() != null;
        if (!hasSession) {
            showAlert("Sesión inválida", "No hay una sesión activa.", Alert.AlertType.WARNING);
            openWelcomePage(anchorPane);
        } else {
            int internId = SessionManager.getInstance().getUser().getId();
            loadAssignmentReason(internId);
            loadFinalGrade(internId);
            loadReportFeedback(internId);
        }
    }

    private void loadAssignmentReason(int internId) {
        try {
            AssignmentDAO assignmentDAO = new AssignmentDAO();
            Assignment assignment = assignmentDAO.getActiveByIdIntern(internId);
            String reasonText = NO_ACTIVE_ASSIGNMENT;
            if (assignment != null) {
                boolean hasReason = assignment.getAssignmentReason() != null
                        && !assignment.getAssignmentReason().isBlank();
                if (hasReason) {
                    reasonText = assignment.getAssignmentReason();
                } else {
                    reasonText = NO_ASSIGNMENT_REASON;
                }
            }
            assignmentReasonLabel.setText(reasonText);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar el motivo de asignación: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudo cargar el motivo de asignación.", Alert.AlertType.ERROR);
        }
    }

    private void loadFinalGrade(int internId) {
        try {
            PracticeDAO practiceDAO = new PracticeDAO();
            List<Practice> practices = practiceDAO.findByIntern(internId);
            Double finalGrade = null;
            for (Practice practice : practices) {
                boolean hasGrade = practice.getGrade() != null;
                if (hasGrade) {
                    finalGrade = practice.getGrade();
                }
            }
            String gradeText = NO_FINAL_GRADE;
            if (finalGrade != null) {
                gradeText = "Calificación final: " + formatGrade(finalGrade);
            }
            finalGradeLabel.setText(gradeText);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar la calificación final: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudo cargar la calificación final de la práctica.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadReportFeedback(int internId) {
        try {
            ReportDAO reportDAO = new ReportDAO();
            List<Report> reports = reportDAO.getByIdInternWithMonth(internId);
            List<InternReportFeedback> rows = new ArrayList<InternReportFeedback>();
            for (Report report : reports) {
                rows.add(buildFeedbackRow(report));
            }
            reportsTableView.setItems(FXCollections.observableArrayList(rows));
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar la retroalimentación de reportes: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudieron cargar las observaciones de los reportes.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private InternReportFeedback buildFeedbackRow(Report report) {
        String gradeText = NOT_GRADED;
        boolean hasGrade = report.getGrade() != null;
        if (hasGrade) {
            gradeText = formatGrade(report.getGrade());
        }
        String evaluationDateText = formatDate(report.getEvaluationDate());
        String observationsText = NO_OBSERVATIONS;
        boolean hasObservations = report.getProfessorObservations() != null
                && !report.getProfessorObservations().isBlank();
        if (hasObservations) {
            observationsText = report.getProfessorObservations();
        }
        InternReportFeedback row = new InternReportFeedback();
        row.setIdReport(report.getIdReport());
        row.setType(report.getTypeWithMonth());
        row.setPeriod(report.getPeriod());
        row.setStatus(report.getDisplayStatus());
        row.setGrade(gradeText);
        row.setEvaluationDate(evaluationDateText);
        row.setObservations(observationsText);
        row.setDocumentPath(report.getDocumentPath());
        row.setSignedDocumentPath(report.getSignedDocumentPath());
        return row;
    }

    private String formatGrade(double grade) {
        String formatted = String.format("%.2f", grade);
        return formatted;
    }

    private String formatDate(LocalDate date) {
        String formatted = NO_DATE;
        if (date != null) {
            formatted = date.toString();
        }
        return formatted;
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
                showAlert("Error al abrir",
                        "No se pudo abrir el documento con el visor predeterminado.",
                        Alert.AlertType.ERROR);
            }
        }
    }

    private void trySaveFileCopy(String filePath) {
        File sourceFile = new File(filePath);
        boolean fileExists = sourceFile.exists();
        if (!fileExists) {
            showAlert("Archivo no encontrado",
                    "El documento no fue encontrado en: " + filePath, Alert.AlertType.WARNING);
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar copia del reporte");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName(sourceFile.getName());
            File destination = fileChooser.showSaveDialog(reportsTableView.getScene().getWindow());
            boolean destinationSelected = destination != null;
            if (destinationSelected) {
                try {
                    Files.copy(sourceFile.toPath(), destination.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    showAlert("Guardado",
                            "Documento guardado correctamente en: " + destination.getAbsolutePath(),
                            Alert.AlertType.INFORMATION);
                } catch (IOException ioException) {
                    LOGGER.log(Level.SEVERE, "Error al guardar copia: {0}", ioException.getMessage());
                    showAlert("Error al guardar",
                            "No se pudo guardar la copia del documento.", Alert.AlertType.ERROR);
                }
            }
        }
    }
}
