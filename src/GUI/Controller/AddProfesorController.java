package GUI.Controller;

import Logic.DAO.ProfessorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Professor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.ValidationUtils.getPasswordValidationMessage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import Logic.DAO.CoordinatorDAO;
import Logic.DTOs.Coordinator;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;
import java.util.Optional;

public class AddProfesorController {
    @FXML
    private TextField idTextField;
    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField secondLastNameTextField;
    @FXML
    private TextField academicAreaTextField;
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
        setTypeAndLength(academicAreaTextField, "Name");
        setTypeAndLength(idTextField, "ID");
        setTypeAndLength(passwordField, "Password");
        setTypeAndLength(confirmPasswordField, "Password");
        setTypeAndLength(emailTextField, "Email");
    }

    @FXML
    public void registerProfessor() {
        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Completa todos los campos obligatorios.",
                    AlertType.WARNING);
        } else if (!isValidEmail(emailTextField.getText())) {
            showAlert("Correo inválido", "Ingrese un correo electrónico válido.",
                    AlertType.WARNING);
        } else if (getPasswordValidationMessage(passwordField.getText()) != null) {
            showAlert("Contraseña no válida",
                    getPasswordValidationMessage(passwordField.getText()),
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
    public void showCoordinatorList() {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            List<Coordinator> coordinators = coordinatorDAO.findCoordinatorsWithoutProfessorRole();

            if (coordinators.isEmpty()) {
                showAlert("Información", "No hay coordinadores disponibles para asignar como profesor.",
                        AlertType.INFORMATION);
            } else {
                ChoiceDialog<Coordinator> dialog = new ChoiceDialog<>(coordinators.getFirst(), coordinators);
                dialog.setTitle("Seleccionar Coordinador");
                dialog.setHeaderText("Coordinadores activos sin rol de profesor");
                dialog.setContentText("Seleccione un coordinador:");
                Optional<Coordinator> result = dialog.showAndWait();
                result.ifPresent(this::addRolToCoordinator);
                showAlert("Éxito", "El rol de profesor ha sido asignado al coordinador seleccionado.",
                        AlertType.INFORMATION);
            }

        } catch (ServiceException serviceException) {
            showAlert("Error", "Error al recuperar coordinadores: " + serviceException.getMessage(),
                    AlertType.ERROR);
        }
    }

    private void addRolToCoordinator(Coordinator coordinator) {
        try {
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            coordinator.setRole("Profesor");
            userRoleDAO.saveUserRole(coordinator);
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
            ProfessorDAO professorDAO = new ProfessorDAO();
            Professor professor = new Professor();
            professor.setMatricula(idTextField.getText());
            professor.setFirstName(firstNameTextField.getText());
            professor.setLastName(lastNameTextField.getText());
            professor.setSecondLastName(secondLastNameTextField.getText());
            professor.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()));
            professor.setStatus("Activo");
            professor.setEmail(emailTextField.getText());
            professor.setAcademicArea(academicAreaTextField.getText());
            professor.setRole("Profesor");
            boolean saved = professorDAO.saveProfessor(professor);

            if (saved) {
                showAlert("Registro exitoso", "Profesor registrado exitosamente.",
                        AlertType.INFORMATION);
                clearFields();
            } else {
                showAlert("Error", "No se pudo registrar la información de usuario.",
                        AlertType.ERROR);
            }

        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible", "Error al procesar el registro. Intente más tarde.",
                    AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {
        boolean isEmpty = idTextField.getText().isEmpty()
                || emailTextField.getText().isEmpty()
                || firstNameTextField.getText().isEmpty()
                || lastNameTextField.getText().isEmpty()
                || secondLastNameTextField.getText().isEmpty()
                || academicAreaTextField.getText().isEmpty()
                || passwordField.getText().isEmpty()
                || confirmPasswordField.getText().isEmpty();
        return isEmpty;
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
        academicAreaTextField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        emailTextField.clear();
    }
}
