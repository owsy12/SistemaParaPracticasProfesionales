package GUI.Controller;

import Logic.DAO.CoordinatorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.Optional;

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
        configureDataColumns();
        loadCoordinators();
        addButtonToTable();
    }

    private void configureDataColumns() {
        tagColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getMatricula());
            }
        });

        nameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getLastName());
            }
        });

        lastNameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getLastName());
            }
        });

        secondLastNameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getSecondLastName());
            }
        });
    }

    private void addButtonToTable() {
        actionsColumn.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> column) {
                return new TableCell<User, Void>() {
                    private final Button inactivateButton = new Button("Inactivar");

                    {
                        inactivateButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                User user = getTableView().getItems().get(getIndex());
                                Optional<ButtonType> response = showAlertAndWait(
                                        "Desea desactivar", "¿Desea desactivar este coordinador?",
                                        Alert.AlertType.CONFIRMATION);

                                if (response.isPresent() && response.get() == ButtonType.OK) {
                                    user.setStatus("Inactivo");
                                    user.setRole("Coordinador");
                                    deactivateProcess(user);
                                    loadCoordinators();
                                }
                            }
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
                };
            }
        });
    }

    private void deactivateProcess(User user) {
        try {
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            userRoleDAO.updateUserRolStatus(user);
            showAlert("Coordinador desactivado",
                    "El coordinador ha sido desactivado exitosamente.",
                    Alert.AlertType.INFORMATION);
        } catch (ServiceException serviceException) {
            showAlert("Error de servicio",
                    "Ocurrió un error al intentar desactivar el coordinador. Por favor, inténtelo de nuevo más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    "Los datos proporcionados no son válidos. Por favor, revise la información e intente nuevamente.",
                    Alert.AlertType.WARNING);
        }
    }

    private void loadCoordinators() {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            tableView.getItems().setAll(coordinatorDAO.findActiveCoordinators());
        } catch (ServiceException serviceException) {
            showAlert("Error de servicio",
                    "Ocurrió un error al cargar los coordinadores. Por favor, inténtelo de nuevo más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

}
