package GUI.Controller;

import Logic.DAO.ProjectDAO;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class ManegeProjectController {

    @FXML
    private TableColumn<Project, String> capacityColumn;

    @FXML
    private TableColumn<Project, String> endDateColumn;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TableColumn<Project, String> nameColumn;

    @FXML
    private TableColumn<Project, String> statusColumn;

    @FXML
    private TableView<Project> tableView;

    @FXML
    private TableColumn<Project, String> organizationColumn;

    @FXML
    private TableColumn<Project, String> startDateColumn;

    @FXML
    private TableColumn<Project, String> nrcColumn;

    private Project selectedProject;

    @FXML
    private void initialize() {
        configureListeners();
        loadProjectsOnTableView();
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        openWelcomePage(anchorPane);
    }

    @FXML
    public void deleteProject(ActionEvent actionEvent) {
        boolean isSelectionMissing = selectedProject == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un proyecto de la tabla para eliminar.",
                    Alert.AlertType.WARNING);
            return;
        }

        Optional<ButtonType> response = showAlertAndWait(
                "Desea Eliminar",
                "¿Desea eliminar este proyecto? Esta acción es irreversible.",
                Alert.AlertType.CONFIRMATION);

        boolean isUserConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isUserConfirmed) {
            deleteProcess(selectedProject.getIdProyect());
        }
    }

    @FXML
    public void updateProject(ActionEvent actionEvent) {
        boolean isSelectionMissing = selectedProject == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un proyecto de la tabla para actualizar.",
                    Alert.AlertType.WARNING);
            return;
        }
        openModifyProjectView(selectedProject);
    }

    private void configureListeners() {
        tableView.getSelectionModel().selectedItemProperty()
                .addListener(new ProjectSelectionListener());
    }

    private final class ProjectSelectionListener implements ChangeListener<Project> {
        @Override
        public void changed(ObservableValue<? extends Project> observable,
                            Project oldValue, Project newValue) {
            selectedProject = newValue;
        }
    }

    private void loadProjectsOnTableView() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projectList = projectDAO.findAll();

            if (projectList != null && !projectList.isEmpty()) {
                tableView.getItems().setAll(projectList);
            } else {
                showAlert("Advertencia", "No se encontraron proyectos.",
                        Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);
            }

        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible, intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void openModifyProjectView(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/view/GUIUpdateProject.fxml"));
            Parent vista = loader.load();
            UpdateProjectController controller = loader.getController();
            controller.setProject(project);
            anchorPane.getChildren().setAll(vista);
        } catch (IOException ioException) {
            showAlert("Error", "No se logró cargar la vista.", Alert.AlertType.ERROR);
        }
    }

    private void deleteProcess(int idProject) {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            projectDAO.deleteProject(idProject);
            selectedProject = null;
            loadProjectsOnTableView();
            showAlert("Éxito", "Proyecto eliminado exitosamente.", Alert.AlertType.INFORMATION);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró validar el proyecto.", Alert.AlertType.ERROR);
        }
    }

}
