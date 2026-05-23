package GUI.Controller;

import Logic.DAO.ProfessorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Professor;
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
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class DeactivateProfessorController {

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    @FXML
    private TableColumn<User, String> secondLastNameColumn;

    @FXML
    private TableView tableView;

    @FXML
    private TableColumn<Professor, String> academicDegreeColumn;

    @FXML
    private TableColumn<User, String> lastNameColumn;

    @FXML
    private TableColumn<User, String> tagColumn;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private void initialize() {
        configureDataColumns();
        loadProfessors();
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

        academicDegreeColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Professor, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Professor, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getAcademicArea());
            }
        });
    }

    private void addButtonToTable() {
        actionsColumn.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> column) {
                return new TableCell<User, Void>() {
                    private final Button button = new Button("Inactivar");

                    {
                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                User user = getTableView().getItems().get(getIndex());
                                Optional<ButtonType> response = showAlertAndWait(
                                        "Desea desactivar",
                                        "¿Desea desactivar este profesor?",
                                        Alert.AlertType.CONFIRMATION);

                                if (response.isPresent() && response.get() == ButtonType.OK) {
                                    user.setStatus("Inactivo");
                                    user.setRole("Profesor");
                                    deactivateProcess(user);
                                    loadProfessors();
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
                            setGraphic(button);
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
            showAlert("Profesor desactivado", "El profesor ha sido desactivado exitosamente.",
                    Alert.AlertType.INFORMATION);
        } catch (ValidationException validationException) {
            showAlert("Error", "Servicio no disponible.",
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logró desactivar.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadProfessors() {
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            tableView.getItems().setAll(professorDAO.findActiveProfessors());
        } catch (ValidationException validationException) {
            showAlert("Error", "Servicio no disponible.",
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logró desactivar.",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void calcelOperation(ActionEvent actionEvent) {
        showAlert("Información", "Operación cancelada.",
                Alert.AlertType.INFORMATION);
        openWelcomePage(anchorPane);
    }

}
