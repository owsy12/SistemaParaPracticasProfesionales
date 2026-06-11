package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ProfessorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Professor;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class DeactivateProfessorController {

    private static final Logger LOGGER = Logger.getLogger(DeactivateProfessorController.class.getName());
    private static final String STATUS_INACTIVE = "Inactivo";

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> secondLastNameColumn;

    @FXML
    private TableView<User> tableView;

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
        loadProfessors();
    }

    @FXML
    public void inactivateProfessor(ActionEvent actionEvent) {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        boolean isSelectionMissing = selectedUser == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un profesor de la tabla para inactivar.",
                    Alert.AlertType.WARNING);
        } else {
            Optional<ButtonType> response = showAlertAndWait(
                    "Desea desactivar",
                    "¿Desea desactivar este profesor?",
                    Alert.AlertType.CONFIRMATION);

            boolean isUserConfirmed = response.isPresent() && response.get() == ButtonType.OK;
            if (isUserConfirmed) {
                selectedUser.setStatus(STATUS_INACTIVE);
                selectedUser.setRole("Profesor");
                deactivateProcess(selectedUser);
                loadProfessors();
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
                    "Usuario {0} inactivó al profesor {1}",
                    new Object[]{SessionManager.getInstance().getUser().getId(), user.getId()});
            showAlert("Profesor desactivado", "El profesor ha sido desactivado exitosamente.",
                    Alert.AlertType.INFORMATION);
        } catch (ValidationException validationException) {
            showAlert("Error", "Servicio no disponible.", Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logró desactivar.", Alert.AlertType.ERROR);
        }
    }

    private void loadProfessors() {
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            tableView.getItems().setAll(professorDAO.findActiveProfessors());
        } catch (ValidationException validationException) {
            showAlert("Error", "Servicio no disponible.", Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logró cargar los profesores.", Alert.AlertType.ERROR);
        }
    }

}
