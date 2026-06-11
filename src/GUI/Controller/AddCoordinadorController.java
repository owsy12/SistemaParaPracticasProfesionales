package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.CoordinatorDAO;
import Logic.DAO.ProfessorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Professor;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.List;

import static GUI.Utils.Alert.showAlert;

public class AddCoordinadorController {

    private static final Logger LOGGER = Logger.getLogger(AddCoordinadorController.class.getName());

    @FXML
    private Label statusLabel;

    @FXML
    private ComboBox<Professor> professorComboBox;

    @FXML
    private Button assignButton;

    @FXML
    private void initialize() {
        loadProfessors();
    }

    @FXML
    public void assignCoordinatorRole() {
        Professor selectedProfessor = professorComboBox.getValue();
        boolean isSelectionMissing = selectedProfessor == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Selecciona el profesor que tomará el rol de coordinador.",
                    AlertType.WARNING);
        } else {
            addCoordinatorRoleToProfessor(selectedProfessor);
        }
    }

    @FXML
    public void cancelRegistration() {
        professorComboBox.getSelectionModel().clearSelection();
        showAlert("Registro cancelado", "La operación ha sido cancelada.",
                AlertType.INFORMATION);
    }

    private void loadProfessors() {
        try {
            CoordinatorDAO coordinatorDAO = new CoordinatorDAO();
            boolean hasActiveCoordinator = !coordinatorDAO.findActiveCoordinators().isEmpty();

            if (hasActiveCoordinator) {
                statusLabel.setText("Ya existe un coordinador activo en el sistema. "
                        + "Solo puede haber un coordinador activo a la vez.");
                professorComboBox.setDisable(true);
                assignButton.setDisable(true);
            } else {
                ProfessorDAO professorDAO = new ProfessorDAO();
                List<Professor> professors = professorDAO.findProfessorsWithoutCoordinatorRole();
                boolean hasProfessorsAvailable = !professors.isEmpty();
                if (hasProfessorsAvailable) {
                    professorComboBox.getItems().setAll(professors);
                } else {
                    statusLabel.setText("No hay profesores disponibles para asignar como coordinador.");
                    professorComboBox.setDisable(true);
                    assignButton.setDisable(true);
                }
            }
        } catch (ServiceException serviceException) {
            showAlert("Error", "Error al recuperar datos: " + serviceException.getMessage(),
                    AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    AlertType.ERROR);
        }
    }

    private void addCoordinatorRoleToProfessor(User professor) {
        try {
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            professor.setRole("Coordinador");
            userRoleDAO.saveUserRole(professor);
            LOGGER.log(Level.INFO,
                    "Usuario {0} asignó el rol de coordinador al profesor {1}",
                    new Object[]{SessionManager.getInstance().getUser().getId(), professor.getId()});
            showAlert("Rol asignado",
                    "El profesor ha sido asignado como coordinador exitosamente.",
                    AlertType.INFORMATION);
            loadProfessors();
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
