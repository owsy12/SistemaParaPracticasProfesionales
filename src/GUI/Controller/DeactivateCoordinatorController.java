package GUI.Controller;

import Logic.DAO.UserDAO;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;

public class DeactivateCoordinatorController {
    @FXML
    private TableColumn<User, String>  nameColumn;
    @FXML
    private TableColumn<User, Void>  actionsColumn;
    @FXML
    private TableColumn<User, String>  secondLastNameColumn;
    @FXML
    private TableView tableView;
    @FXML
    private TableColumn<User, String>  lastNameColumn;
    @FXML
    private TableColumn<User, String>  matriculaColumn;

    @FXML
    private void initialize() {
        matriculaColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMatricula()));

        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastName()));

        lastNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastName()));

        secondLastNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSecondLastName()));
        addButtonToTable();
    }

    private void addButtonToTable() {
        actionsColumn.setCellFactory(param -> new javafx.scene.control.TableCell<User, Void>() {

            private final Button btn = new Button("Inactivar");

            {
                btn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    showAlertAndWait("Desea desactivar ","desae", Alert.AlertType.CONFIRMATION).ifPresent(response -> {;
                        if (response == javafx.scene.control.ButtonType.OK) {

                            showAlert("Coordinador desactivado", "El coordinador ha sido desactivado exitosamente.", Alert.AlertType.INFORMATION);

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
                    setGraphic(btn);
                }
            }
        });
    }

    private void deactivateProcess(){
        try {
            User userUpdate = new User();
            userUpdate.setStatus("Inactivo");

            UserDAO userDAO = new UserDAO();
            userDAO.update(userUpdate);
        }catch (ServiceException e){

        }catch (ValidationException e){


        }
    }
}
