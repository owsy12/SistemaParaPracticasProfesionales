package GUI.Controller;

import Logic.DAO.CoordinatorDAO;
import Logic.DAO.UserRoleDAO;
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
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;
    @FXML
    private TableColumn<User, String> secondLastNameColumn;
    @FXML
    private TableView tableView;
    @FXML
    private TableColumn<User, String> lastNameColumn;
    @FXML
    private TableColumn<User, String> tagColumn;

    @FXML
    private void initialize() {
        tagColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMatricula()));

        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastName()));

        lastNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastName()));

        secondLastNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSecondLastName()));
        loadCoordinators();
        addButtonToTable();
    }

    private void addButtonToTable() {
        actionsColumn.setCellFactory(column -> new javafx.scene.control.TableCell<User, Void>() {

            private final Button inactivateButton = new Button("Inactivar");

            {
                inactivateButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    showAlertAndWait("Desea desactivar ","desae", Alert.AlertType.CONFIRMATION).ifPresent(response -> {;

                        if (response == javafx.scene.control.ButtonType.OK) {
                            user.setStatus("Inactivo");
                            user.setRole("Coordinador");
                            deactivateProcess(user);
                            loadCoordinators();
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
                    setGraphic(inactivateButton);
                }

            }
        });
    }

    private void deactivateProcess(User user ){
        try {
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            userRoleDAO.updateUserRolStatus(user);
            showAlert("Coordinador desactivado", "El coordinador ha sido desactivado exitosamente.", Alert.AlertType.INFORMATION);
        }catch (ServiceException serviceException){
            showAlert("Error de servicio", "Ocurrió un error al intentar desactivar el coordinador. Por favor, inténtelo de nuevo más tarde.",
                    Alert.AlertType.ERROR);
        }catch (ValidationException validationException){
            showAlert("Error de validación", "Los datos proporcionados no son válidos. Por favor, revise la información e intente nuevamente.",
                    Alert.AlertType.WARNING);
        }
    }

    private void loadCoordinators(){
        try{
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            tableView.getItems().setAll(coordinatorDAO.findActiveCoordinators());
        } catch (ServiceException serviceException) {
            showAlert("Error de servicio", "Ocurrió un error al cargar los coordinadores. Por favor, inténtelo de nuevo más tarde.",
                    Alert.AlertType.ERROR);
        }
    }
}
