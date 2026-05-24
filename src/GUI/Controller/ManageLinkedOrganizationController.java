package GUI.Controller;

import Logic.DAO.LinkedOrganizationDAO;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
    private AnchorPane anchorPane;

    @FXML
    private TableColumn<LinkedOrganization, String> nameColumn;

    @FXML
    private TableColumn<LinkedOrganization, String> statusColumn;

    @FXML
    private TableColumn<LinkedOrganization, String> emailColumn;

    @FXML
    private TableView<LinkedOrganization> organizationTableView;

    private LinkedOrganization selectedOrganization;

    @FXML
    private void initialize() {
        configureDataColumns();
        configureListeners();
        loadOrganizations();
    }

    @FXML
    public void cancelAction() {
        openWelcomePage(anchorPane);
    }

    @FXML
    public void deleteOrganization(ActionEvent actionEvent) {
        boolean isSelectionMissing = selectedOrganization == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione una organización de la tabla para eliminar.",
                    Alert.AlertType.WARNING);
            return;
        }

        String confirmationMessage = "¿Desea eliminar la organización «"
                + selectedOrganization.getName()
                + "»? Se eliminarán también sus técnicos responsables."
                + " Esta acción es irreversible.";
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmar eliminación", confirmationMessage, Alert.AlertType.CONFIRMATION);

        if (response.isPresent() && response.get() == ButtonType.OK) {
            deleteLinkedOrganizationProcess(selectedOrganization.getIdLinkedOrganization());
        }
    }

    private void configureDataColumns() {
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
    }

    private void configureListeners() {
        organizationTableView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<LinkedOrganization>() {
                    @Override
                    public void changed(ObservableValue<? extends LinkedOrganization> observable,
                                        LinkedOrganization oldValue, LinkedOrganization newValue) {
                        selectedOrganization = newValue;
                    }
                });
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
                organizationTableView.getItems().setAll(organizationList);
            }

        } catch (ServiceException serviceException) {
            showAlert("Error de servicio",
                    "No se pudieron cargar las organizaciones vinculadas.",
                    Alert.AlertType.ERROR);
        }
    }

    private void deleteLinkedOrganizationProcess(int idLinkedOrganization) {
        try {
            LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();
            linkedOrganizationDAO.deleteLinkedOrganization(idLinkedOrganization);
            selectedOrganization = null;
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
