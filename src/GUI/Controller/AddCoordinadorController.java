package GUI.Controller;

import Logic.DAO.CoordinatorDAO;
import Logic.DAO.ProfessorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Professor;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;

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
        showAlert("Operación no permitida",
                "Los coordinadores solo pueden asignarse desde un profesor existente. "
                        + "Use el botón \"Profesor Existente\".",
                AlertType.WARNING);
    }

    @FXML
    public void showProfessorList() {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            boolean hasActiveCoordinator = !coordinatorDAO.findActiveCoordinators().isEmpty();

            if (hasActiveCoordinator) {
                showAlert("Coordinador existente",
                        "Ya existe un coordinador activo en el sistema. "
                                + "Solo puede haber un coordinador activo a la vez.",
                        AlertType.WARNING);
                return;
            }

            ProfessorDAO professorDAO = new ProfessorDAO();
            List<Professor> professors = professorDAO.findProfessorsWithoutCoordinatorRole();

            boolean hasProfessorsAvailable = !professors.isEmpty();
            if (!hasProfessorsAvailable) {
                showAlert("Sin profesores disponibles",
                        "No hay profesores disponibles para asignar como coordinador.",
                        AlertType.INFORMATION);
            } else {
                ChoiceDialog<Professor> dialog = new ChoiceDialog<>(professors.get(0), professors);
                dialog.setTitle("Seleccionar Profesor");
                dialog.setHeaderText("Profesores activos sin rol de coordinador");
                dialog.setContentText("Seleccione un profesor:");
                Optional<Professor> selectionResult = dialog.showAndWait();

                boolean isProfessorSelected = selectionResult.isPresent();
                if (isProfessorSelected) {
                    addCoordinatorRoleToProfessor(selectionResult.get());
                    showAlert("Rol asignado",
                            "El profesor ha sido asignado como coordinador exitosamente.",
                            AlertType.INFORMATION);
                }
            }

        } catch (ServiceException serviceException) {
            String serviceErrorMessage = "Error al recuperar datos: " + serviceException.getMessage();
            showAlert("Error", serviceErrorMessage, AlertType.ERROR);
        } catch (ValidationException validationException) {
            String validationErrorMessage = "Error al validar datos: " + validationException.getMessage();
            showAlert("Error de validación", validationErrorMessage, AlertType.ERROR);
        }
    }

    @FXML
    public void cancelRegistration() {
        showAlert("Registro cancelado", "La operación ha sido cancelada.",
                AlertType.INFORMATION);
    }

    private void addCoordinatorRoleToProfessor(User professor) {
        try {
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            professor.setRole("Coordinador");
            userRoleDAO.saveUserRole(professor);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible",
                    "No se pudo asignar el rol. Intente más tarde.",
                    AlertType.ERROR);
        }
    }

}
