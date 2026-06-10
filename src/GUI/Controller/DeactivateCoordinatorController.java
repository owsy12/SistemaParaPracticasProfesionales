package GUI.Controller;

import Logic.DAO.CoordinatorDAO;
import GUI.Utils.AuditLog;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class DeactivateCoordinatorController {
    private static final String STATUS_INACTIVE = "Inactivo";

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> secondLastNameColumn;

    @FXML
    private TableView<User> tableView;

    @FXML
    private TableColumn<User, String> lastNameColumn;

    @FXML
    private TableColumn<User, String> tagColumn;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private void initialize() {
        loadCoordinators();
    }

    @FXML
    public void inactivateCoordinator(ActionEvent actionEvent) {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        boolean isSelectionMissing = selectedUser == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un coordinador de la tabla para inactivar.",
                    Alert.AlertType.WARNING);
        } else {
            Optional<ButtonType> response = showAlertAndWait(
                    "Desea desactivar",
                    "¿Desea desactivar este coordinador?",
                    Alert.AlertType.CONFIRMATION);

            boolean isUserConfirmed = response.isPresent() && response.get() == ButtonType.OK;
            if (isUserConfirmed) {
                selectedUser.setStatus(STATUS_INACTIVE);
                selectedUser.setRole("Coordinador");
                deactivateProcess(selectedUser);
                loadCoordinators();
            }
        }
    }

    @FXML
    public void cancelOperation(ActionEvent actionEvent) {
        showAlert("Información", "Operación cancelada.", Alert.AlertType.INFORMATION);
        openWelcomePage(anchorPane);
    }

    private void deactivateProcess(User user) {
        try {
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            userRoleDAO.updateUserRolStatus(user);
            AuditLog.record("inactivó al coordinador " + user.getId());
            showAlert("Coordinador desactivado",
                    "El coordinador ha sido desactivado exitosamente.",
                    Alert.AlertType.INFORMATION);
        } catch (ServiceException serviceException) {
            showAlert("Error de servicio",
                    "Ocurrió un error al intentar desactivar el coordinador. Intente de nuevo más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    "Los datos proporcionados no son válidos. Revise la información e intente nuevamente.",
                    Alert.AlertType.WARNING);
        }
    }

    private void loadCoordinators() {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            tableView.getItems().setAll(coordinatorDAO.findActiveCoordinators());
        } catch (ServiceException serviceException) {
            showAlert("Error de servicio",
                    "Ocurrió un error al cargar los coordinadores. Intente de nuevo más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

}
