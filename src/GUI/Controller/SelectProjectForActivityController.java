package GUI.Controller;

import Logic.DAO.ProjectDAO;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;

public class SelectProjectForActivityController {

    private static final Logger LOGGER =
            Logger.getLogger(SelectProjectForActivityController.class.getName());

    @FXML
    private TableView<Project> projectsTable;

    @FXML
    private TableColumn<Project, String> colName;

    @FXML
    private TableColumn<Project, String> colOrganization;

    @FXML
    private TableColumn<Project, String> colStatus;

    @FXML
    private TableColumn<Project, String> colPeriod;

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
        projectsTable.getSelectionModel().selectedItemProperty()
                .addListener(new ProjectSelectionListener());
    }

    private final class ProjectSelectionListener implements ChangeListener<Project> {
        @Override
        public void changed(ObservableValue<? extends Project> observable,
                            Project oldValue, Project newValue) {
            boolean hasSelection = newValue != null;
            addActivityButton.setDisable(!hasSelection);
            manageActivitiesButton.setDisable(!hasSelection);
        }
    }

    private void loadProjects() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> allProjects = projectDAO.findAll();
            List<Project> activeProjects = new ArrayList<>();

            for (Project projectItem : allProjects) {
                if (!"Cancelado".equals(projectItem.getStatus())) {
                    activeProjects.add(projectItem);
                }
            }

            projectsTable.setItems(FXCollections.observableArrayList(activeProjects));
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar proyectos: {0}",
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
                contentPane.getChildren().setAll(view);
            }

        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al cargar vista de actividades: {0}",
                    ioException.getMessage());
            showAlert("Error de navegación",
                    "No se pudo abrir la vista de actividades.",
                    Alert.AlertType.ERROR);
        }
    }


}
