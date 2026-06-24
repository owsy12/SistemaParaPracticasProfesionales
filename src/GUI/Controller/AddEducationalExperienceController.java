package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.EducationalExperienceDAO;
import Logic.DAO.ProfessorDAO;
import Logic.DTOs.EducationalExperience;
import Logic.DTOs.Professor;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import GUI.Utils.RestrictedTextField;
import javafx.scene.layout.AnchorPane;

import java.util.List;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ViewsUtils.openWelcomePage;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AddEducationalExperienceController {

    private static final Logger LOGGER = Logger.getLogger(AddEducationalExperienceController.class.getName());
    @FXML
    public AnchorPane anchorPane;

    @FXML
    private RestrictedTextField nrcTextField;

    @FXML
    private RestrictedTextField nameTextField;

    @FXML
    private ComboBox<Professor> professorComboBox;
    @FXML
    private ComboBox<String> periodcomboBox;

    @FXML
    private void initialize() {
        setTypeAndLength(nrcTextField, "Number");
        setTypeAndLength(nameTextField, "Name");
        loadProfessors();
        loadPeriodCombobox();
    }

    @FXML
    public void save(ActionEvent actionEvent) {
        if (isAnyFieldEmpty()) {
            showAlert("Campos incompletos", "Por favor complete todos los campos.",
                    Alert.AlertType.WARNING);
        } else {
            registrationProcess();
        }
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmar cancelación",
                "¿Desea salir? Los datos ingresados no se guardarán.",
                Alert.AlertType.CONFIRMATION);
        boolean isConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isConfirmed) {
            clear();
            openWelcomePage(anchorPane);
        }
    }

    private void registrationProcess() {
        try {
            EducationalExperience educationalExperience = buildEducationalExperience();
            EducationalExperienceDAO educationalExperienceDAO = new EducationalExperienceDAO();

            if (educationalExperienceDAO.save(educationalExperience)) {
                LOGGER.log(Level.INFO,
                        "Usuario {0} registró la experiencia educativa con NRC {1}",
                        new Object[]{SessionManager.getInstance().getUser().getId(), educationalExperience.getNrc()});
                showAlert("Éxito", "La experiencia educativa ha sido registrada exitosamente.",
                        Alert.AlertType.INFORMATION);
                clear();
            } else {
                showAlert("Error", "No se pudo registrar la experiencia educativa. Intente más tarde.",
                        Alert.AlertType.ERROR);
            }
        } catch (DuplicateEntryException duplicateEntryException) {
            showAlert("Experiencia educativa duplicada",
                    "Ya existe una experiencia educativa con ese NRC en ese periodo.",
                    Alert.AlertType.WARNING);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible", "No se puede conectar al servicio. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private EducationalExperience buildEducationalExperience() {
        EducationalExperience educationalExperience = new EducationalExperience();
        educationalExperience.setNrc(nrcTextField.getText().trim());
        educationalExperience.setName(nameTextField.getText().trim());
        educationalExperience.setIdProfessor(professorComboBox.getValue().getId());
        educationalExperience.setPeriod(periodcomboBox.getValue());
        return educationalExperience;
    }

    private void loadProfessors() {
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            List<Professor> professorList = professorDAO.findActiveProfessors();
            boolean hasProfessors = !professorList.isEmpty();
            if (hasProfessors) {
                professorComboBox.getItems().setAll(professorList);
            } else {
                showAlert("Sin profesores disponibles",
                        "No existen profesores disponibles para asociar a la Experiencia Educativa.",
                        Alert.AlertType.WARNING);
                openWelcomePage(anchorPane);
            }
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se pudieron cargar los profesores. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", "Error al cargar los profesores.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadPeriodCombobox(){
        String year = String.valueOf(java.time.LocalDate.now().getYear());

        periodcomboBox.getItems().add("FEB-JUL-" + year);
        periodcomboBox.getItems().add("AUG-ENE-" + year);
    }

    private boolean isAnyFieldEmpty() {
        boolean isNrcEmpty = nrcTextField.getText().isBlank();
        boolean isNameEmpty = nameTextField.getText().isBlank();
        boolean isProfessorMissing = professorComboBox.getValue() == null;
        boolean isPeriodMissing = periodcomboBox.getValue() == null;

        boolean hasEmptyFields = isNrcEmpty || isNameEmpty || isProfessorMissing || isPeriodMissing;

        return hasEmptyFields;
    }

    private void clear() {
        nrcTextField.clear();
        nameTextField.clear();
        professorComboBox.getSelectionModel().clearSelection();
        periodcomboBox.getSelectionModel().clearSelection();
    }

}
