package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ProfessorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Professor;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import java.util.logging.Level;
import java.util.logging.Logger;
import GUI.Utils.RestrictedPasswordField;
import GUI.Utils.RestrictedTextField;
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

public class AddProfessorController {

    private static final Logger LOGGER = Logger.getLogger(AddProfessorController.class.getName());
    private static final String STATUS_ACTIVE = "Activo";


    @FXML
    private RestrictedTextField idTextField;

    @FXML
    private RestrictedTextField firstNameTextField;

    @FXML
    private RestrictedTextField lastNameTextField;

    @FXML
    private RestrictedTextField secondLastNameTextField;

    @FXML
    private RestrictedTextField academicAreaTextField;

    @FXML
    private RestrictedPasswordField passwordField;

    @FXML
    private RestrictedPasswordField confirmPasswordField;

    @FXML
    private RestrictedTextField emailTextField;

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
        String passwordValidationMessage = getPasswordValidationMessage(passwordField.getText());
        boolean isPasswordInvalid = passwordValidationMessage != null;

        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Completa todos los campos obligatorios.",
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
    public void showCoordinatorList() {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            List<Coordinator> coordinators = coordinatorDAO.findCoordinatorsWithoutProfessorRole();

            if (coordinators.isEmpty()) {
                showAlert("Información", "No hay coordinadores disponibles para asignar como profesor.",
                        AlertType.INFORMATION);
            } else {
                ChoiceDialog<Coordinator> dialog = new ChoiceDialog<>(coordinators.get(0), coordinators);
                dialog.setTitle("Seleccionar Coordinador");
                dialog.setHeaderText("Coordinadores activos sin rol de profesor");
                dialog.setContentText("Seleccione un coordinador:");
                Optional<Coordinator> result = dialog.showAndWait();

                if (result.isPresent()) {
                    addRolToCoordinator(result.get());
                    LOGGER.log(Level.INFO,
                            "User {0} assigned the professor role to coordinator {1}",
                            new Object[]{SessionManager.getInstance().getUser().getIdUser(), result.get().getIdUser()});
                    showAlert("Éxito", "El rol de profesor ha sido asignado al coordinador seleccionado.",
                            AlertType.INFORMATION);
                }
            }

        } catch (ServiceException serviceException) {
            String serviceErrorMessage = "Error al recuperar coordinadores: " + serviceException.getMessage();
            showAlert("Error", serviceErrorMessage, AlertType.ERROR);
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
            professor.setRegistrationNumber(idTextField.getText());
            professor.setFirstName(firstNameTextField.getText());
            professor.setLastName(lastNameTextField.getText());
            professor.setSecondLastName(secondLastNameTextField.getText());
            professor.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()));
            professor.setStatus(STATUS_ACTIVE);
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

        } catch (DuplicateEntryException duplicateEntryException) {
            String duplicateMessage = "Ya existe un profesor registrado con esa matrícula.";
            if (duplicateEntryException.isEmailDuplicated()) {
                duplicateMessage = "Ya existe un usuario registrado con ese correo electrónico.";
            }
            showAlert("Registro duplicado", duplicateMessage, AlertType.WARNING);
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible", "Error al conectar, Servidor fuera de linea",
                    AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {
        boolean isIdEmpty = idTextField.getText().isEmpty();
        boolean isEmailEmpty = emailTextField.getText().isEmpty();
        boolean isFirstNameEmpty = firstNameTextField.getText().isEmpty();
        boolean isLastNameEmpty = lastNameTextField.getText().isEmpty();
        boolean isSecondLastNameEmpty = secondLastNameTextField.getText().isEmpty();
        boolean isAcademicAreaEmpty = academicAreaTextField.getText().isEmpty();
        boolean isPasswordEmpty = passwordField.getText().isEmpty();
        boolean isConfirmPasswordEmpty = confirmPasswordField.getText().isEmpty();

        boolean hasEmpty = false;
        if (isIdEmpty) {
            hasEmpty = true;
        } else if (isEmailEmpty) {
            hasEmpty = true;
        } else if (isFirstNameEmpty) {
            hasEmpty = true;
        } else if (isLastNameEmpty) {
            hasEmpty = true;
        } else if (isSecondLastNameEmpty) {
            hasEmpty = true;
        } else if (isAcademicAreaEmpty) {
            hasEmpty = true;
        } else if (isPasswordEmpty) {
            hasEmpty = true;
        } else if (isConfirmPasswordEmpty) {
            hasEmpty = true;
        }

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
        academicAreaTextField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        emailTextField.clear();
    }

}
