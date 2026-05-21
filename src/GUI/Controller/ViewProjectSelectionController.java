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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class ViewProjectSelectionController {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private FlowPane flowProjects;
    private List<Project> projectList;

    @FXML
    public void handleRegresar(ActionEvent actionEvent) {
    }

    @FXML
    public void handleCancelar(ActionEvent actionEvent) {
    }

    @FXML
    public void confirmButtom(ActionEvent actionEvent) {
        showAlertAndWait("Confirmacin","Sguro qeu desea seleccinar sos proeyectos",
                Alert.AlertType.CONFIRMATION).ifPresent(response ->{
                    if (response == ButtonType.OK){
                        requestProjectProcess();
                    }
                }
        );
    }

    private void requestProjectProcess(){
        try {
            ApplicationDAO applicationDAO = new ApplicationDAO();
            ProjectApplicationDAO projectApplicationDAO = new ProjectApplicationDAO();

            Application application = applicationDAO.findByIntern(SessionManager.getInstance().getUsuario().getId());

            if (application == null){
                application = new Application();
                application.setIdIntern(SessionManager.getInstance().getUsuario().getId());
                application.setApplicationDate(LocalDate.now(ZoneId.of("America/Mexico_City")));
                application.setStatus("Pendiente");
                application.setIdApplication(applicationDAO.create(application));

            }

            for (Project project: projectList){
                ProjectApplication projectApplication = new ProjectApplication();
                projectApplication.setIdApplication(application.getIdApplication());
                projectApplication.setIdProyect(project.getIdProyect());
                projectApplicationDAO.create(projectApplication);
            }

            showAlert("Exito", "su solicitud a sido creada",
                    Alert.AlertType.INFORMATION);
            openWelcomePage((AnchorPane) anchorPane.getParent());

        }catch (IllegalStateException illegalStateException){
            showAlert("Error", "No se a logrado encontrar un poryecto seleccionado",
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error", "servicio no disponible",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error ", "No s elogro concretar su solicitud",
                    Alert.AlertType.ERROR);
        }

    }

    private VBox createCard(Project project) {
        VBox card = new VBox(8);
        card.setPrefWidth(250.0);
        card.setStyle("""
        -fx-background-color: white;
        -fx-padding: 15;
        -fx-background-radius: 15;
        -fx-border-radius: 15;
        -fx-border-color: #ddd;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);""");

        Label nombre = new Label(project.getName());
        nombre.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        Label descripcion = new Label(project.getDescription());
        descripcion.setWrapText(true);
        Label fechas = new Label("Inicio: " + project.getStartDate() +
                "\nFin: " + project.getEndDate());
        Label cupo = new Label("Cupo: " + project.getAvaliablePlaces());
        Label org = new Label("Org: " + project.getOrganizationName());
        card.getChildren().addAll(nombre, descripcion, fechas, cupo, org);

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
