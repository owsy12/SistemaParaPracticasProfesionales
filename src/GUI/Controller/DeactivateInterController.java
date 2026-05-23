package GUI.Controller;

import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
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

import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;

public class DeactivateInterController {

    @FXML
    private TableView<User> internsTabeView;

    @FXML
    private TableColumn<User, Void> actionColumn;

    @FXML
    private TableColumn<User, String> tagColumn;

    @FXML
    private TableColumn<User, String> interFullNameColumn;

    @FXML
    private void initialize() {
        configureTable();
        configureListeners();
    }

    private void configureTable() {
        loadActiveInterns();
        configureDataColumns();
        addButtonToRow();
    }

    private void configureListeners() {
    }

    private void loadActiveInterns() {
        try {
            InternDAO internDAO = new InternDAO();
            List<Intern> internList = internDAO.findAllActiveinterns();
            internsTabeView.getItems().setAll(internList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Error en el servicio, intente nuevamente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró recuperar los practicantes.",
                    Alert.AlertType.ERROR);
        }
    }

    private void configureDataColumns() {
        tagColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getMatricula());
            }
        });

        interFullNameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                return new SimpleStringProperty(
                        cellData.getValue().getFirstName()
                        + cellData.getValue().getLastName()
                        + cellData.getValue().getSecondLastName());
            }
        });
    }

    private void addButtonToRow() {
        actionColumn.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> column) {
                return new TableCell<User, Void>() {
                    private final Button button = new Button("Inactivar");

                    {
                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                User user = getTableView().getItems().get(getIndex());
                                Optional<ButtonType> confirmationResponse = showAlertAndWait(
                                        "Advertencia",
                                        "¿Está seguro que desea inactivar este practicante?",
                                        Alert.AlertType.CONFIRMATION);

                                if (confirmationResponse.isPresent()
                                        && confirmationResponse.get() == ButtonType.OK) {
                                    inactiveProcess(user);
                                    internsTabeView.refresh();
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

    private void inactiveProcess(User user) {
        try {
            InternDAO internDAO = new InternDAO();
            internDAO.deactivateIntern(user.getId());
        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible por el momento, intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró comprobar al practicante.",
                    Alert.AlertType.ERROR);
        }
    }

}
