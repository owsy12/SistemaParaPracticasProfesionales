package GUI.Controller;

import Logic.DAO.LinkedOrganizationDAO;
import Logic.DTOs.LinkedOrganization;
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

import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class ManageLinkedOrganizationController {

    @FXML
    private TableColumn<LinkedOrganization, String> addressColumn;

    @FXML
    private TableColumn<LinkedOrganization, String> sectorColumn;

    @FXML
    private TableColumn<LinkedOrganization, Void> actionColumn;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TableColumn<LinkedOrganization, String> nameColumn;

    @FXML
    private TableColumn<LinkedOrganization, String> statusColumn;

    @FXML
    private TableColumn<LinkedOrganization, String> emailColumn;

    @FXML
    private TableView<LinkedOrganization> organizationTableView;

    @FXML
    private void initialize() {
        loadOrganizations();
    }

    @FXML
    public void cancelAction() {
        openWelcomePage(anchorPane);
    }

    private void loadOrganizations() {
        try {
            LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();
            List<LinkedOrganization> organizationList = linkedOrganizationDAO.findAll();

            if (organizationList.isEmpty()) {
                showAlert("Sin registros",
                        "No hay organizaciones vinculadas registradas.",
                        Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);
            } else {
                configureDataColumn();
                organizationTableView.getItems().setAll(organizationList);
            }

        } catch (ServiceException serviceException) {
            showAlert("Error de servicio",
                    "No se pudieron cargar las organizaciones vinculadas.",
                    Alert.AlertType.ERROR);
        }
    }

    private void configureDataColumn() {
        nameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<LinkedOrganization, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<LinkedOrganization, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getName());
            }
        });

        emailColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<LinkedOrganization, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<LinkedOrganization, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getEmail());
            }
        });

        addressColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<LinkedOrganization, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<LinkedOrganization, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getAddress());
            }
        });

        sectorColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<LinkedOrganization, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<LinkedOrganization, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getSector());
            }
        });

        statusColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<LinkedOrganization, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<LinkedOrganization, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getStatus());
            }
        });

        actionColumn.setCellFactory(
                new Callback<TableColumn<LinkedOrganization, Void>,
                        TableCell<LinkedOrganization, Void>>() {
            @Override
            public TableCell<LinkedOrganization, Void> call(
                    TableColumn<LinkedOrganization, Void> column) {
                return new TableCell<LinkedOrganization, Void>() {
                    private final Button deleteButton = new Button("Eliminar");

                    {
                        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                LinkedOrganization organization =
                                        getTableView().getItems().get(getIndex());

                                Optional<ButtonType> response = showAlertAndWait(
                                        "Confirmar eliminación",
                                        "¿Desea eliminar la organización «" + organization.getName()
                                                + "»? Se eliminarán también sus técnicos responsables."
                                                + " Esta acción es irreversible.",
                                        Alert.AlertType.CONFIRMATION);

                                if (response.isPresent() && response.get() == ButtonType.OK) {
                                    deleteLinkedOrganizationProcess(
                                            organization.getIdLinkedOrganization());
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
                            setGraphic(deleteButton);
                        }
                    }
                };
            }
        });
    }

    private void deleteLinkedOrganizationProcess(int idLinkedOrganization) {
        try {
            LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();
            linkedOrganizationDAO.deleteLinkedOrganization(idLinkedOrganization);
            showAlert("Eliminación exitosa",
                    "La organización y sus técnicos responsables fueron eliminados.",
                    Alert.AlertType.INFORMATION);
            loadOrganizations();

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(),
                    Alert.AlertType.WARNING);
        } catch (ServiceException serviceException) {
            showAlert("No se pudo eliminar",
                    serviceException.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

}
