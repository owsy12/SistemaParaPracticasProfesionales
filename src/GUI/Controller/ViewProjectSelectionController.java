package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ApplicationDAO;
import Logic.DAO.ProjectApplicationDAO;
import Logic.DTOs.Application;
import Logic.DTOs.Project;
import Logic.DTOs.ProjectApplication;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class ViewProjectSelectionController {

    private static final Logger LOGGER = Logger.getLogger(ViewProjectSelectionController.class.getName());
    private static final String STATUS_PENDING = "Pendiente";


    @FXML
    private AnchorPane anchorPane;

    @FXML
    private FlowPane flowProjects;

    private List<Project> projectList;

    @FXML
    public void handleRegresar(ActionEvent actionEvent) {
        try {
            AnchorPane parentPane = (AnchorPane) anchorPane.getParent();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/view/GUIRequestProject.fxml"));
            Parent view = loader.load();
            parentPane.getChildren().setAll(view);
        } catch (IOException ioException) {
            showAlert("Error", "No se pudo regresar a la selección de proyectos.",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleCancelar(ActionEvent actionEvent) {
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmar cancelación",
                "¿Desea salir? Los datos ingresados no se guardarán.",
                Alert.AlertType.CONFIRMATION);
        boolean isConfirmed = false;
        if (response.isPresent()) {
            if (response.get() == ButtonType.OK) {
                isConfirmed = true;
            }
        }
        if (isConfirmed) {
            openWelcomePage((AnchorPane) anchorPane.getParent());
        }
    }

    @FXML
    public void confirmButtom(ActionEvent actionEvent) {
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmación",
                "¿Seguro que desea seleccionar estos proyectos?",
                Alert.AlertType.CONFIRMATION);

        boolean isUserConfirmed = false;

        if (response.isPresent()) {

            if (response.get() == ButtonType.OK) {

                isUserConfirmed = true;

            }

        }
        if (isUserConfirmed) {
            requestProjectProcess();
        }
    }

    private void requestProjectProcess() {
        try {
            ApplicationDAO applicationDAO = new ApplicationDAO();
            ProjectApplicationDAO projectApplicationDAO = new ProjectApplicationDAO();

            int currentUserId = SessionManager.getInstance().getUser().getIdUser();
            Application pendingApplication = applicationDAO.findActiveApplicationByIntern(currentUserId);

            Application application;
            if (pendingApplication == null) {
                application = new Application();
                application.setIdIntern(currentUserId);
                application.setApplicationDate(LocalDate.now(ZoneId.of("America/Mexico_City")));
                application.setStatus(STATUS_PENDING);
                application.setIdApplication(applicationDAO.create(application));
            } else {
                application = pendingApplication;
            }

            for (Project project : projectList) {
                ProjectApplication projectApplication = new ProjectApplication();
                projectApplication.setIdApplication(application.getIdApplication());
                projectApplication.setIdProject(project.getIdProject());
                projectApplicationDAO.create(projectApplication);
            }

            LOGGER.log(Level.INFO,
                    "User {0} registered project application {1}",
                    new Object[]{SessionManager.getInstance().getUser().getIdUser(), application.getIdApplication()});
            showAlert("Éxito", "Su solicitud ha sido creada.",
                    Alert.AlertType.INFORMATION);
            openWelcomePage((AnchorPane) anchorPane.getParent());

        } catch (IllegalStateException illegalStateException) {
            showAlert("Error", "No se logró encontrar un proyecto seleccionado.",
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró concretar su solicitud.",
                    Alert.AlertType.ERROR);
        }
    }

    private VBox createCard(Project project) {
        VBox card = new VBox(8);
        card.setPrefWidth(250.0);
        card.getStyleClass().add("projectCardVBox");

        Label projectNameLabel = new Label(project.getName());
        projectNameLabel.getStyleClass().add("projectNameLabel");

        Label descriptionLabel = new Label(project.getDescription());
        descriptionLabel.setWrapText(true);

        Label datesLabel = new Label("Inicio: " + project.getStartDate()
                + "\nFin: " + project.getEndDate());

        Label capacityLabel = new Label("Cupo: " + project.getAvailablePlaces());

        Label organizationLabel = new Label("Org: " + project.getOrganizationName());

        card.getChildren().addAll(projectNameLabel, descriptionLabel, datesLabel,
                capacityLabel, organizationLabel);

        return card;
    }

    public List<Project> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<Project> projectList) {
        this.projectList = projectList;
        flowProjects.getChildren().clear();

        for (Project project : projectList) {
            VBox card = createCard(project);
            flowProjects.getChildren().add(card);
        }
    }

}
