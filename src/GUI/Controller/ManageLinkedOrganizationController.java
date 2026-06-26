package GUI.Controller;

import Logic.DAO.LinkedOrganizationDAO;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

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

    @FXML
    private void initialize() {
        loadOrganizations();
    }

    @FXML
    public void cancelAction() {
        openWelcomePage(anchorPane);
    }

    @FXML
    public void deleteOrganization(ActionEvent actionEvent) {
        LinkedOrganization selectedOrganization = organizationTableView.getSelectionModel().getSelectedItem();
        boolean isSelectionMissing = selectedOrganization == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione una organización de la tabla para eliminar.",
                    Alert.AlertType.WARNING);
        } else {

            String confirmationMessage = "¿Desea eliminar la organización «" + selectedOrganization.getName()
                    + "»? Se eliminarán también sus técnicos responsables." + " Esta acción es irreversible.";
            Optional<ButtonType> response = showAlertAndWait(
                    "Confirmar eliminación", confirmationMessage, Alert.AlertType.CONFIRMATION);

            boolean isUserConfirmed = false;

            if (response.isPresent()) {

                if (response.get() == ButtonType.OK) {

                    isUserConfirmed = true;

                }

            }
            if (isUserConfirmed) {
                deleteLinkedOrganizationProcess(selectedOrganization.getIdLinkedOrganization());
            }
        }
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