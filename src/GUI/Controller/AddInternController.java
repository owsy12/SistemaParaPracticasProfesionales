package GUI.Controller;

import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.ValidationUtils.getPasswordValidationMessage;

public class AddInternController {

    @FXML
    private TextField idTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField secondLastNameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField firstNameTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField creditTextField;

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
        clear();
    }

    @FXML
    public void registerIntern(ActionEvent actionEvent) {
        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Por favor complete todos los campos obligatorios.",
                    Alert.AlertType.WARNING);
        } else if (!isValidEmail(emailTextField.getText())) {
            showAlert("Correo inválido", "Ingrese un correo electrónico válido.",
                    Alert.AlertType.WARNING);
        } else if (getPasswordValidationMessage(passwordField.getText()) != null) {
            showAlert("Contraseña no válida",
                    getPasswordValidationMessage(passwordField.getText()),
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
            intern.setMatricula(idTextField.getText());
            intern.setFirstName(firstNameTextField.getText());
            intern.setLastName(lastNameTextField.getText());
            intern.setSecondLastName(secondLastNameTextField.getText());
            intern.setEmail(emailTextField.getText());
            intern.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()));
            intern.setCredits(Integer.parseInt(creditTextField.getText()));
            intern.setStatus("Activo");
            intern.setRole("Practicante");

            if (internDAO.saveIntern(intern)) {
                showAlert("Éxito", "Practicante registrado exitosamente.",
                        Alert.AlertType.INFORMATION);
                clear();
            } else {
                showAlert("Error", "No se pudo registrar el practicante.",
                        Alert.AlertType.ERROR);
            }
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible", "No se pudo conectar al servicio. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {
        boolean isEmpty = idTextField.getText().isEmpty()
                || creditTextField.getText().isEmpty()
                || lastNameTextField.getText().isEmpty()
                || secondLastNameTextField.getText().isEmpty()
                || emailTextField.getText().isEmpty()
                || firstNameTextField.getText().isEmpty()
                || passwordField.getText().isEmpty()
                || confirmPasswordField.getText().isEmpty();
        return isEmpty;
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
