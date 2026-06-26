package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ApplicationDAO;
import Logic.DAO.InternDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Application;
import Logic.DTOs.Intern;
import Logic.DTOs.Practice;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.List;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class RequestProjectController {

    private static final int SELECTION_LIMIT = 3;
    private static final int MINIMUM_NUMBER_OF_CREDITS_REQUIRED = 270;

    @FXML
    public AnchorPane anchorPane;

    @FXML
    private TableView<Project> availableTableView;

    @FXML
    private TableView<Project> selectedTableView;

    @FXML
    private Label selectedCountLabel;

    private final ObservableList<Project> availableProjects = FXCollections.observableArrayList();
    private final ObservableList<Project> selectedProjects = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        availableTableView.setItems(availableProjects);
        selectedTableView.setItems(selectedProjects);
        verifyActiveInternProjectApplication();
    }

    @FXML
    public void addToSelection(ActionEvent actionEvent) {
        Project project = availableTableView.getSelectionModel().getSelectedItem();
        boolean isNotSelected = project == null;
        if (isNotSelected) {
            showAlert("Sin selección", "Seleccione un proyecto de la lista izquierda para agregar.",
                    Alert.AlertType.WARNING);
        } else {
            boolean isLimitReached = selectedProjects.size() >= SELECTION_LIMIT;
            if (isLimitReached) {
                showAlert("Límite alcanzado",
                        "Ya seleccionó 3 proyectos. Quite uno antes de agregar otro.",
                        Alert.AlertType.WARNING);
            } else {
                project.setSelectionOrder(selectedProjects.size() + 1);
                availableProjects.remove(project);
                selectedProjects.add(project);
                updateCountLabel();
            }
        }
    }

    @FXML
    public void removeFromSelection(ActionEvent actionEvent) {
        Project project = selectedTableView.getSelectionModel().getSelectedItem();
        boolean isNotSelected = project == null;
        if (isNotSelected) {
            showAlert("Sin selección", "Seleccione un proyecto de la lista derecha para quitar.",
                    Alert.AlertType.WARNING);
        } else {
            project.setSelectionOrder(0);
            selectedProjects.remove(project);
            availableProjects.add(project);
            reorderSelectionNumbers();
            updateCountLabel();
        }
    }

    @FXML
    public void projectSelectionListButton(ActionEvent actionEvent) {
        boolean hasRequiredSelections = selectedProjects.size() == SELECTION_LIMIT;
        if (hasRequiredSelections) {
            viewProjectSelections(selectedProjects);
        } else {
            showAlert("Advertencia", "Debe seleccionar exactamente 3 proyectos.",
                    Alert.AlertType.WARNING);
        }
    }

    private void viewProjectSelections(ObservableList<Project> projectList) {
        try {
            openViewProjectSelection(projectList);
        } catch (IllegalStateException illegalStateException) {
            showAlert("Error", "No se logró encontrar un proyecto seleccionado.",
                    Alert.AlertType.ERROR);
        }
    }

    private void updateCountLabel() {
        selectedCountLabel.setText("Mis selecciones (" + selectedProjects.size() + " / " + SELECTION_LIMIT + "):");
    }

    private void reorderSelectionNumbers() {
        for (int i = 0; i < selectedProjects.size(); i++) {
            selectedProjects.get(i).setSelectionOrder(i + 1);
        }
        selectedTableView.refresh();
    }

    private void verifyActiveInternProjectApplication() {
        try {
            int currentUserId = SessionManager.getInstance().getUser().getIdUser();

            PracticeDAO practiceDAO = new PracticeDAO();
            boolean hasConcluded = practiceDAO.hasConcludedPractice(currentUserId);
            Practice activePractice = practiceDAO.findActiveByIntern(currentUserId);

            ApplicationDAO applicationDAO = new ApplicationDAO();
            Application pendingApplication = applicationDAO.findActiveApplicationByIntern(currentUserId);

            InternDAO internDAO = new InternDAO();
            Intern intern = internDAO.findById(currentUserId);
            boolean hasEnoughCredits = intern.getCredits() > MINIMUM_NUMBER_OF_CREDITS_REQUIRED;

            boolean hasConcludedPractice = hasConcluded;
            boolean hasActivePractice = activePractice != null;
            boolean hasPendingApplication = pendingApplication != null;

            if (hasConcludedPractice) {
                showAlert("Práctica concluida",
                        "Ya concluyó una práctica profesional y no puede volver a solicitar.",
                        Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);
            } else if (hasActivePractice) {
                showAlert("Práctica activa",
                        "Ya cuenta con una práctica activa.",
                        Alert.AlertType.WARNING);
                openWelcomePage(anchorPane);
            } else if (hasPendingApplication) {
                showAlert("Solicitud pendiente",
                        "Ya cuenta con una solicitud pendiente.",
                        Alert.AlertType.WARNING);
                openWelcomePage(anchorPane);
            } else if (!hasEnoughCredits) {
                showAlert("Créditos insuficientes",
                        "No cuenta con los créditos suficientes para solicitar una práctica profesional.",
                        Alert.AlertType.WARNING);
                openWelcomePage(anchorPane);
            } else {
                loadProjects();
            }

        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible en este momento.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró encontrar su usuario.",
                    Alert.AlertType.ERROR);
        }
    }

    private void openViewProjectSelection(List<Project> projects) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/view/GUIViewProjectSelection.fxml"));
            Parent view = loader.load();
            ViewProjectSelectionController controller = loader.getController();
            controller.setProjectList(projects);
            anchorPane.getChildren().setAll(view);

        } catch (IOException ioException) {
            showAlert("Error", "Error al cargar.", Alert.AlertType.ERROR);
        }
    }

    private void loadProjects() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projectList = projectDAO.findAllAvailable();
            availableProjects.setAll(projectList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logró cargar los proyectos.",
                    Alert.AlertType.ERROR);
        }
    }

}
