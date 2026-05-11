package GUI.Controller;

import Logic.DAO.ApplicationDAO;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.ProjectApplicationDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.*;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;

public class ViewInterProjectSelection {
    public TableView projectsTableView;
    private User user;
    @FXML
    private TableColumn<Project, Void> actionColumn;
    @FXML
    private TableColumn<Project,String> placesColumn;
    @FXML
    private TableColumn<Project,String> datesColumn;
    @FXML
    private Label internNameLabel;
    @FXML
    private TableColumn<Project,String> organizationColumn;
    @FXML
    private TableColumn<Project, String> projetcNameColumn;
    List<Project> projectList = new ArrayList<>();
    List<ProjectApplication> projectApplicationList;
    @FXML
    private void initialize(){

    }

    private void configureTable(){
        int applicationUserID = loadInterProjectSelection();
        configureDataColumn();
        addButtonAssignToRow(applicationUserID);
    }

    private int loadInterProjectSelection(){
        int applicationID = 0;

        try {
            ApplicationDAO applicationDAO = new ApplicationDAO();
            ProjectApplicationDAO projectApplicationDAO = new ProjectApplicationDAO();
            ProjectDAO projectDAO = new ProjectDAO();
            Application application = applicationDAO.findByIntern(user.getId());
            projectApplicationList = projectApplicationDAO.findByApplication(application.getIdApplication());

            for (ProjectApplication projectApplication : projectApplicationList){
                projectList.add(projectDAO.findById(projectApplication.getIdProyect()));
            }

            applicationID = application.getIdApplication();
            projectsTableView.getItems().setAll(projectList);
        } catch (ServiceException e) {
            showAlert("Error", "servicio no disponible intente mas tarde",
                    Alert.AlertType.ERROR);
        } catch (ValidationException e) {
            showAlert("Error", "no se logro cargar projects seleccinados ",
                    Alert.AlertType.ERROR);
        }

        return applicationID;
    }

    private void configureDataColumn(){
        placesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getAvaliablePlaces())));

        datesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getStartDate()) + String.valueOf(cellData.getValue().getEndDate())));

        organizationColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOrganizationName()));

        projetcNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
    }

    private void addButtonAssignToRow(int userApplicationId){
        actionColumn.setCellFactory(param -> new javafx.scene.control.TableCell<Project, Void>(){
            private final Button button = new Button("Asignar");

            {
                button.setOnAction(event -> {
                    Project project = getTableView().getItems().get(getIndex());
                    showAlertAndWait("Advertecia", "Seguro que desea asignar ese proyeto a el practiante",
                            Alert.AlertType.CONFIRMATION).ifPresent(response -> {

                                if (response == ButtonType.OK){
                                    assignProjectProcess(project, userApplicationId);
                                }

                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(button);
                }

            }

        });
    }

    private void assignProjectProcess(Project project, int userApplicationId){
        try {
            ApplicationDAO applicationDAO = new ApplicationDAO();
            AssignmentDAO assignmentDAO = new AssignmentDAO();
            Assignment assignment = new Assignment();
            assignment.setIdApplication(projectApplicationList.getFirst().getIdApplication());
            assignment.setIdProyect(project.getIdProyect());
            assignment.setIdIntern(userApplicationId);
            assignment.setAssignmentDate(LocalDate.now(ZoneId.of("America/Mexico_City")));
            assignmentDAO.save(assignment);
            applicationDAO.updateStatus(userApplicationId, "Aceptada");

        } catch (ServiceException e) {
            showAlert("Error", "El servicio no se encitrna disponible en este momento",
                    Alert.AlertType.ERROR);
        } catch (ValidationException e) {
            showAlert("Error", "No se a logrado proesar su solicitud, intente nuevamente",
                    Alert.AlertType.ERROR);
        }

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        configureTable();
    }


}
