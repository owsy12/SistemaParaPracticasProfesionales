package GUI.Controller;

import Logic.DAO.EducationalExperienceDAO;
import Logic.DAO.ProfessorDAO;
import Logic.DTOs.EducationalExperience;
import Logic.DTOs.Professor;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.util.List;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class AddEducationalExperienceController {

    @FXML
    public AnchorPane anchorPane;

    @FXML
    private TextField nrcTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private ComboBox<Professor> professorComboBox;

    @FXML
    private void initialize() {
        setTypeAndLength(nrcTextField, "Number");
        setTypeAndLength(nameTextField, "Name");
        loadProfessors();
        configureProfessorComboBox();
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
        openWelcomePage(anchorPane);
    }

    private void configureProfessorComboBox() {
        professorComboBox.setCellFactory(new Callback<ListView<Professor>, ListCell<Professor>>() {
            @Override
            public ListCell<Professor> call(ListView<Professor> listView) {
                return new ListCell<Professor>() {
                    @Override
                    protected void updateItem(Professor item, boolean empty) {
                        super.updateItem(item, empty);
                        String displayText = null;
                        if (!empty && item != null) {
                            displayText = item.getFirstName() + " " + item.getLastName();
                        }
                        setText(displayText);
                    }
                };
            }
        });

        professorComboBox.setButtonCell(new ListCell<Professor>() {
            @Override
            protected void updateItem(Professor item, boolean empty) {
                super.updateItem(item, empty);
                String displayText = null;
                if (!empty && item != null) {
                    displayText = item.getFirstName() + " " + item.getLastName();
                }
                setText(displayText);
            }
        });
    }

    private void registrationProcess() {
        try {
            EducationalExperience educationalExperience = buildEducationalExperience();
            EducationalExperienceDAO educationalExperienceDAO = new EducationalExperienceDAO();

            if (educationalExperienceDAO.save(educationalExperience)) {
                showAlert("Éxito", "La experiencia educativa ha sido registrada exitosamente.",
                        Alert.AlertType.INFORMATION);
                clear();
            } else {
                showAlert("Error", "No se pudo registrar la experiencia educativa. Intente más tarde.",
                        Alert.AlertType.ERROR);
            }
        } catch (DuplicateEntryException duplicateEntryException) {
            showAlert("NRC duplicado", "Ya existe una experiencia educativa con ese NRC.",
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
        return educationalExperience;
    }

    private void loadProfessors() {
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            List<Professor> professorList = professorDAO.findActiveProfessors();
            professorComboBox.getItems().setAll(professorList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se pudieron cargar los profesores. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", "Error al cargar los profesores.",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean isAnyFieldEmpty() {
        boolean isNrcEmpty = nrcTextField.getText().isBlank();
        boolean isNameEmpty = nameTextField.getText().isBlank();
        boolean isProfessorMissing = professorComboBox.getValue() == null;

        boolean hasEmptyFields = isNrcEmpty || isNameEmpty || isProfessorMissing;

        return hasEmptyFields;
    }

    private void clear() {
        nrcTextField.clear();
        nameTextField.clear();
        professorComboBox.getSelectionModel().clearSelection();
    }

}
