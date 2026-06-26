package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.CoordinatorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Coordinator;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class DeactivateCoordinatorController {

    private static final Logger LOGGER = Logger.getLogger(DeactivateCoordinatorController.class.getName());
    private static final String STATUS_INACTIVE = "Inactivo";
    private static final String NO_COORDINATOR = "—";

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Label registrationNumberLabel;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Button inactivateButton;

    private User activeCoordinator;

    @FXML
    private void initialize() {
        loadCoordinator();
    }

    @FXML
    public void inactivateCoordinator(ActionEvent actionEvent) {
        boolean isCoordinatorMissing = activeCoordinator == null;
        if (isCoordinatorMissing) {
            showAlert("Sin coordinador activo",
                    "No hay un coordinador activo registrado en el sistema.",
                    Alert.AlertType.WARNING);
        } else {
            Optional<ButtonType> response = showAlertAndWait(
                    "Desea desactivar",
                    "¿Desea desactivar este coordinador?",
                    Alert.AlertType.CONFIRMATION);

            boolean isUserConfirmed = false;

            if (response.isPresent()) {

                if (response.get() == ButtonType.OK) {

                    isUserConfirmed = true;

                }

            }
            if (isUserConfirmed) {
                activeCoordinator.setStatus(STATUS_INACTIVE);
                activeCoordinator.setRole("Coordinador");
                deactivateProcess(activeCoordinator);
                loadCoordinator();
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
            LOGGER.log(Level.INFO,
                    "User {0} deactivated coordinator {1}",
                    new Object[]{SessionManager.getInstance().getUser().getIdUser(), user.getIdUser()});
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

    private void loadCoordinator() {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            List<Coordinator> coordinators = coordinatorDAO.findActiveCoordinators();
            activeCoordinator = null;
            if (!coordinators.isEmpty()) {
                activeCoordinator = coordinators.get(0);
            }
            showCoordinator();
        } catch (ServiceException serviceException) {
            showAlert("Error de servicio",
                    "Ocurrió un error al cargar el coordinador activo. Intente de nuevo más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void showCoordinator() {
        boolean hasCoordinator = activeCoordinator != null;
        if (hasCoordinator) {
            registrationNumberLabel.setText(activeCoordinator.getRegistrationNumber());
            String fullName = activeCoordinator.getFirstName() + " "
                    + activeCoordinator.getLastName() + " "
                    + activeCoordinator.getSecondLastName();
            fullNameLabel.setText(fullName);
        } else {
            registrationNumberLabel.setText(NO_COORDINATOR);
            fullNameLabel.setText("No hay un coordinador activo registrado.");
        }
        inactivateButton.setDisable(!hasCoordinator);
    }
}
