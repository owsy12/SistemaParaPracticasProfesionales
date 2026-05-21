package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.InitialFormatDAO;
import Logic.DTOs.InitialFormat;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
import static GUI.Utils.DocumentManngemt.saveFile;

public class UploadInitialDocumentsController {
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

    private void validatePendingInitialDocuments(){
        try {

            InitialFormatDAO initialFormatDAO = new InitialFormatDAO();
            initialFormatDAO.findPendingByIntern(SessionManager.getInstance().getUsuario().getId());
            List<InitialFormat> obteinPendingDocuments = initialFormatDAO.findPendingByIntern(SessionManager.getInstance().getUsuario().getId());

            if (obteinPendingDocuments.isEmpty()){

                showAlert("Advertenica", "No cuenta con documentos pendientes por subir, será redirigido a la página de bienvenida",
                        Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);

            }else {

                pendingDocuments = obteinPendingDocuments;
                loadDocumentTypes();
                configureDragAndDrop();
                clearSelectedFile();
            }

        }catch (ServiceException serviceException){
            showAlert("Error", "No se logro cargar la información, intente más tarde",
                    Alert.AlertType.ERROR);
        }catch (ValidationException validationException){
            showAlert("Error", "Error al verificar documentos pendientes", Alert.AlertType.ERROR);
        }
    }

    private void loadDocumentTypes() {

        comboBoxDocumentType.getItems().clear();
        for (InitialFormat initialFormat : pendingDocuments) {

            String documentType = initialFormat.getFormatType();

            if (documentType != null && !comboBoxDocumentType.getItems().contains(documentType)) {
                comboBoxDocumentType.getItems().add(documentType);

            }

        }

    }

    private void configureDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }

            event.consume();
        });

        dropZone.setOnDragDropped(event -> {

            Dragboard dragboard = event.getDragboard();
            boolean success = false;

            if (dragboard.hasFiles()) {

                File file = dragboard.getFiles().getFirst();
                processSelectedFile(file);
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
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

        }else {
            selectedFile = file;
            labelFileName.setText(file.getName());
            labelStatus.setText("Archivo válido");
        }

    }

    @FXML
    private void uploadDocument() {
        String errorMensaje = null;

        if (comboBoxDocumentType.getValue() == null) {
            errorMensaje= "Seleccione un tipo de documento";
        } else if (selectedFile == null) {
            errorMensaje = "Seleccione un archivo PDF";
        }

        if (errorMensaje != null){
            showError(errorMensaje);
        }else {
            saveDocumentProcess();
            showInfo("Documento guardado correctamente");
            if (pendingDocuments.isEmpty()){
                showAlert("Éxito", "Ya no tiene mas documntos pendientes sera redirigido",
                        Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);
            }

        }

    }

    private void saveDocumentProcess() {
        try {

            String relativeFolder = "storage/intern_" + SessionManager.getInstance().getUsuario().getMatricula() + "/project_" +
                    pendingDocuments.getFirst().getIdProject() + "/initial_formats";
            String newFileName = comboBoxDocumentType.getValue().replaceAll(" ", "_").toLowerCase() +
                    SessionManager.getInstance().getUsuario().getMatricula();
            InitialFormatDAO initialFormatDAO = new InitialFormatDAO();
            InitialFormat initialFormat = new InitialFormat();
            initialFormat.setFormatType(comboBoxDocumentType.getValue());
            initialFormat.setFilePath(relativeFolder);
            initialFormat.setIdProject(pendingDocuments.getFirst().getIdProject());
            initialFormat.setIdInitialFormat(pendingDocuments.getFirst().getIdInitialFormat());
            initialFormat.setSubmissionDate(LocalDate.from(LocalDateTime.now(ZoneId.of("America/Mexico_City"))));
            comboBoxDocumentType.getItems().remove(comboBoxDocumentType.getValue());

            if (initialFormatDAO.updateStatus(initialFormat) > 0){
                pendingDocuments.remove(pendingDocuments.getFirst());
                saveFile(selectedFile, relativeFolder, newFileName);
            }

        } catch (ValidationException validationException) {
            showError("Error al guardar el documento, datos no válidos");
        } catch (ServiceException serviceException) {
            showError("Error al guardar el documento, Servicio no disponible");
        }

    }



    private void clearSelectedFile() {

        selectedFile = null;

        labelFileName.setText("Ningún archivo seleccionado");

        labelStatus.setText("");

    }

    private void showError(String message) {

        labelStatus.setStyle("-fx-text-fill: red;");
        labelStatus.setText(message);
    }

    private void showInfo(String message) {

        labelStatus.setStyle("-fx-text-fill: green;");
        labelStatus.setText(message);
    }
}