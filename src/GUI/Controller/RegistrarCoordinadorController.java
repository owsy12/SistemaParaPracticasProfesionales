package GUI.Controller;

import DataAccess.DataBaseConnection;
import Logic.DAO.CoordinatorDAO;
import Logic.DTOs.Coordinator;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.sql.Connection;
import java.sql.SQLException;

import javafx.scene.control.ChoiceDialog;
import Logic.DAO.ProfessorDAO;
import Logic.DTOs.Professor;
import java.util.List;
import java.util.Optional;

public class RegistrarCoordinadorController {

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
    private Button existingProfessorButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button cancelButton;

    @FXML
    public void registerCoordinator() {
        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Por favor, completa todos los campos obligatorios.", AlertType.WARNING);
        } else if (!isPasswordMatching()) {
            showAlert("Error de contraseña", "Las contraseñas no coinciden, verifica la información.", AlertType.ERROR);
        } else {
            processRegistration();
        }
    }

    @FXML
    public void showProfessorList() {
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            ProfessorDAO professorDAO = new ProfessorDAO(connection);
            List<Professor> professors = professorDAO.findProfessorsWithoutCoordinatorRole();
            if (professors.isEmpty()) {
                showAlert("Información", "No hay profesores disponibles para asignar como coordinador.", AlertType.INFORMATION);
                return;
            }
            ChoiceDialog<Professor> dialog = new ChoiceDialog<>(professors.get(0), professors);
            dialog.setTitle("Seleccionar Profesor");
            dialog.setHeaderText("Profesores activos sin rol de coordinador");
            dialog.setContentText("Seleccione un profesor:");
            Optional<Professor> result = dialog.showAndWait();
            result.ifPresent(this::fillFieldsWithProfessor);
        } catch (SQLException | DatabaseException | ValidationException exception) {
            showAlert("Error", "Error al recuperar profesores: " + exception.getMessage(), AlertType.ERROR);
        }
    }

    private void fillFieldsWithProfessor(Professor professor) {
        idTextField.setText(professor.getMatricula());
        firstNameTextField.setText(professor.getFirstName());
        lastNameTextField.setText(professor.getLastName());
        secondLastNameTextField.setText(professor.getSecondLastName());
        passwordField.setText(professor.getPassword());
        confirmPasswordField.setText(professor.getPassword());
        idTextField.setEditable(false);
    }

    @FXML
    public void cancelRegistration() {
        showAlert("Registro cancelado", "La operación ha sido cancelada.", AlertType.INFORMATION);
        clearFields();
    }

    private void processRegistration() {
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO(connection);
            Coordinator coordinator = new Coordinator();
            coordinator.setMatricula(idTextField.getText());
            coordinator.setFirstName(firstNameTextField.getText());
            coordinator.setLastName(lastNameTextField.getText());
            coordinator.setSecondLastName(secondLastNameTextField.getText());
            coordinator.setPassword(passwordField.getText());
            coordinator.setStatus("Activo");

            if (coordinatorDAO.save(coordinator)) {
                showAlert("Registro Exitoso", "Coordinador registrado exitosamente.", AlertType.INFORMATION);
                clearFields();
            } else {
                showAlert("Error", "No se pudo realizar el registro.", AlertType.ERROR);
            }
        } catch (SQLException | DatabaseException | ValidationException exception) {
            showAlert("Error de conexión", "Error al conectar con la base de datos: " + exception.getMessage(), AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {
        return idTextField.getText().isEmpty() ||
               firstNameTextField.getText().isEmpty() ||
               lastNameTextField.getText().isEmpty() ||
               secondLastNameTextField.getText().isEmpty() ||
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
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
