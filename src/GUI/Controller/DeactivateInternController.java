package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
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

import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class DeactivateInternController {

    private static final Logger LOGGER = Logger.getLogger(DeactivateInternController.class.getName());

    @FXML
    private TableView<User> internsTableView;

    @FXML
    private TableColumn<User, String> tagColumn;

    @FXML
    private TableColumn<User, String> interFullNameColumn;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private void initialize() {
        loadActiveInterns();
    }

    @FXML
    public void inactivateIntern(ActionEvent actionEvent) {
        User selectedUser = internsTableView.getSelectionModel().getSelectedItem();
        boolean isSelectionMissing = selectedUser == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un practicante de la tabla para inactivar.",
                    Alert.AlertType.WARNING);
        } else {
            Optional<ButtonType> confirmationResponse = showAlertAndWait("Advertencia",
                    "¿Está seguro que desea inactivar este practicante?",
                    Alert.AlertType.CONFIRMATION);

            boolean isUserConfirmed = false;
            if (confirmationResponse.isPresent()) {
                if (confirmationResponse.get() == ButtonType.OK) {
                    isUserConfirmed = true;
                }
            }
            if (isUserConfirmed) {
                inactiveProcess(selectedUser);
                loadActiveInterns();
            }
        }
    }

    @FXML
    public void cancelOperation(ActionEvent actionEvent) {
        openWelcomePage(anchorPane);
    }

    private void loadActiveInterns() {
        try {
            InternDAO internDAO = new InternDAO();
            List<Intern> internList = internDAO.findAllActiveinterns();
            internsTableView.getItems().setAll(internList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Error en el servicio, intente nuevamente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró recuperar los practicantes.",
                    Alert.AlertType.ERROR);
        }
    }

    private void inactiveProcess(User user) {
        try {
            InternDAO internDAO = new InternDAO();
            internDAO.deactivateIntern(user.getIdUser());
            LOGGER.log(Level.INFO,
                    "User {0} deactivated intern {1}",
                    new Object[]{SessionManager.getInstance().getUser().getIdUser(), user.getIdUser()});
        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible por el momento, intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró comprobar al practicante.",
                    Alert.AlertType.ERROR);
        }
    }

}
