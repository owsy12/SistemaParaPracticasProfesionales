package GUI.Controller;

import GUI.SessionManager.SessionManager;
import GUI.Utils.RestrictedPasswordField;
import GUI.Utils.RestrictedTextField;
import Logic.DAO.UserDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Level;
import java.util.logging.Logger;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.Alert.showAlert;

public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    @FXML
    private RestrictedTextField userTextField;
    @FXML
    private RestrictedPasswordField passwordField;
    @FXML
    private Button registerAdminButton;
    private User currentUser;

    @FXML
    private void initialize() {
        setTypeAndLength(userTextField, "Email");
        setTypeAndLength(passwordField, "Password");
        updateAdminRegistrationVisibility();
    }

    @FXML
    public void clickRegisterAdmin(ActionEvent actionEvent) {
        openAdministratorRegistration();
        updateAdminRegistrationVisibility();
    }

    private void updateAdminRegistrationVisibility() {
        boolean noUsersExist = hasNoRegisteredUsers();
        registerAdminButton.setVisible(noUsersExist);
        registerAdminButton.setManaged(noUsersExist);
    }

    private boolean hasNoRegisteredUsers() {
        boolean noUsersExist = false;
        try {
            UserDAO userDAO = new UserDAO();
            noUsersExist = userDAO.findAll().isEmpty();
        } catch (ServiceException serviceException) {
            showAlert("Error de servicio",
                    "No se pudo verificar el estado del sistema. Inténtalo de nuevo más tarde.",
                    Alert.AlertType.ERROR);
        }
        return noUsersExist;
    }

    private void openAdministratorRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/View/GUIAddAdministrator.fxml"));
            Parent root = loader.load();
            Stage mainStage = new Stage();
            mainStage.setScene(new Scene(root));
            mainStage.setTitle("Registrar Administrador");
            mainStage.show();
            Stage loginStage = (Stage) userTextField.getScene().getWindow();
            loginStage.close();
        } catch (Exception e) {
            showAlert("Error", "Error al abrir, intente más tarde.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void clickLogin(ActionEvent actionEvent) {
        if (isEmpty()) {
            showAlert("Campos vacíos", "Por favor, completa todos los campos obligatorios.",
                   Alert.AlertType.WARNING);
        } else if (loginProcess()) {
            openWindow("GUIMainPage.fxml", "Menú Principal");
        }
    }

    private boolean loginProcess() {
        boolean isValidUser = false;

        try {
            UserDAO userDao = new UserDAO();
            currentUser = userDao.findByIdentifier(userTextField.getText());
            UserRoleDAO userRoleDao = new UserRoleDAO();
            currentUser.setRoles(userRoleDao.getActiveRolsByUserId(currentUser.getId()));

            boolean isPasswordValid = BCrypt.checkpw(passwordField.getText(), currentUser.getPassword());
            if (!isPasswordValid) {
                throw new ValidationException("Contraseña incorrecta");
            } else {
                boolean hasActiveRole = currentUser.getRoles() != null
                        && !currentUser.getRoles().isEmpty();
                if (!hasActiveRole) {
                    LOGGER.log(Level.WARNING,
                            "Acceso denegado: el usuario {0} no tiene ningún rol activo.",
                            currentUser.getMatricula());
                    showAlert("Acceso denegado",
                            "No cuenta con ningún rol activo dentro del sistema. Contacte al administrador.",
                            Alert.AlertType.WARNING);
                } else {
                    SessionManager.getInstance().login(currentUser);
                    isValidUser = true;
                }
            }

        } catch (ServiceException e) {
            showAlert("Error de servicio",
                    "Ocurrió un error al procesar la solicitud. Inténtalo de nuevo más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException e) {
            showAlert("Error de validación",
                    "Usuario o contraseña incorrectos. Verifica tu información e inténtalo de nuevo.",
                    Alert.AlertType.ERROR);
        } catch (NullPointerException e) {
            showAlert("Error de autenticación",
                    "Usuario no encontrado. Verifica tu matrícula e inténtalo de nuevo.",
                    Alert.AlertType.ERROR);
            clearFields();
        }

        return isValidUser;
    }

    private boolean isEmpty() {
        boolean empty = false;

        if (userTextField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            empty = true;
        }

        return empty;
    }

    public void openWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/View/" + fxml));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            Stage mainStage = new Stage();
            mainStage.setScene(new Scene(root));
            mainStage.setTitle(title);
            mainStage.setMinWidth(1024.0);
            mainStage.setMinHeight(720.0);
            mainStage.setMaximized(true);
            controller.setCurrentUser(currentUser);
            mainStage.show();
            Stage loginStage = (Stage) userTextField.getScene().getWindow();
            loginStage.close();
        } catch (Exception e) {
            showAlert("Error", "Error al abrir, intente más tarde.", Alert.AlertType.ERROR);
        }
    }

    private void clearFields() {
        userTextField.clear();
        passwordField.clear();
    }
}
