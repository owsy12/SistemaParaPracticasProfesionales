package GUI.Controller;

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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.Alert.showAlert;

public class LoginController {
    @FXML
    private TextField userTextField;
    @FXML
    private PasswordField passwordField;
    private User currentUser;

    @FXML
    private void initialize() {
        setTypeAndLength(userTextField,"Email");
        setTypeAndLength(passwordField,"Text");
    }

    @FXML
    public void clickLogin(ActionEvent actionEvent) {
        if (isEmpty()){
            showAlert("Campos vacíos", "Por favor, completa todos los campos obligatorios.",
                   Alert.AlertType.WARNING);
        }else if (loginProcess()){
            openWindow("GUIMainPage.fxml", "Menú Principal");
        }else {
            showAlert("Error inesperado","Estamos teniendo problemas inten†e mas tarde", Alert.AlertType.WARNING);
        }
    }

    private boolean loginProcess(){
        boolean isValidUser = false;

        try{
            UserDAO user = new UserDAO();
            currentUser = user.findByMatricula(userTextField.getText());

            if (currentUser == null){
                currentUser = user.findByEmail(userTextField.getText());
            }

            getRoles();

            if(BCrypt.checkpw(passwordField.getText(), currentUser.getPassword())){
                isValidUser = true;
            }else {
                throw new ValidationException("Contraseña incorrecta");
            }

        } catch (ServiceException e){
            showAlert("Error de servicio", "Ocurrió un error al procesar la solicitud. Inténtalo de nuevo más tarde.",
                    Alert.AlertType.ERROR);
        }catch (ValidationException e){
            showAlert("Error de validación", "Usuario o contraseña incorrectos. Verifica tu información e inténtalo de nuevo.",
                    Alert.AlertType.ERROR);
        }catch (NullPointerException e){
            showAlert("Error de autenticación", "Usuario no encontrado. Verifica tu matrícula e inténtalo de nuevo.",
                    Alert.AlertType.ERROR);
            clearFields();
        }

        return isValidUser;
    }

    private boolean isEmpty(){
        boolean empty = false;

        if (userTextField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            empty = true;
        }

        return empty;
    }

    private void getRoles(){
        try {
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            currentUser.setRoles(userRoleDAO.getActiveRolsByUserId(currentUser.getId()));
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    public void openWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/View/" + fxml));
            Parent root = loader.load();
            MainMenuController controller = loader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            controller.setCurrentUser(currentUser);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        userTextField.clear();
        passwordField.clear();
    }
}
