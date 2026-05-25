package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.InternActivityDAO;
import Logic.DAO.ReportDAO;
import Logic.DAO.ReportObservationDAO;
import Logic.DTOs.InternActivity;
import Logic.DTOs.Report;
import Logic.DTOs.ReportObservation;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;

public class EvaluateReportController {

    private static final Logger LOGGER = Logger.getLogger(EvaluateReportController.class.getName());

    @FXML
    private TableView<Report> reportsTableView;

    @FXML
    private TableColumn<Report, String> internColumn;

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
    private TableView<InternActivity> activitiesTableView;

    @FXML
    private TableColumn<InternActivity, String> actNameColumn;

    @FXML
    private TableColumn<InternActivity, String> actHoursColumn;

    @FXML
    private TableColumn<InternActivity, String> actStatusColumn;

    @FXML
    private TableColumn<InternActivity, String> actObsColumn;

    @FXML
    private TextArea observationsTextArea;

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Button requestCorrectionButton;

    @FXML
    private Button markInReviewButton;

    private Report selectedReport;

    @FXML
    private void initialize() {
        configureListeners();
        loadProfessorReports();
    }

    @FXML
    public void approveReport(ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean isStatusInvalid = selectedReport != null
                && !"Entregado".equals(selectedReport.getStatus())
                && !"Pendiente".equals(selectedReport.getStatus());

        if (isReportMissing) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else if (isStatusInvalid) {
            showAlert("Estado inválido",
                    "Solo puede aprobar reportes en estado Pendiente o Entregado.",
                    Alert.AlertType.WARNING);
        } else {
            updateReportStatus("Aprobado");
        }
    }

    @FXML
    public void rejectReport(ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean isObservationEmpty = observationsTextArea.getText().isBlank();

        if (isReportMissing) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else if (isObservationEmpty) {
            showAlert("Observación requerida",
                    "Ingrese el motivo del rechazo en el campo de observaciones.",
                    Alert.AlertType.WARNING);
        } else {
            updateReportStatus("Rechazado");
        }
    }

    @FXML
    public void requestCorrection(ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean isObservationEmpty = observationsTextArea.getText().isBlank();

        if (isReportMissing) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else if (isObservationEmpty) {
            showAlert("Observación requerida",
                    "Ingrese las correcciones requeridas en el campo de observaciones.",
                    Alert.AlertType.WARNING);
        } else {
            updateReportStatus("Corrección solicitada");
        }
    }

    @FXML
    public void markInReview(ActionEvent actionEvent) {
        if (selectedReport == null) {
            showAlert("Sin selección", "Seleccione un reporte.", Alert.AlertType.WARNING);
        } else {
            updateReportStatus("En revisión");
        }
    }

    @FXML
    public void clearSelection(ActionEvent actionEvent) {
        clearForm();
    }

    private void configureListeners() {
        reportsTableView.getSelectionModel().selectedItemProperty()
                .addListener(new ReportSelectionListener());
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

    private void loadProfessorReports() {
        try {
            int professorId = SessionManager.getInstance().getUsuario().getId();
            ReportDAO reportDAO = new ReportDAO();
            List<Report> reports = reportDAO.getByIdProfessor(professorId);
            reportsTableView.setItems(FXCollections.observableArrayList(reports));
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar reportes del profesor: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los reportes. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void populateReportDetail(Report report) {
        String tardyIndicator = "";
        if (report.isEntregaTardia()) {
            tardyIndicator = "  ENTREGA TARDÍA";
        }

        reportDetailLabel.setText(
                "Tipo: " + report.getReportType()
                + "  |  Período: " + report.getPeriod()
                + "  |  Horas: " + report.getReportedHours()
                + "  |  Estado: " + report.getStatus()
                + tardyIndicator);

        String documentPathText = "—";
        if (report.getDocumentPath() != null) {
            documentPathText = report.getDocumentPath();
        }
        documentPathLabel.setText(documentPathText);

        String signedPathText = "—";
        if (report.getSignedDocumentPath() != null) {
            signedPathText = report.getSignedDocumentPath();
        }
        signedPathLabel.setText(signedPathText);

        if (report.getProfessorObservations() != null) {
            observationsTextArea.setText(report.getProfessorObservations());
        }
    }

    private void loadActivitiesForReport(Report report) {
        try {
            InternActivityDAO internActivityDAO = new InternActivityDAO();
            List<InternActivity> activities =
                    internActivityDAO.findByInternAndProject(
                            report.getIdIntern(), report.getIdProyect());
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
            java.sql.Date reviewDate = java.sql.Date.valueOf(LocalDate.now());

            String observationsToSave = null;
            if (!observations.isEmpty()) {
                observationsToSave = observations;
            }

            boolean updated = reportDAO.updateStatus(
                    selectedReport.getIdReport(), newStatus, observationsToSave, reviewDate);

            if (updated) {
                if (!observations.isEmpty()) {
                    saveObservationRecord(observations);
                }
                String message = buildStatusMessage(newStatus);
                showAlert("Estado actualizado", message, Alert.AlertType.INFORMATION);
                loadProfessorReports();
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

    private String buildStatusMessage(String status) {
        String message;

        switch (status) {
            case "Aprobado":
                message = "Reporte aprobado. Las horas han sido validadas.";
                break;
            case "Rechazado":
                message = "Reporte rechazado.";
                break;
            case "Corrección solicitada":
                message = "Se ha solicitado corrección al practicante.";
                break;
            case "En revisión":
                message = "El reporte ha sido marcado como en revisión.";
                break;
            default:
                message = "Estado actualizado a: " + status;
        }

        return message;
    }

    private void saveObservationRecord(String comment) {
        try {
            int professorId = SessionManager.getInstance().getUsuario().getId();
            ReportObservation observation = new ReportObservation();
            observation.setIdReport(selectedReport.getIdReport());
            observation.setIdProfessor(professorId);
            observation.setComment(comment);

            ReportObservationDAO observationDAO = new ReportObservationDAO();
            observationDAO.save(observation);
        } catch (ValidationException | ServiceException observationException) {
            LOGGER.log(Level.SEVERE, "Error al guardar observación del reporte: {0}",
                    observationException.getMessage());
        }
    }

    private void clearForm() {
        selectedReport = null;
        reportDetailLabel.setText("");
        documentPathLabel.setText("");
        signedPathLabel.setText("");
        observationsTextArea.clear();
        activitiesTableView.getItems().clear();
        reportsTableView.getSelectionModel().clearSelection();
    }

}
