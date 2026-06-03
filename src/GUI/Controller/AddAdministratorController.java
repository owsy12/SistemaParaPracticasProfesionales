package GUI.Controller;

import GUI.Utils.RestrictedPasswordField;
import GUI.Utils.RestrictedTextField;
import GUI.Utils.ViewsUtils;
import Logic.DAO.UserDAO;
import Logic.DTOs.User;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.mindrot.jbcrypt.BCrypt;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.getPasswordValidationMessage;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.ValidationUtils.setTypeAndLength;

public class AddAdministratorController {

    private static final String LOGIN_FXML_PATH = "/GUI/View/GUILogin.fxml";
    private static final String LOGIN_WINDOW_TITLE = "Inicio de Sesión — SGPP";
    private static final String STATUS_ACTIVE = "Activo";
    private static final String ROLE_ADMINISTRATOR = "Administrador";

    @FXML
    private RestrictedTextField idTextField;

    @FXML
    private RestrictedTextField firstNameTextField;

    @FXML
    private RestrictedTextField lastNameTextField;

    @FXML
    private RestrictedTextField secondLastNameTextField;

    @FXML
    private RestrictedTextField emailTextField;

    @FXML
    private RestrictedPasswordField passwordField;

    @FXML
    private RestrictedPasswordField confirmPasswordField;

    @FXML
    private void initialize() {
        setTypeAndLength(idTextField, "ID");
        setTypeAndLength(firstNameTextField, "Name");
        setTypeAndLength(lastNameTextField, "Name");
        setTypeAndLength(secondLastNameTextField, "Name");
        setTypeAndLength(emailTextField, "Email");
        setTypeAndLength(passwordField, "Password");
        setTypeAndLength(confirmPasswordField, "Password");
    }

    @FXML
    public void registerAdministrator() {
        String passwordValidationMessage = getPasswordValidationMessage(passwordField.getText());
        boolean isPasswordInvalid = passwordValidationMessage != null;

        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Por favor, completa todos los campos obligatorios.",
                    Alert.AlertType.WARNING);
        } else if (!isValidEmail(emailTextField.getText())) {
            showAlert("Correo inválido", "Ingrese un correo electrónico válido.",
                    Alert.AlertType.WARNING);
        } else if (isPasswordInvalid) {
            showAlert("Contraseña no válida", passwordValidationMessage,
                    Alert.AlertType.WARNING);
        } else if (!isPasswordMatching()) {
            showAlert("Contraseñas no coinciden",
                    "La confirmación de contraseña no coincide con la contraseña ingresada.",
                    Alert.AlertType.WARNING);
        } else {
            registrationProcess();
        }
    }

    @FXML
    public void cancelRegistration() {
        ViewsUtils.openWindow(LOGIN_FXML_PATH, LOGIN_WINDOW_TITLE, idTextField);
    }

    private void registrationProcess() {
        try {
            UserDAO userDAO = new UserDAO();
            boolean systemAlreadyHasUsers = !userDAO.findAll().isEmpty();

            if (systemAlreadyHasUsers) {
                showAlert("Operación no permitida",
                        "El administrador inicial ya fue registrado. Esta acción solo puede realizarse una vez.",
                        Alert.AlertType.WARNING);
            } else {
                User administrator = new User();
                administrator.setMatricula(idTextField.getText());
                administrator.setFirstName(firstNameTextField.getText());
                administrator.setLastName(lastNameTextField.getText());
                administrator.setSecondLastName(secondLastNameTextField.getText());
                administrator.setEmail(emailTextField.getText());
                administrator.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()));
                administrator.setStatus(STATUS_ACTIVE);
                administrator.setRole(ROLE_ADMINISTRATOR);

                boolean isRegistered = userDAO.saveUser(administrator) > 0;
                if (isRegistered) {
                    showAlert("Éxito", "Administrador registrado exitosamente.",
                            Alert.AlertType.INFORMATION);
                    ViewsUtils.openWindow(LOGIN_FXML_PATH, LOGIN_WINDOW_TITLE, idTextField);
                } else {
                    showAlert("Error", "No se pudo registrar el administrador.",
                            Alert.AlertType.ERROR);
                }
            }
        } catch (DuplicateEntryException duplicateEntryException) {
            String duplicateMessage = "Ya existe un usuario registrado con esa matrícula.";
            if (duplicateEntryException.isEmailDuplicated()) {
                duplicateMessage = "Ya existe un usuario registrado con ese correo electrónico.";
            }
            showAlert("Registro duplicado", duplicateMessage, Alert.AlertType.WARNING);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible",
                    "No se pudo conectar al servicio. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {
        boolean isIdEmpty = idTextField.getText().isEmpty();
        boolean isFirstNameEmpty = firstNameTextField.getText().isEmpty();
        boolean isLastNameEmpty = lastNameTextField.getText().isEmpty();
        boolean isEmailEmpty = emailTextField.getText().isEmpty();
        boolean isPasswordEmpty = passwordField.getText().isEmpty();
        boolean isConfirmPasswordEmpty = confirmPasswordField.getText().isEmpty();

        boolean hasEmpty = isIdEmpty || isFirstNameEmpty || isLastNameEmpty
                || isEmailEmpty || isPasswordEmpty || isConfirmPasswordEmpty;
        return hasEmpty;
    }

    private boolean isPasswordMatching() {
        boolean isMatching = passwordField.getText().equals(confirmPasswordField.getText());
        return isMatching;
    }

}
