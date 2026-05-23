package GUI.Controller;

import Logic.DAO.CoordinatorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Coordinator;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.ValidationUtils.getPasswordValidationMessage;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import Logic.DAO.ProfessorDAO;
import Logic.DTOs.Professor;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;
import java.util.Optional;

public class AddCoordinadorController {

    @FXML
    private TextField idTextField;

    @FXML
    private TextField firstNameTextField;

    @FXML
    private TextField lastNameTextField;

    @FXML
    private TextField secondLastNameTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailTextField;

    @FXML
    private void initialize() {
        setTypeAndLength(firstNameTextField, "Name");
        setTypeAndLength(lastNameTextField, "Name");
        setTypeAndLength(secondLastNameTextField, "Name");
        setTypeAndLength(idTextField, "ID");
        setTypeAndLength(passwordField, "Password");
        setTypeAndLength(confirmPasswordField, "Password");
        setTypeAndLength(emailTextField, "Email");
    }

    @FXML
    public void registerCoordinator() {
        String passwordValidationMessage = getPasswordValidationMessage(passwordField.getText());
        boolean isPasswordInvalid = passwordValidationMessage != null;

        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Por favor complete todos los campos obligatorios.",
                    AlertType.WARNING);
        } else if (!isValidEmail(emailTextField.getText())) {
            showAlert("Correo inválido", "Ingrese un correo electrónico válido.",
                    AlertType.WARNING);
        } else if (isPasswordInvalid) {
            showAlert("Contraseña no válida", passwordValidationMessage,
                    AlertType.WARNING);
        } else if (!isPasswordMatching()) {
            showAlert("Contraseñas no coinciden",
                    "La confirmación de contraseña no coincide con la contraseña ingresada.",
                    AlertType.WARNING);
        } else {
            processRegistration();
        }
    }

    @FXML
    public void showProfessorList() {
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            List<Professor> professors = professorDAO.findProfessorsWithoutCoordinatorRole();

            if (professors.isEmpty()) {
                showAlert("Información", "No hay profesores disponibles para asignar como coordinador.",
                        AlertType.INFORMATION);
            } else {
                ChoiceDialog<Professor> dialog = new ChoiceDialog<>(professors.get(0), professors);
                dialog.setTitle("Seleccionar Profesor");
                dialog.setHeaderText("Profesores activos sin rol de coordinador");
                dialog.setContentText("Seleccione un profesor:");
                Optional<Professor> result = dialog.showAndWait();

                if (result.isPresent()) {
                    addRolToProfessor(result.get());
                    showAlert("Rol asignado", "El profesor ha sido asignado como coordinador exitosamente.",
                            AlertType.INFORMATION);
                }
            }

        } catch (ServiceException serviceException) {
            String serviceErrorMessage = "Error al recuperar profesores: " + serviceException.getMessage();
            showAlert("Error", serviceErrorMessage, AlertType.ERROR);
        } catch (ValidationException validationException) {
            String validationErrorMessage = "Error al validar datos: " + validationException.getMessage();
            showAlert("Error de validación", validationErrorMessage, AlertType.ERROR);
        }
    }

    private void addRolToProfessor(Professor professor) {
        try {
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            professor.setRole("Coordinador");
            userRoleDAO.saveUserRole(professor);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible", "No se pudo asignar el rol. Intente más tarde.",
                    AlertType.ERROR);
        }
    }

    @FXML
    public void cancelRegistration() {
        showAlert("Registro cancelado", "La operación ha sido cancelada.",
                AlertType.INFORMATION);
        clearFields();
    }

    private void processRegistration() {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            Coordinator coordinator = new Coordinator();
            coordinator.setMatricula(idTextField.getText());
            coordinator.setFirstName(firstNameTextField.getText());
            coordinator.setLastName(lastNameTextField.getText());
            coordinator.setSecondLastName(secondLastNameTextField.getText());
            coordinator.setEmail(emailTextField.getText());
            coordinator.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()));
            coordinator.setStatus("Activo");
            coordinator.setRole("Coordinador");

            if (coordinatorDAO.save(coordinator)) {
                showAlert("Registro exitoso", "Coordinador registrado exitosamente.",
                        AlertType.INFORMATION);
                clearFields();
            } else {
                showAlert("Error", "No se pudo realizar el registro.",
                        AlertType.ERROR);
            }

        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible", "Error al conectar con la base de datos. Intente más tarde.",
                    AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {
        boolean isIdEmpty = idTextField.getText().isEmpty();
        boolean isFirstNameEmpty = firstNameTextField.getText().isEmpty();
        boolean isLastNameEmpty = lastNameTextField.getText().isEmpty();
        boolean isSecondLastNameEmpty = secondLastNameTextField.getText().isEmpty();
        boolean isEmailEmpty = emailTextField.getText().isEmpty();
        boolean isPasswordEmpty = passwordField.getText().isEmpty();
        boolean isConfirmPasswordEmpty = confirmPasswordField.getText().isEmpty();

        boolean hasEmpty = isIdEmpty || isFirstNameEmpty || isLastNameEmpty
                || isSecondLastNameEmpty || isEmailEmpty || isPasswordEmpty || isConfirmPasswordEmpty;

        return hasEmpty;
    }

    private boolean isPasswordMatching() {
        boolean isMatching = passwordField.getText().equals(confirmPasswordField.getText());
        return isMatching;
    }

    private void clearFields() {
        idTextField.clear();
        firstNameTextField.clear();
        lastNameTextField.clear();
        secondLastNameTextField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        emailTextField.clear();
    }

}
