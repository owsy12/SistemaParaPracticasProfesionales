package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.InitialFormatDAO;
import Logic.DTOs.InitialFormat;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.isPDF;
import static GUI.Utils.ViewsUtils.openWelcomePage;
import static GUI.DocumentGeneration.DocumentStorage.saveFile;

public class UploadInitialDocumentsController implements EventHandler<DragEvent> {

    private static final Logger LOGGER = Logger.getLogger(UploadInitialDocumentsController.class.getName());
    private static final String STATUS_ERROR_STYLE_CLASS = "statusErrorLabel";
    private static final String STATUS_SUCCESS_STYLE_CLASS = "statusSuccessLabel";

    public AnchorPane anchorPane;

    private List<InitialFormat> pendingDocuments;

    @FXML
    private ComboBox<String> comboBoxDocumentType;

    @FXML
    private Pane dropZone;

    @FXML
    private Label labelFileName;

    @FXML
    private Label labelStatus;

    private File selectedFile;

    @FXML
    public void initialize() {
        validatePendingInitialDocuments();
    }

    private void validatePendingInitialDocuments() {
        try {
            InitialFormatDAO initialFormatDAO = new InitialFormatDAO();
            int currentUserId = SessionManager.getInstance().getUser().getIdUser();
            List<InitialFormat> obtainedPendingDocuments = initialFormatDAO.findPendingByIntern(currentUserId);

            if (obtainedPendingDocuments.isEmpty()) {
                showAlert("Advertencia",
                        "No cuenta con documentos pendientes por subir, será redirigido a la página de bienvenida",
                        Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);
            } else {
                pendingDocuments = obtainedPendingDocuments;
                loadDocumentTypes();
                configureDragAndDrop();
                clearSelectedFile();
            }

        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logró cargar la información, intente más tarde",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "Error al verificar documentos pendientes", Alert.AlertType.ERROR);
        }
    }

    private void loadDocumentTypes() {
        comboBoxDocumentType.getItems().clear();

        for (InitialFormat initialFormat : pendingDocuments) {
            String documentType = initialFormat.getFormatType();
            boolean isNotNull = documentType != null;
            boolean isNotDuplicate = !comboBoxDocumentType.getItems().contains(documentType);

            if (isNotNull && isNotDuplicate) {
                comboBoxDocumentType.getItems().add(documentType);
            }
        }
    }

    private void configureDragAndDrop() {
        dropZone.setOnDragOver(this);
        dropZone.setOnDragDropped(this);
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

    @FXML
    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(comboBoxDocumentType.getScene().getWindow());

        if (file != null) {
            processSelectedFile(file);
        }
    }

    private void processSelectedFile(File file) {
        if (!isPDF(file)) {
            showError("El archivo seleccionado no es un PDF válido");
        } else {
            selectedFile = file;
            labelFileName.setText(file.getName());
            labelStatus.setText("Archivo válido");
        }
    }

    @FXML
    private void uploadDocument() {
        String errorMessage = null;

        boolean isDocumentTypeMissing = comboBoxDocumentType.getValue() == null;
        boolean isFileMissing = selectedFile == null;

        if (isDocumentTypeMissing) {
            errorMessage = "Seleccione un tipo de documento";
        } else if (isFileMissing) {
            errorMessage = "Seleccione un archivo PDF";
        }

        if (errorMessage != null) {
            showError(errorMessage);
        } else {
            boolean wasSaved = saveDocumentProcess();
            if (wasSaved) {
                clearSelectedFile();
                comboBoxDocumentType.getSelectionModel().clearSelection();
                if (pendingDocuments.isEmpty()) {
                    showAlert("Éxito",
                            "Ha subido todos los documentos iniciales requeridos. Será redirigido.",
                            Alert.AlertType.INFORMATION);
                    openWelcomePage(anchorPane);
                } else {
                    showInfo("Documento guardado. Documentos pendientes: " + pendingDocuments.size());
                }
            }
        }
    }

    private boolean saveDocumentProcess() {
        boolean wasSaved = false;
        try {
            String selectedType = comboBoxDocumentType.getValue();
            InitialFormat targetDocument = findPendingByType(selectedType);
            if (targetDocument == null) {
                showError("El documento seleccionado ya no está pendiente.");
            } else {
                String registrationNumber = SessionManager.getInstance().getUser().getRegistrationNumber();
                int idProject = targetDocument.getIdProject();
                String relativeFolder = "storage/intern_" + registrationNumber + "/project_" + idProject + "/initial_formats";
                String documentTypeName = selectedType.replaceAll(" ", "_").toLowerCase();
                String newFileName = documentTypeName + registrationNumber;
                String relativeFilePath = relativeFolder + "/" + newFileName + ".pdf";

                InitialFormatDAO initialFormatDAO = new InitialFormatDAO();
                InitialFormat initialFormat = new InitialFormat();
                initialFormat.setFormatType(selectedType);
                initialFormat.setFilePath(relativeFilePath);
                initialFormat.setIdProject(targetDocument.getIdProject());
                initialFormat.setIdInitialFormat(targetDocument.getIdInitialFormat());
                initialFormat.setSubmissionDate(
                        LocalDate.from(LocalDateTime.now(ZoneId.of("America/Mexico_City"))));

                boolean updateSucceeded = initialFormatDAO.updateStatus(initialFormat) > 0;
                if (updateSucceeded) {
                    saveFile(selectedFile, relativeFolder, newFileName);
                    pendingDocuments.remove(targetDocument);
                    comboBoxDocumentType.getItems().remove(selectedType);
                    LOGGER.log(Level.INFO,
                            "User {0} uploaded initial format {1}",
                            new Object[]{SessionManager.getInstance().getUser().getIdUser(), initialFormat.getFormatType()});
                    wasSaved = true;
                }
            }

        } catch (ValidationException validationException) {
            showError("Error al guardar el documento, datos no válidos");
        } catch (ServiceException serviceException) {
            showError("Error al guardar el documento, Servicio no disponible");
        }
        return wasSaved;
    }

    private InitialFormat findPendingByType(String documentType) {
        InitialFormat match = null;
        for (InitialFormat document : pendingDocuments) {
            boolean isSameType = match == null && documentType != null
                    && documentType.equals(document.getFormatType());
            if (isSameType) {
                match = document;
            }
        }
        return match;
    }

    private void clearSelectedFile() {
        selectedFile = null;
        labelFileName.setText("Ningún archivo seleccionado");
        labelStatus.setText("");
    }

    private void showError(String message) {
        labelStatus.getStyleClass().removeAll(STATUS_SUCCESS_STYLE_CLASS, STATUS_ERROR_STYLE_CLASS);
        labelStatus.getStyleClass().add(STATUS_ERROR_STYLE_CLASS);
        labelStatus.setText(message);
    }

    private void showInfo(String message) {
        labelStatus.getStyleClass().removeAll(STATUS_ERROR_STYLE_CLASS, STATUS_SUCCESS_STYLE_CLASS);
        labelStatus.getStyleClass().add(STATUS_SUCCESS_STYLE_CLASS);
        labelStatus.setText(message);
    }

}
