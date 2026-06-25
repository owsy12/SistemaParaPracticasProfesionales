package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.InternDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ReportDAO;
import Logic.DTOs.Intern;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class ValidateClosureRecordsController {

    private static final Logger LOGGER =
            Logger.getLogger(ValidateClosureRecordsController.class.getName());

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TableView<Intern> internsTableView;

    @FXML
    private TableColumn<Intern, String> registrationNumberColumn;

    @FXML
    private TableColumn<Intern, String> fullNameColumn;

    @FXML
    private void initialize() {
        loadPendingClosures();
    }

    @FXML
    public void viewClosureRecord(ActionEvent actionEvent) {
        Intern selectedIntern = internsTableView.getSelectionModel().getSelectedItem();
        if (selectedIntern == null) {
            showAlert("Sin selección",
                    "Seleccione un practicante de la tabla.", Alert.AlertType.WARNING);
        } else {
            tryOpenClosureRecord(selectedIntern.getId());
        }
    }

    @FXML
    public void validateClosureRecord(ActionEvent actionEvent) {
        Intern selectedIntern = internsTableView.getSelectionModel().getSelectedItem();
        if (selectedIntern == null) {
            showAlert("Sin selección",
                    "Seleccione un practicante de la tabla.", Alert.AlertType.WARNING);
        } else {
            confirmAndValidate(selectedIntern);
        }
    }

    @FXML
    public void cancelOperation(ActionEvent actionEvent) {
        openWelcomePage(anchorPane);
    }

    private void confirmAndValidate(Intern intern) {
        Optional<ButtonType> confirmationResponse = showAlertAndWait("Validar acta de cierre",
                "¿Validar el acta de cierre de " + intern.getFullName()
                + "? Esto concluirá su práctica.",
                Alert.AlertType.CONFIRMATION);
        boolean isConfirmed = confirmationResponse.isPresent()
                && confirmationResponse.get() == ButtonType.OK;
        if (isConfirmed) {
            validateProcess(intern);
        }
    }

    private void validateProcess(Intern intern) {
        try {
            PracticeDAO practiceDAO = new PracticeDAO();
            String closureRecordPath = practiceDAO.findClosureRecordPath(intern.getId());
            boolean hasClosureRecord = closureRecordPath != null && !closureRecordPath.isBlank();

            if (!hasClosureRecord) {
                showAlert("Sin acta",
                        "El practicante ya no tiene un acta de cierre pendiente de validación.",
                        Alert.AlertType.INFORMATION);
                loadPendingClosures();
            } else {
                ReportDAO reportDAO = new ReportDAO();
                Double practiceGrade = reportDAO.getAveragePracticeGrade(intern.getId());
                boolean concluded = practiceDAO.concludeWithClosureRecord(
                        intern.getId(), closureRecordPath, practiceGrade);
                if (concluded) {
                    LOGGER.log(Level.INFO,
                            "Usuario {0} validó el acta de cierre del practicante {1}; práctica concluida",
                            new Object[]{SessionManager.getInstance().getUser().getId(),
                                    intern.getId()});
                    showAlert("Práctica concluida",
                            "El acta de cierre fue validada y la práctica fue marcada como Concluida.",
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Error",
                            "No se pudo validar el acta de cierre. Intente nuevamente.",
                            Alert.AlertType.ERROR);
                }
                loadPendingClosures();
            }
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al validar el acta de cierre del practicante {0}: {1}",
                    new Object[]{intern.getId(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo validar el acta de cierre. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void tryOpenClosureRecord(int internId) {
        try {
            PracticeDAO practiceDAO = new PracticeDAO();
            String closureRecordPath = practiceDAO.findClosureRecordPath(internId);
            boolean hasClosureRecord = closureRecordPath != null && !closureRecordPath.isBlank();
            if (!hasClosureRecord) {
                showAlert("Sin acta",
                        "El practicante no tiene un acta de cierre registrada.",
                        Alert.AlertType.INFORMATION);
            } else {
                tryOpenFile(closureRecordPath);
            }
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al recuperar el acta de cierre del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo recuperar el acta de cierre.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void tryOpenFile(String filePath) {
        File file = new File(filePath);
        boolean fileExists = file.exists();
        if (!fileExists) {
            showAlert("Archivo no encontrado",
                    "El documento no fue encontrado en: " + filePath, Alert.AlertType.WARNING);
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

    private void loadPendingClosures() {
        try {
            InternDAO internDAO = new InternDAO();
            List<Intern> pendingInterns = internDAO.findWithPendingClosureValidation();
            internsTableView.getItems().setAll(pendingInterns);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al cargar actas de cierre pendientes: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudieron cargar las actas de cierre pendientes.",
                    Alert.AlertType.ERROR);
        }
    }
}
