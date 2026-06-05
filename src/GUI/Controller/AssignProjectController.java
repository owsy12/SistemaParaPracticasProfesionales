package GUI.Controller;

import Logic.DAO.ApplicationDAO;
import Logic.DAO.UserDAO;
import Logic.DTOs.Application;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;
import javafx.scene.control.ButtonType;

public class AssignProjectController {
    private static final String STATUS_PENDING = "Pendiente";

    @FXML
    private TableView<User> internsTableView;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, String> matriculaColumn;

    @FXML
    private void initialize() {
        loadPendingApplicationInterns();
    }

    @FXML
    public void assignProject(ActionEvent actionEvent) {
        User selectedUser = internsTableView.getSelectionModel().getSelectedItem();
        boolean isSelectionMissing = selectedUser == null;
        if (isSelectionMissing) {
            showAlert("Sin selección", "Seleccione un practicante de la tabla para asignar proyecto.",
                    Alert.AlertType.WARNING);
        } else {
            openInternProjectSelection(selectedUser);
        }
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmar cancelación",
                "¿Desea salir sin asignar proyecto?",
                Alert.AlertType.CONFIRMATION);
        boolean isConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isConfirmed) {
            openWelcomePage(anchorPane);
        }
    }

    private void loadPendingApplicationInterns() {
        try {
            ApplicationDAO applicationDAO = new ApplicationDAO();
            List<Application> applicationList = applicationDAO.findByStatus(STATUS_PENDING);

            if (applicationList.isEmpty()) {
                showAlert("Advertencia", "En este momento no existen solicitudes.",
                        Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);
            } else {
                loadInternsOnTable(applicationList);
            }

        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible, intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "Error al verificar practicantes con solicitudes pendientes.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadInternsOnTable(List<Application> applicationList) {
        List<User> userList = new ArrayList<>();
        try {
            UserDAO userDAO = new UserDAO();
            for (Application application : applicationList) {
                userList.add(userDAO.findById(application.getIdIntern()));
            }
            internsTableView.getItems().setAll(userList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró cargar los practicantes.", Alert.AlertType.ERROR);
        }
    }

    private void openInternProjectSelection(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/view/GUIViewInternProjectSelection.fxml"));
            Parent vista = loader.load();
            ViewInterProjectSelection controller = loader.getController();
            controller.setUser(user);
            anchorPane.getChildren().setAll(vista);
        } catch (IOException ioException) {
            showAlert("Error", "No se logró cargar la vista.", Alert.AlertType.ERROR);
        }
    }

}
