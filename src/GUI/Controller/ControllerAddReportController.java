package GUI.Controller;

import GUI.SessionManager.SessionManager;
import GUI.Utils.AuditLog;
import Logic.DAO.ReportDAO;
import Logic.DTOs.Report;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ValidationUtils.isPDF;
import static GUI.Utils.ViewsUtils.openWelcomePage;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import java.util.Optional;

public class ControllerAddReportController implements EventHandler<DragEvent>, ChangeListener<Report> {
    private static final String STATUS_PENDING = "Pendiente";


    private static final Logger LOGGER =
            Logger.getLogger(ControllerAddReportController.class.getName());

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TableView<Report> reportsTableView;

    @FXML
    private TableColumn<Report, String> reportTypeColumn;

    @FXML
    private TableColumn<Report, String> periodColumn;

    @FXML
    private TableColumn<Report, String> statusColumn;

    @FXML
    private TableColumn<Report, String> dateColumn;

    @FXML
    private Pane dropZone;

    @FXML
    private Label labelFileName;

    @FXML
    private Label labelStatus;

    @FXML
    private Button uploadButton;

    private File selectedFile;
    private Report selectedReport;

    @FXML
    private void initialize() {
        configureListeners();
        loadPendingReports();
    }

    @FXML
    public void openFileChooser(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar PDF firmado");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(dropZone.getScene().getWindow());

        if (file != null) {
            processSelectedFile(file);
        }
    }

    @FXML
    public void uploadSignedReport(ActionEvent actionEvent) {
        boolean isReportMissing = selectedReport == null;
        boolean isFileMissing = selectedFile == null;
        boolean isStatusInvalid = selectedReport != null
                && !STATUS_PENDING.equals(selectedReport.getStatus());

        if (isReportMissing) {
            showStatus("Seleccione un reporte de la tabla.", true);
        } else if (isFileMissing) {
            showStatus("Seleccione el PDF firmado.", true);
        } else if (isStatusInvalid) {
            showStatus("Solo puede subir el documento firmado de reportes en estado Pendiente.", true);
        } else {
            if (isLateDelivery()) {
                showAlert("Entrega tardía",
                        "El tiempo límite de entrega ha terminado, su documento será aceptado "
                        + "con retardo y a consideración del coordinador/profesor.",
                        Alert.AlertType.WARNING);
            }
            uploadProcess();
        }
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        Optional<ButtonType> response = showAlertAndWait("Confirmar cancelación",
                "¿Desea salir? Los datos ingresados no se guardarán.",
                Alert.AlertType.CONFIRMATION);
        boolean isConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isConfirmed) {
            clearSelection();
            openWelcomePage(anchorPane);
        }
    }

    private boolean isLateDelivery() {
        boolean isLate = selectedReport.getDeadline() != null
                && LocalDate.now().isAfter(selectedReport.getDeadline());
        return isLate;
    }

    private void configureListeners() {
        reportsTableView.getSelectionModel().selectedItemProperty().addListener(this);
        dropZone.setOnDragOver(this);
        dropZone.setOnDragDropped(this);
    }

    @Override
    public void changed(ObservableValue<? extends Report> observable,
                        Report oldValue, Report newValue) {
        if (newValue != null) {
            selectedReport = newValue;
            String selectionStatusText = "Reporte seleccionado: " + newValue.getTypeWithMonth() + " - " + newValue.getPeriod();
            showStatus(selectionStatusText, false);
        }
    }

    @Override
    public void handle(DragEvent event) {
        if (event.getEventType() == DragEvent.DRAG_OVER) {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        } else if (event.getEventType() == DragEvent.DRAG_DROPPED) {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                processSelectedFile(dragboard.getFiles().get(0));
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        }
        event.consume();
    }

    private void loadPendingReports() {
        try {
            int internId = SessionManager.getInstance().getUser().getId();
            ReportDAO reportDAO = new ReportDAO();
            List<Report> allReports = reportDAO.getByIdInternWithMonth(internId);
            List<Report> pendingReports = new ArrayList<>();

            for (Report report : allReports) {
                boolean isPending = STATUS_PENDING.equals(report.getStatus());
                if (isPending) {
                    pendingReports.add(report);
                }
            }

            reportsTableView.setItems(FXCollections.observableArrayList(pendingReports));
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar reportes del practicante: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los reportes. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void processSelectedFile(File file) {
        if (!isPDF(file)) {
            showStatus("El archivo seleccionado no es un PDF válido.", true);
            selectedFile = null;
        } else {
            selectedFile = file;
            labelFileName.setText(file.getName());
            showStatus("Archivo PDF válido seleccionado.", false);
        }
    }

    private void uploadProcess() {
        try {
            String signedPath = copySignedFile();
            ReportDAO reportDAO = new ReportDAO();

            if (reportDAO.updateSignedDocumentPath(selectedReport.getIdReport(), signedPath)) {
                if (isLateDelivery()) {
                    reportDAO.markLateDelivery(selectedReport.getIdReport());
                }
                AuditLog.record("subió el documento firmado del reporte " + selectedReport.getIdReport());
                showAlert("Documento firmado subido",
                        "El PDF firmado fue registrado correctamente.",
                        Alert.AlertType.INFORMATION);
                loadPendingReports();
                clearSelection();
            } else {
                showAlert("Error",
                        "No se pudo registrar el documento firmado.",
                        Alert.AlertType.ERROR);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al guardar documento firmado del reporte {0}: {1}",
                    new Object[]{selectedReport.getIdReport(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo guardar el documento. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al copiar archivo firmado: {0}",
                    ioException.getMessage());
            showAlert("Error de archivo",
                    "No se pudo copiar el archivo firmado al almacenamiento.",
                    Alert.AlertType.ERROR);
        }
    }

    private String copySignedFile() throws IOException {
        String internRegistrationNumber = SessionManager.getInstance().getUser().getRegistrationNumber();
        String folder = "storage/intern_" + internRegistrationNumber + "/project_" + selectedReport.getIdProject()
                + "/reportes_generados";
        String fileName = "reporte_" + selectedReport.getReportType().toLowerCase()
                + "_" + selectedReport.getIdReport() + "_firmado";

        Path folderPath = Paths.get(folder);
        Files.createDirectories(folderPath);

        Path destination = folderPath.resolve(fileName + ".pdf");
        Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        String destinationPath = destination.toString();
        return destinationPath;
    }

    private void showStatus(String message, boolean isError) {
        String textFillStyle = "-fx-text-fill: green;";
        if (isError) {
            textFillStyle = "-fx-text-fill: red;";
        }
        labelStatus.setStyle(textFillStyle);
        labelStatus.setText(message);
    }

    @FXML
    private void clearSelection() {
        selectedFile = null;
        selectedReport = null;
        labelFileName.setText("Ningún archivo seleccionado");
        labelStatus.setText("");
        reportsTableView.getSelectionModel().clearSelection();
    }

}
