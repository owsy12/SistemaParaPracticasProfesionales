package GUI.Controller;

import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.mindrot.jbcrypt.BCrypt;
import static GUI.Utils.ValidationUtils.getPasswordValidationMessage;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import javafx.scene.control.Alert.AlertType;
import static GUI.Utils.Alert.showFormAlert;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class AddInternController {

    @FXML private TextField idTextField;
    @FXML private TextField lastNameTextField;
    @FXML private TextField secondLastNameTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField firstNameTextField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField creditTextField;
    @FXML private AnchorPane anchorPane;

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
        showFormAlert(AlertType.INFORMATION,
                "Registro cancelado",
                "Operaci\u00F3n cancelada",
                "El registro del practicante ha sido cancelado. Ser\u00E1s redirigido al inicio.");
        clear();
        openWelcomePage(anchorPane);
    }

    @FXML
    public void registerIntern(ActionEvent actionEvent) {
        String passwordValidationMessage = getPasswordValidationMessage(passwordField.getText());
        boolean isPasswordInvalid = passwordValidationMessage != null;

        if (hasEmptyFields()) {
            showFormAlert(AlertType.WARNING,
                    "Campos incompletos",
                    "Campos obligatorios vac\u00EDos",
                    "Por favor completa todos los campos obligatorios antes de continuar.");
        } else if (!isValidEmail(emailTextField.getText())) {
            showFormAlert(AlertType.WARNING,
                    "Correo inv\u00E1lido",
                    "Formato de correo incorrecto",
                    "Ingresa un correo electr\u00F3nico v\u00E1lido.");
        } else if (isPasswordInvalid) {
            showFormAlert(AlertType.WARNING,
                    "Contrase\u00F1a no v\u00E1lida",
                    "Requisitos de contrase\u00F1a",
                    passwordValidationMessage);
        } else if (!isPasswordMatching()) {
            showFormAlert(AlertType.WARNING,
                    "Contrase\u00F1as no coinciden",
                    "Error de confirmaci\u00F3n",
                    "La confirmaci\u00F3n de contrase\u00F1a no coincide con la contrase\u00F1a ingresada.");
        } else {
            registrationProcess();
        }
    }

    private void registrationProcess() {
        try {
            InternDAO internDAO = new InternDAO();
            Intern intern = buildIntern();
            if (internDAO.saveIntern(intern)) {
                showFormAlert(AlertType.INFORMATION,
                        "\u00C9xito",
                        "Practicante registrado",
                        "El practicante ha sido registrado exitosamente en el sistema.");
                clear();
            } else {
                showFormAlert(AlertType.ERROR,
                        "Registro fallido",
                        "No se pudo registrar",
                        "No se pudo registrar el practicante. Intenta nuevamente.");
            }
        } catch (ValidationException validationException) {
            showFormAlert(AlertType.ERROR,
                    "Error de validaci\u00F3n",
                    "Datos inv\u00E1lidos",
                    validationException.getMessage());
        } catch (ServiceException serviceException) {
            showFormAlert(AlertType.ERROR,
                    "Servicio no disponible",
                    "Error de conexi\u00F3n",
                    "No se pudo conectar al servicio. Intente m\u00E1s tarde.");
        }
    }

    private Intern buildIntern() {
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
        return intern;
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