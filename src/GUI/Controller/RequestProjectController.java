package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ApplicationDAO;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.InternDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Application;
import Logic.DTOs.Assignment;
import Logic.DTOs.Intern;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class RequestProjectController {

    private static final int SELECTION_LIMIT = 3;
    private static final int MINIMUM_NUMBER_OF_CREDITS_REQUIRED = 270;

    @FXML
    public AnchorPane anchorPane;

    @FXML
    private TableColumn<Project, String> endDateColumn;

    @FXML
    private TableColumn<Project, String> nameColumn;

    @FXML
    private TableColumn<Project, String> startDateColumn;

    @FXML
    private TableColumn<Project, String> placesColumn;

    @FXML
    private TableColumn<Project, String> organizationColumn;

    @FXML
    private TableColumn<Project, String> descriptionColumn;

    @FXML
    private TableView<Project> projectsTableView;

    @FXML
    private TableColumn<Project, String> nrcColumn;

    @FXML
    private void initialize() {
        verifyActiveInternProjectApplication();
    }

    @FXML
    public void projectSelectionListButton(ActionEvent actionEvent) {
        ObservableList<Project> selectedItems =
                projectsTableView.getSelectionModel().getSelectedItems();

        boolean hasRequiredSelections = selectedItems.size() == SELECTION_LIMIT;
        if (hasRequiredSelections) {
            viewProjectSelections(new ArrayList<>(selectedItems));
        } else {
            showAlert("Advertencia", "Verifique su selección, debe seleccionar 3 proyectos.",
                    Alert.AlertType.WARNING);
        }
    }

    private void viewProjectSelections(List<Project> projectList) {
        try {
            openViewProjectSelection(projectList);
        } catch (IllegalStateException illegalStateException) {
            showAlert("Error", "No se logró encontrar un proyecto seleccionado.",
                    Alert.AlertType.ERROR);
        }
    }

    private void verifyActiveInternProjectApplication() {
        try {
            InternDAO internDAO = new InternDAO();
            AssignmentDAO assignmentDAO = new AssignmentDAO();
            ApplicationDAO applicationDAO = new ApplicationDAO();
            int currentUserId = SessionManager.getInstance().getUsuario().getId();

            Application application = applicationDAO.findActiveApplicationByIntern(currentUserId);
            Assignment assignment = assignmentDAO.getActiveByIdIntern(currentUserId);
            Intern intern = internDAO.findById(currentUserId);

            boolean hasEnoughCredits = intern.getCredits() > MINIMUM_NUMBER_OF_CREDITS_REQUIRED;
            boolean hasNoAssignment = assignment == null;
            boolean hasNoApplication = application == null;
            boolean canRequest = hasEnoughCredits && hasNoAssignment && hasNoApplication;

            if (canRequest) {
                allowRequest();
            } else {
                showAlert("Advertencia",
                        "No cumple con los requisitos para poder crear una solicitud.",
                        Alert.AlertType.WARNING);
                openWelcomePage(anchorPane);
            }

        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible en este momento.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró encontrar su usuario.",
                    Alert.AlertType.ERROR);
        }
    }

    private void allowRequest() throws ServiceException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        int currentUserId = SessionManager.getInstance().getUsuario().getId();
        Application application = applicationDAO.findByIntern(currentUserId);

        boolean hasPendingApplication =
                application != null && "Pendiente".equals(application.getStatus());

        if (!hasPendingApplication) {
            configureTable();
        } else {
            showAlert("Advertencia", "Usted ya tiene una solicitud pendiente.",
                    Alert.AlertType.WARNING);
            openWelcomePage(anchorPane);
        }
    }

    private void openViewProjectSelection(List<Project> projects) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/view/GUIViewProjectSelection.fxml"));
            Parent vista = loader.load();
            ViewProjectSelectionController controller = loader.getController();
            controller.setProjectList(projects);
            anchorPane.getChildren().setAll(vista);

        } catch (IOException ioException) {
            showAlert("Error", "Error al cargar.", Alert.AlertType.ERROR);
        }
    }

    private void configureTable() {
        projectsTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        loadProjects();
    }

    private void loadProjects() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projectList = projectDAO.findAllAvailable();
            projectsTableView.getItems().setAll(projectList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logró cargar los proyectos.",
                    Alert.AlertType.ERROR);
        }
    }

}
