package GUI.Controller;

import Logic.DAO.CoordinatorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Coordinator;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ValidationUtils.isValidEmail;
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
    private void initialize(){
        setTypeAndLength(firstNameTextField,"Name");
        setTypeAndLength(lastNameTextField,"Name");
        setTypeAndLength(secondLastNameTextField,"Name");
        setTypeAndLength(idTextField,"ID");
        setTypeAndLength(passwordField,"Email");
    }

    @FXML
    public void registerCoordinator() {
        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Por favor, completa todos los campos obligatorios.",
                    AlertType.WARNING);
        } else if (!isPasswordMatching() && isValidEmail(emailTextField.getText())) {
            showAlert("Error de contraseña", "Las contraseñas no coinciden, verifica la información.",
                    AlertType.ERROR);
        } else {
            processRegistration();
        }
    }

    @FXML
    public void showProfessorList() {
        try  {
            ProfessorDAO professorDAO = new ProfessorDAO();
            List<Professor> professors = professorDAO.findProfessorsWithoutCoordinatorRole();

            if (professors.isEmpty()) {
                showAlert("Información", "No hay profesores disponibles para asignar como coordinador.",
                        AlertType.INFORMATION);
                return;
            }

            ChoiceDialog<Professor> dialog = new ChoiceDialog<>(professors.get(0), professors);
            dialog.setTitle("Seleccionar Profesor");
            dialog.setHeaderText("Profesores activos sin rol de coordinador");
            dialog.setContentText("Seleccione un profesor:");
            Optional<Professor> result = dialog.showAndWait();
            result.ifPresent(this::addRolToProfessor);
            showAlert("Rol asignado", "El profesor ha sido asignado como coordinador exitosamente.",
                    AlertType.INFORMATION);
        } catch (ServiceException exception) {
            showAlert("Error", "Error al recuperar profesores: " + exception.getMessage(),
                    AlertType.ERROR);
        }catch (ValidationException exception) {
            showAlert("Error de validación", "Error al validar datos: " + exception.getMessage(),
                    AlertType.ERROR);
        }

    }

    private void addRolToProfessor(Professor professor) {
        try{
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            professor.setRole("Coordinador");
            userRoleDAO.saveUserRole(professor);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void cancelRegistration() {
        showAlert("Registro cancelado", "La operación ha sido cancelada.",
                AlertType.INFORMATION);
        clearFields();
    }

    private void processRegistration() {
        try  {
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
                showAlert("Registro Exitoso", "Coordinador registrado exitosamente.",
                        AlertType.INFORMATION);
                clearFields();
            } else {
                showAlert("Error", "No se pudo realizar el registro.", AlertType.ERROR);
            }

        } catch ( ServiceException exception) {
            showAlert("Error de conexión", "Error al conectar con la base de datos: " + exception.getMessage(),
                    AlertType.ERROR);
        }catch ( ValidationException exception) {
            showAlert("Error de validación", "Error al validar datos: " + exception.getMessage(),
                    AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {
        boolean isEmptu = false;

        if (idTextField.getText().isEmpty() ||
            firstNameTextField.getText().isEmpty() ||
            lastNameTextField.getText().isEmpty() ||
            secondLastNameTextField.getText().isEmpty() ||
            emailTextField.getText().isEmpty()  ||
            passwordField.getText().isEmpty() ||
            confirmPasswordField.getText().isEmpty()) {
                isEmptu = true;
        }

        return isEmptu;
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