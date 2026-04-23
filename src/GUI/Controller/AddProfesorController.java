package GUI.Controller;

import Logic.DAO.ProfessorDAO;
import Logic.DAO.UserDAO;
import Logic.DTOs.Professor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import static GUI.Utils.Alert.showAlert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import Logic.DAO.CoordinatorDAO;
import Logic.DTOs.Coordinator;
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
    public void registerProfessor() {
        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Completa todos los campos obligatorios.", AlertType.WARNING);
        } else if (!isPasswordMatching()) {
            showAlert("Error de contraseña", "Las contraseñas no coinciden, verifica la información.", AlertType.ERROR);
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
                showAlert("Información", "No hay coordinadores disponibles para asignar como profesor.", AlertType.INFORMATION);
                return;
            }
            ChoiceDialog<Coordinator> dialog = new ChoiceDialog<>(coordinators.get(0), coordinators);
            dialog.setTitle("Seleccionar Coordinador");
            dialog.setHeaderText("Coordinadores activos sin rol de profesor");
            dialog.setContentText("Seleccione un coordinador:");
            Optional<Coordinator> result = dialog.showAndWait();
            result.ifPresent(this::fillFieldsWithCoordinator);
        } catch (ServiceException exception) {
            showAlert("Error", "Error al recuperar coordinadores: " + exception.getMessage(), AlertType.ERROR);
        }
    }

    private void fillFieldsWithCoordinator(Coordinator coordinator) {
        idTextField.setText(coordinator.getMatricula());
        firstNameTextField.setText(coordinator.getFirstName());
        lastNameTextField.setText(coordinator.getLastName());
        secondLastNameTextField.setText(coordinator.getSecondLastName());
        passwordField.setText(coordinator.getPassword());
        confirmPasswordField.setText(coordinator.getPassword());
        idTextField.setEditable(false);
    }

    @FXML
    public void cancelRegistration() {
        showAlert("Registro cancelado", "La operación ha sido cancelada.", AlertType.INFORMATION);
        clearFields();
    }

    private void processRegistration() {

            try {
                UserDAO userDAO = new UserDAO();
                ProfessorDAO professorDAO = new ProfessorDAO();
                
                Professor professor = new Professor();
                professor.setMatricula(idTextField.getText());
                professor.setFirstName(firstNameTextField.getText());
                professor.setLastName(lastNameTextField.getText());
                professor.setSecondLastName(secondLastNameTextField.getText());
                professor.setPassword(passwordField.getText());
                professor.setStatus("Activo");
                professor.setAcademicArea(academicAreaTextField.getText());

                int userId = userDAO.saveUser(professor);
                if (userId > 0) {

                        showAlert("Registro Exitoso", "Profesor registrado exitosamente.", AlertType.INFORMATION);
                        clearFields();

                } else {

                    showAlert("Error", "No se pudo registrar la información de usuario.", AlertType.ERROR);
                }
            } catch (ServiceException exception) {

                showAlert("Error", "Error al procesar el registro: " + exception.getMessage(), AlertType.ERROR);
            } catch (ValidationException exception) {

                showAlert("Error de validación", "Error al validar datos: " + exception.getMessage(), AlertType.ERROR);
            }

    }

    private boolean hasEmptyFields() {
        return idTextField.getText().isEmpty() ||
               firstNameTextField.getText().isEmpty() ||
               lastNameTextField.getText().isEmpty() ||
               secondLastNameTextField.getText().isEmpty() ||
               academicAreaTextField.getText().isEmpty() ||
               passwordField.getText().isEmpty() ||
               confirmPasswordField.getText().isEmpty();
    }

    private boolean isPasswordMatching() {
        return passwordField.getText().equals(confirmPasswordField.getText());
    }

    private void clearFields() {
        idTextField.clear();
        firstNameTextField.clear();
        lastNameTextField.clear();
        secondLastNameTextField.clear();
        academicAreaTextField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}
