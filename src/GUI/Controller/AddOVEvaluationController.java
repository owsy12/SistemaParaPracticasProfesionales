package GUI.Controller;

import GUI.SessionManager.SessionManager;
import GUI.Utils.EvaluationPrerequisiteChecker;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.OVEvaluationDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Assignment;
import Logic.DTOs.OVEvaluation;
import Logic.DTOs.Project;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.isPDF;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class AddOVEvaluationController implements EventHandler<DragEvent> {
    private static final String STATUS_SUBMITTED = "Entregada";


    private static final Logger LOGGER =
            Logger.getLogger(AddOVEvaluationController.class.getName());

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Label statusLabel;

    @FXML
    private Label labelFileName;

    @FXML
    private Pane dropZone;

    @FXML
    private Button uploadButton;

    private File selectedFile;
    private int internId;
    private int projectId;
    private String internRegistrationNumber;

    @FXML
    private void initialize() {
        configureDropZone();
        loadInternContext();
    }

    @FXML
    public void openFileChooser(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar evaluación OV firmada");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(dropZone.getScene().getWindow());
        if (file != null) {
            processSelectedFile(file);
        }
    }

    @FXML
    public void uploadOVEvaluation(ActionEvent actionEvent) {
        boolean isFileMissing = selectedFile == null;
        if (isFileMissing) {
            showStatus("Seleccione el PDF de la evaluación OV.", true);
        } else {
            uploadProcess();
        }
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        openWelcomePage(anchorPane);
    }

    private void configureDropZone() {
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
                processSelectedFile(dragboard.getFiles().getFirst());
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        }
        event.consume();
    }

    private void loadInternContext() {
        boolean hasSession = SessionManager.getInstance().getUser() != null;
        if (!hasSession) {
            showAlert("Sesión inválida", "No hay una sesión activa.", Alert.AlertType.WARNING);
            openWelcomePage(anchorPane);
        } else {
            internId = SessionManager.getInstance().getUser().getId();
            internRegistrationNumber = SessionManager.getInstance().getUser().getRegistrationNumber();
            loadInternData();
        }
    }

    private void loadInternData() {
        try {
            AssignmentDAO assignmentDAO = new AssignmentDAO();
            Assignment activeAssignment = assignmentDAO.getActiveByIdIntern(internId);

            if (activeAssignment == null) {
                showAlert("Sin proyecto asignado",
                        "No tiene un proyecto activo asignado. No puede entregar la evaluación OV.",
                        Alert.AlertType.WARNING);
                openWelcomePage(anchorPane);
            } else {
                loadProjectAndPrerequisites(activeAssignment);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    "Datos de sesión inválidos.", Alert.AlertType.ERROR);
            openWelcomePage(anchorPane);
        } catch (DuplicateEntryException duplicateEntryException) {
            showAlert("Conflicto de datos",
                    "Error al recuperar su información.", Alert.AlertType.ERROR);
            openWelcomePage(anchorPane);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron recuperar sus datos. Intente más tarde.",
                    Alert.AlertType.ERROR);
            openWelcomePage(anchorPane);
        }
    }

    private void loadProjectAndPrerequisites(Assignment activeAssignment) throws ValidationException, DuplicateEntryException, ServiceException {
        ProjectDAO projectDAO = new ProjectDAO();
        Project currentProject = projectDAO.findById(activeAssignment.getIdProject());
        projectId = currentProject.getIdProject();

        String prerequisiteMessage = EvaluationPrerequisiteChecker.check(internId, currentProject);

        if (prerequisiteMessage != null) {
            showAlert("Requisitos no cumplidos", prerequisiteMessage, Alert.AlertType.WARNING);
            openWelcomePage(anchorPane);
        } else {
            checkAlreadyDelivered();
        }
    }

    private void checkAlreadyDelivered() throws ServiceException, ValidationException {
        OVEvaluationDAO ovEvaluationDAO = new OVEvaluationDAO();
        OVEvaluation existing = ovEvaluationDAO.findByInternAndProject(internId, projectId);

        if (existing != null) {
            showAlert("Evaluación OV ya entregada", "Ya entregó su evaluación OV para este proyecto.",
                    Alert.AlertType.INFORMATION);
            openWelcomePage(anchorPane);
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
            String filePath = copyFile();
            OVEvaluationDAO ovEvaluationDAO = new OVEvaluationDAO();
            OVEvaluation ovEvaluation = buildOVEvaluation(filePath);
            int rowsAffected = ovEvaluationDAO.save(ovEvaluation);

            if (rowsAffected > 0) {
                LOGGER.log(Level.INFO,
                        "Usuario {0} entregó la evaluación OV del proyecto {1}",
                        new Object[]{SessionManager.getInstance().getUser().getId(), projectId});
                showAlert("Evaluación OV entregada",
                        "La evaluación OV fue registrada correctamente.",
                        Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);
            } else {
                showAlert("Error",
                        "No se pudo registrar la evaluación OV. Intente nuevamente.",
                        Alert.AlertType.ERROR);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (DuplicateEntryException duplicateEntryException) {
            showAlert("Evaluación OV ya entregada",
                    "Ya existe una evaluación OV registrada para este proyecto.",
                    Alert.AlertType.WARNING);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al guardar evaluación OV del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo guardar el documento. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al copiar archivo de evaluación OV: {0}",
                    ioException.getMessage());
            showAlert("Error de archivo",
                    "No se pudo copiar el archivo al almacenamiento.",
                    Alert.AlertType.ERROR);
        }
    }

    private OVEvaluation buildOVEvaluation(String filePath) {
        OVEvaluation ovEvaluation = new OVEvaluation();
        ovEvaluation.setIdIntern(internId);
        ovEvaluation.setIdProject(projectId);
        ovEvaluation.setDocumentPath(filePath);
        ovEvaluation.setStatus(STATUS_SUBMITTED);
        ovEvaluation.setDeliveryDate(LocalDateTime.now());
        return ovEvaluation;
    }

    private String copyFile() throws IOException {
        String folder = "storage/intern_" + internRegistrationNumber
                + "/project_" + projectId + "/ov_evaluation";
        String fileName = "ov_evaluation_" + internId + "_project_" + projectId;
        Path folderPath = Paths.get(folder);
        Files.createDirectories(folderPath);
        Path destination = folderPath.resolve(fileName + ".pdf");
        Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }

    private void showStatus(String message, boolean isError) {
        String textFillStyle = isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;";
        statusLabel.setStyle(textFillStyle);
        statusLabel.setText(message);
    }

    private void clearSelection() {
        selectedFile = null;
        labelFileName.setText("Ningún archivo seleccionado");
        statusLabel.setText("");
    }
}
