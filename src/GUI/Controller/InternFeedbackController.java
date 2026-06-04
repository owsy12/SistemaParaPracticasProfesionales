package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.ReportDAO;
import Logic.DTOs.Assignment;
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

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Label assignmentReasonLabel;

    @FXML
    private TableView<Report> reportsTableView;

    @FXML
    private TableColumn<Report, String> typeColumn;

    @FXML
    private TableColumn<Report, String> periodColumn;

    @FXML
    private TableColumn<Report, String> statusColumn;

    @FXML
    private TableColumn<Report, String> observationsColumn;

    @FXML
    private void initialize() {
        loadFeedback();
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        openWelcomePage(anchorPane);
    }

    private void loadFeedback() {
        boolean hasSession = SessionManager.getInstance().getUsuario() != null;
        if (!hasSession) {
            showAlert("Sesión inválida", "No hay una sesión activa.", Alert.AlertType.WARNING);
            openWelcomePage(anchorPane);
        } else {
            int internId = SessionManager.getInstance().getUsuario().getId();
            loadAssignmentReason(internId);
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

    private void loadReportFeedback(int internId) {
        try {
            ReportDAO reportDAO = new ReportDAO();
            List<Report> reports = reportDAO.getByIdIntern(internId);
            reportsTableView.setItems(FXCollections.observableArrayList(reports));
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar la retroalimentación de reportes: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudieron cargar las observaciones de los reportes.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }
}
