package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ViewsUtils.wrapInScrollableContent;

public class SelectProjectForActivityController implements ChangeListener<Project> {

    private static final Logger LOGGER =
            Logger.getLogger(SelectProjectForActivityController.class.getName());

    @FXML
    private TableView<Project> projectsTable;

    @FXML
    private TableColumn<Project, String> columnName;

    @FXML
    private TableColumn<Project, String> columnOrganization;

    @FXML
    private TableColumn<Project, String> columnStatus;

    @FXML
    private TableColumn<Project, String> columnPeriod;

    @FXML
    private Button addActivityButton;

    @FXML
    private Button manageActivitiesButton;

    @FXML
    private void initialize() {
        configureListeners();
        loadProjects();
    }

    @FXML
    public void openAddActivity(ActionEvent actionEvent) {
        navigateTo("/GUI/view/GUIAddActivity.fxml");
    }

    @FXML
    public void openManageActivities(ActionEvent actionEvent) {
        navigateTo("/GUI/view/GUIManageActivities.fxml");
    }

    private void configureListeners() {
        projectsTable.getSelectionModel().selectedItemProperty().addListener(this);
    }

    private void loadProjects() {
        try {
            int professorId = SessionManager.getInstance().getUsuario().getId();
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projects = projectDAO.findByProfessorAvailable(professorId);
            projectsTable.setItems(FXCollections.observableArrayList(projects));
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar proyectos del profesor: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los proyectos. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void navigateTo(String fxmlPath) {
        Project project = projectsTable.getSelectionModel().getSelectedItem();
        if (project == null) {
            showAlert("Sin selección", "Seleccione un proyecto de la tabla.",
                    Alert.AlertType.WARNING);
        } else {
            loadProjectView(fxmlPath, project);
        }
    }

    private void loadProjectView(String fxmlPath, Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object loaderController = loader.getController();
            if (loaderController instanceof AddActivityController) {
                ((AddActivityController) loaderController).setProject(project);
            } else if (loaderController instanceof ManageActivitiesController) {
                ((ManageActivitiesController) loaderController).setProject(project);
            }

            StackPane contentPane = (StackPane) projectsTable.getScene().lookup("#contentPane");
            if (contentPane != null) {
                contentPane.getChildren().setAll(wrapInScrollableContent(view));
            }

        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al cargar vista de actividades: {0}",
                    ioException.getMessage());
            showAlert("Error de navegación",
                    "No se pudo abrir la vista de actividades.",
                    Alert.AlertType.ERROR);
        }
    }

    @Override
    public void changed(ObservableValue<? extends Project> observable,
                        Project oldValue, Project newValue) {
        boolean hasSelection = newValue != null;
        addActivityButton.setDisable(!hasSelection);
        manageActivitiesButton.setDisable(!hasSelection);
    }

}
