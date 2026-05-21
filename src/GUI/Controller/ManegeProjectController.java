package GUI.Controller;

import Logic.DAO.ProjectDAO;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.List;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class ManegeProjectController {

    @FXML
    private TableColumn<Project,String> capacityColumn;
    @FXML
    private TableColumn<Project,String> endDateColumn;
    @FXML
    private TableColumn<Project, Void> updateColumn;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TableColumn<Project,String> nameColumn;
    @FXML
    private TableColumn<Project,String> statusColmun;
    @FXML
    private TableView tableView;
    @FXML
    private TableColumn<Project,String> organizationColumn;
    @FXML
    private TableColumn deleteColumn;
    @FXML
    private TableColumn<Project,String> starDateColumn;
    @FXML
    private TableColumn<Project, String> nrcColumn;

    @FXML
    private void initialize(){
        loadProjectoOnTableView();
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        openWelcomePage(anchorPane);
    }

    private void loadProjectoOnTableView(){
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projectList = projectDAO.findAll();

            if (projectList != null) {
                configurateDataColumnsTable();
                tableView.getItems().setAll(projectList);
            } else {
              showAlert("Advertencia", "No se encontraron proyectos", Alert.AlertType.INFORMATION);
              openWelcomePage(anchorPane);
            }

        } catch (ServiceException serviceException) {
            showAlert("Error", "servico no disponible intente mas tarde", Alert.AlertType.ERROR);
        }
    }

    private void configurateDataColumnsTable(){
        capacityColumn.setCellValueFactory(project ->
                new SimpleStringProperty(String.valueOf(project.getValue().getMaximumPlaces())));

        endDateColumn.setCellValueFactory(project ->
                new SimpleStringProperty(project.getValue().getEndDate().toString()));

        starDateColumn.setCellValueFactory(project ->
                new SimpleStringProperty(project.getValue().getStartDate().toString()));

        nameColumn.setCellValueFactory(project ->
                new SimpleStringProperty(project.getValue().getName()));

        statusColmun.setCellValueFactory(project ->
                new SimpleStringProperty(project.getValue().getStatus()));

        organizationColumn.setCellValueFactory(project ->
                new SimpleStringProperty(project.getValue().getOrganizationName()));

        nrcColumn.setCellValueFactory(project ->
                new SimpleStringProperty(String.valueOf(project.getValue().getIdProyect())));

        addActionButtons();
    }

    private void addActionButtons(){
        deleteColumn.setCellFactory(column -> new TableCell<Project, Void>(){

            Button button = new Button("Eliminar");

            {
                button.setOnAction(event ->{
                    int idProject = getTableView().getItems().get(getIndex()).getIdProyect();
                    showAlertAndWait("Desea Eliminar", "Desea eliminar este proeycto, esta accion es ireversible",
                            Alert.AlertType.ERROR).ifPresent(response ->{
                                if (response == ButtonType.OK) {
                                    deleteProcess(idProject);
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

        updateColumn.setCellFactory(column -> new TableCell<Project, Void>(){

            Button button = new Button("Actualizar");

            {
                button.setOnAction(event ->{
                    Project idProject = getTableView().getItems().get(getIndex());
                    openModifyProjectView(idProject);
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

    private void openModifyProjectView(Project project){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/view/GUIUpdateProject.fxml"));
            Parent vista = loader.load();
            UpdateProjectController controller = loader.getController();
            controller.setProject(project);
            anchorPane.getChildren().setAll(vista);
        } catch (IOException ioException) {
            showAlert("Error", "Np se logro cargar la vista",
                    Alert.AlertType.ERROR);
        }
    }

    private void deleteProcess(int idProject){
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            projectDAO.deleteProject(idProject);
            loadProjectoOnTableView();
            showAlert("exito", "Proyecto eliminado exitosamente",
                    Alert.AlertType.INFORMATION);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no dispoible" , Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error ", "no se logro validar el proyecto", Alert.AlertType.ERROR);
        }
    }
}
