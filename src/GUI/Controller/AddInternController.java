package GUI.Controller;

import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import GUI.Utils.RestrictedPasswordField;
import GUI.Utils.RestrictedTextField;
import javafx.scene.layout.AnchorPane;
import org.mindrot.jbcrypt.BCrypt;

import GUI.Utils.AuditLog;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.ValidationUtils.getPasswordValidationMessage;
import static GUI.Utils.ViewsUtils.openWelcomePage;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AddInternController {
    private static final String STATUS_ACTIVE = "Activo";


    @FXML
    private AnchorPane anchorPane;

    @FXML
    private RestrictedTextField idTextField;
    @FXML
    private RestrictedTextField lastNameTextField;
    @FXML
    private RestrictedTextField secondLastNameTextField;
    @FXML
    private RestrictedTextField emailTextField;
    @FXML
    private RestrictedTextField firstNameTextField;
    @FXML
    private RestrictedPasswordField passwordField;
    @FXML
    private RestrictedPasswordField confirmPasswordField;
    @FXML
    private RestrictedTextField creditTextField;

    @FXML
    private void initialize() {
        setTypeAndLength(idTextField, "ID");
        setTypeAndLength(lastNameTextField, "Name");
        setTypeAndLength(secondLastNameTextField, "Name");
        setTypeAndLength(emailTextField, "Email");
        setTypeAndLength(firstNameTextField, "Name");
        setTypeAndLength(passwordField, "Password");
        setTypeAndLength(confirmPasswordField, "Password");
        setTypeAndLength(creditTextField, "Number");
    }

    @FXML
    public void cancelRegistration(ActionEvent actionEvent) {
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmar cancelación",
                "¿Desea salir? Los datos ingresados no se guardarán.",
                Alert.AlertType.CONFIRMATION);
        boolean isConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isConfirmed) {
            clear();
            openWelcomePage(anchorPane);
        }
    }

    @FXML
    public void registerIntern(ActionEvent actionEvent) {
        String passwordValidationMessage = getPasswordValidationMessage(passwordField.getText());
        boolean isPasswordInvalid = passwordValidationMessage != null;

        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Por favor complete todos los campos obligatorios.",
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

    private void registrationProcess() {
        try {
            InternDAO internDAO = new InternDAO();
            Intern intern = new Intern();
            intern.setRegistrationNumber(idTextField.getText());
            intern.setFirstName(firstNameTextField.getText());
            intern.setLastName(lastNameTextField.getText());
            intern.setSecondLastName(secondLastNameTextField.getText());
            intern.setEmail(emailTextField.getText());
            intern.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()));
            intern.setCredits(Integer.parseInt(creditTextField.getText()));
            intern.setStatus(STATUS_ACTIVE);
            intern.setRole("Practicante");

            if (internDAO.saveIntern(intern)) {
                AuditLog.record("registró al practicante con matrícula " + intern.getRegistrationNumber());
                showAlert("Éxito", "Practicante registrado exitosamente.",
                        Alert.AlertType.INFORMATION);
                clear();
            } else {
                showAlert("Error", "No se pudo registrar el practicante.",
                        Alert.AlertType.ERROR);
            }
        } catch (DuplicateEntryException duplicateEntryException) {
            String duplicateMessage = "Ya existe un practicante registrado con esa matrícula.";
            if (duplicateEntryException.isEmailDuplicated()) {
                duplicateMessage = "Ya existe un usuario registrado con ese correo electrónico.";
            }
            showAlert("Registro duplicado", duplicateMessage, Alert.AlertType.WARNING);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible", "No se pudo conectar al servicio. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {
        boolean isIdEmpty =
                idTextField.getText().isEmpty();

        boolean isCreditEmpty =
                creditTextField.getText().isEmpty();

        boolean isLastNameEmpty =
                lastNameTextField.getText().isEmpty();

        boolean isSecondLastNameEmpty =
                secondLastNameTextField.getText().isEmpty();

        boolean isEmailEmpty =
                emailTextField.getText().isEmpty();

        boolean isFirstNameEmpty =
                firstNameTextField.getText().isEmpty();

        boolean isPasswordEmpty =
                passwordField.getText().isEmpty();

        boolean isConfirmPasswordEmpty =
                confirmPasswordField.getText().isEmpty();

        boolean hasEmptyFields = isIdEmpty || isLastNameEmpty || isSecondLastNameEmpty || isEmailEmpty
                || isFirstNameEmpty || isPasswordEmpty || isConfirmPasswordEmpty;

        return hasEmptyFields;
    }

    private boolean isPasswordMatching() {
        boolean isMatching = passwordField.getText().equals(confirmPasswordField.getText());
        return isMatching;
    }

    private void clear() {
        idTextField.clear();
        lastNameTextField.clear();
        secondLastNameTextField.clear();
        emailTextField.clear();
        firstNameTextField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        creditTextField.clear();
    }
}
