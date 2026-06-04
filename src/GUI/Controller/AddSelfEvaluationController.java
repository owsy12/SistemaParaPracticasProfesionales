package GUI.Controller;

import GUI.SessionManager.SessionManager;
import GUI.Utils.EvaluationPrerequisiteChecker;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.SelfEvaluationDAO;
import Logic.DTOs.Assignment;
import Logic.DTOs.Project;
import Logic.DTOs.SelfEvaluation;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.isPDF;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class AddSelfEvaluationController implements EventHandler<DragEvent> {

    private static final Logger LOGGER =
            Logger.getLogger(AddSelfEvaluationController.class.getName());
    private static final String DELIVERED_STATUS = "Entregada";

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Label evaluationInfoLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Pane dropZone;

    @FXML
    private Label labelFileName;

    @FXML
    private Button uploadButton;

    private File selectedFile;
    private SelfEvaluation selfEvaluation;
    private int internId;
    private String internMatricula;

    @FXML
    private void initialize() {
        configureDropZone();
        loadInternContext();
    }

    @FXML
    public void openFileChooser(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar autoevaluación firmada");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(dropZone.getScene().getWindow());
        if (file != null) {
            processSelectedFile(file);
        }
    }

    @FXML
    public void uploadSelfEvaluation(ActionEvent actionEvent) {
        boolean isFileMissing = selectedFile == null;
        if (isFileMissing) {
            showStatus("Seleccione el PDF firmado de la autoevaluación.", true);
        } else {
            uploadProcess();
        }
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        clearSelection();
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
                processSelectedFile(dragboard.getFiles().get(0));
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        }
        event.consume();
    }

    private void loadInternContext() {
        boolean hasSession = SessionManager.getInstance().getUsuario() != null;
        if (!hasSession) {
            showAlert("Sesión inválida", "No hay una sesión activa.", Alert.AlertType.WARNING);
            openWelcomePage(anchorPane);
        } else {
            internId = SessionManager.getInstance().getUsuario().getId();
            internMatricula = SessionManager.getInstance().getUsuario().getMatricula();
            loadInternData();
        }
    }

    private void loadInternData() {
        try {
            AssignmentDAO assignmentDAO = new AssignmentDAO();
            Assignment activeAssignment = assignmentDAO.getActiveByIdIntern(internId);

            if (activeAssignment == null) {
                showAlert("Sin proyecto asignado",
                        "No tiene un proyecto activo asignado. No puede entregar la autoevaluación.",
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

        String prerequisiteMessage = EvaluationPrerequisiteChecker.check(internId, currentProject);

        if (prerequisiteMessage != null) {
            showAlert("Requisitos no cumplidos", prerequisiteMessage, Alert.AlertType.WARNING);
            openWelcomePage(anchorPane);
        } else {
            loadSelfEvaluation();
        }
    }

    private void loadSelfEvaluation() throws ServiceException, ValidationException {
        SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();
        selfEvaluation = selfEvaluationDAO.findByIdIntern(internId);

        boolean isNotAvailable = selfEvaluation == null
                || DELIVERED_STATUS.equals(selfEvaluation.getStatus());

        if (isNotAvailable) {
            String message = selfEvaluation == null
                    ? "No ha generado su autoevaluación aún. Genérela primero."
                    : "Su autoevaluación ya fue entregada anteriormente.";
            showAlert("Autoevaluación no disponible", message, Alert.AlertType.INFORMATION);
            openWelcomePage(anchorPane);
        } else {
            String infoText = "Autoevaluación: " + selfEvaluation.getPeriod()
                    + "  |  Estado: " + selfEvaluation.getStatus();
            evaluationInfoLabel.setText(infoText);
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
            String filePath = copySignedFile();
            SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();

            boolean pathUpdated = selfEvaluationDAO.updateDocumentPath(
                    selfEvaluation.getIdSelfEvalation(), filePath);
            boolean statusUpdated = selfEvaluationDAO.updateStatus(
                    selfEvaluation.getIdSelfEvalation(), DELIVERED_STATUS);

            if (pathUpdated && statusUpdated) {
                showAlert("Autoevaluación entregada",
                        "Su autoevaluación firmada fue registrada correctamente.",
                        Alert.AlertType.INFORMATION);
                concludePracticeIfComplete(internId, selfEvaluation.getIdProject());
                openWelcomePage(anchorPane);
            } else {
                showAlert("Error",
                        "No se pudo registrar la autoevaluación. Intente nuevamente.",
                        Alert.AlertType.ERROR);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al guardar autoevaluación firmada del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo guardar el documento. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al copiar archivo de autoevaluación: {0}",
                    ioException.getMessage());
            showAlert("Error de archivo",
                    "No se pudo copiar el archivo al almacenamiento.",
                    Alert.AlertType.ERROR);
        }
    }

    private void concludePracticeIfComplete(int internIdentifier, int projectIdentifier) {
        try {
            if (EvaluationPrerequisiteChecker.isPracticeComplete(internIdentifier, projectIdentifier)) {
                PracticeDAO practiceDAO = new PracticeDAO();
                boolean concluded = practiceDAO.concludeActiveByIntern(internIdentifier);
                if (concluded) {
                    showAlert("Práctica concluida",
                            "El practicante cumplió todos los requisitos; su práctica fue marcada como Concluida.",
                            Alert.AlertType.INFORMATION);
                }
            }
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al evaluar el cierre de la práctica del practicante {0}: {1}",
                    new Object[]{internIdentifier, serviceException.getMessage()});
        } catch (ValidationException validationException) {
            LOGGER.log(Level.WARNING, "Validación al cerrar la práctica: {0}",
                    validationException.getMessage());
        }
    }

    private String copySignedFile() throws IOException {
        String folder = "storage/intern_" + internMatricula + "/project_" + selfEvaluation.getIdProject() + "/self_evaluation";
        String fileName = "self_evaluation_" + selfEvaluation.getIdSelfEvalation() + "_signed";
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
