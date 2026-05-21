package GUI.Controller;

import Logic.DAO.ProfessorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Professor;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class DeactivateProfessorController {
    @FXML
    private TableColumn<User,String> nameColumn;
    @FXML
    private TableColumn<User,Void> actionsColumn;
    @FXML
    private TableColumn<User,String> secondLastNameColumn;
    @FXML
    private TableView tableView;
    @FXML
    private TableColumn<Professor,String>academicDegreeColumn;
    @FXML
    private TableColumn<User,String> lastNameColumn;
    @FXML
    private TableColumn<User,String> tagColumn;
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private void initialize() {
        tagColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMatricula()));

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLastName()));

        lastNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLastName()));

        secondLastNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSecondLastName()));
        academicDegreeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAcademicArea()));
        loadProfessors();
        addButtonToTable();
    }

    private void addButtonToTable() {
        actionsColumn.setCellFactory(column -> new TableCell<User, Void>() {

            private final Button button = new Button("Inactivar");

            {
                button.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    showAlertAndWait("Desea desactivar ","desea desactivar este profesor",
                            Alert.AlertType.CONFIRMATION).ifPresent(response -> {

                        if (response == ButtonType.OK) {
                            user.setStatus("Inactivo");
                            user.setRole("Profesor");
                            deactivateProcess(user);
                            loadProfessors();
                        }

                    });;

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

    private void deactivateProcess(User user){
        try{
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            userRoleDAO.updateUserRolStatus(user);
            showAlert("Profesor desactivado", "El profesor ha sido desactivado exitosamente.",
                    Alert.AlertType.INFORMATION);
        } catch (ValidationException validationException) {
            showAlert("Error", "servicio no disponible",
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error" , "no se logro desactivar",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadProfessors(){
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            tableView.getItems().setAll(professorDAO.findActiveProfessors());
        }  catch (ValidationException validationException) {
            showAlert("Error", "servicio no disponible",
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error" , "no se logro desactivar",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void calcelOperation(ActionEvent actionEvent) {
        showAlert("Informacion", "operacion cancelada",
                Alert.AlertType.INFORMATION);
        openWelcomePage(anchorPane);
    }
}
