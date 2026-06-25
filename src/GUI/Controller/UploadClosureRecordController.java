package GUI.Controller;

import GUI.SessionManager.SessionManager;
import GUI.Utils.EvaluationPrerequisiteChecker;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Assignment;
import Logic.DTOs.Project;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.isPDF;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class UploadClosureRecordController implements EventHandler<DragEvent> {

    private static final Logger LOGGER =
            Logger.getLogger(UploadClosureRecordController.class.getName());

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Label statusLabel;

    @FXML
    private Label labelFileName;

    @FXML
    private Pane dropZone;

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
        fileChooser.setTitle("Seleccionar acta de cierre");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(dropZone.getScene().getWindow());
        if (file != null) {
            processSelectedFile(file);
        }
    }

    @FXML
    public void uploadClosureRecord(ActionEvent actionEvent) {
        boolean isFileMissing = selectedFile == null;
        if (isFileMissing) {
            showStatus("Seleccione el PDF del acta de cierre.", true);
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
                        "No tiene un proyecto activo asignado. No puede subir el acta de cierre.",
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

    private void loadProjectAndPrerequisites(Assignment activeAssignment)
            throws ValidationException, DuplicateEntryException, ServiceException {
        ProjectDAO projectDAO = new ProjectDAO();
        Project currentProject = projectDAO.findById(activeAssignment.getIdProject());
        projectId = currentProject.getIdProject();

        PracticeDAO practiceDAO = new PracticeDAO();
        boolean alreadyConcluded = practiceDAO.hasConcludedPractice(internId);
        boolean evaluationsComplete =
                EvaluationPrerequisiteChecker.isPracticeComplete(internId, projectId);

        if (alreadyConcluded) {
            showAlert("Práctica concluida",
                    "Su práctica ya está concluida; no es necesario subir el acta de cierre.",
                    Alert.AlertType.INFORMATION);
            openWelcomePage(anchorPane);
        } else if (!evaluationsComplete) {
            showAlert("Cierre no disponible",
                    "Aún no puede subir el acta de cierre. Debe completar todos los reportes, "
                    + "la autoevaluación y la evaluación de la organización vinculada.",
                    Alert.AlertType.WARNING);
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
            PracticeDAO practiceDAO = new PracticeDAO();
            boolean registered = practiceDAO.markClosureRecordPending(internId, filePath);

            if (registered) {
                LOGGER.log(Level.INFO,
                        "Usuario {0} subió el acta de cierre del proyecto {1}; pendiente de validación",
                        new Object[]{internId, projectId});
                showAlert("Acta enviada",
                        "El acta de cierre fue enviada y está pendiente de validación por el "
                        + "coordinador. La práctica se cerrará cuando el coordinador la valide.",
                        Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);
            } else {
                showAlert("Error",
                        "No se pudo registrar el acta de cierre. Intente nuevamente.",
                        Alert.AlertType.ERROR);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al guardar acta de cierre del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo guardar el documento. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al copiar archivo del acta de cierre: {0}",
                    ioException.getMessage());
            showAlert("Error de archivo",
                    "No se pudo copiar el archivo al almacenamiento.",
                    Alert.AlertType.ERROR);
        }
    }

    private String copyFile() throws IOException {
        String folder = "storage/intern_" + internRegistrationNumber
                + "/project_" + projectId + "/closure";
        String fileName = "closure_record_" + internId + "_project_" + projectId;
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
