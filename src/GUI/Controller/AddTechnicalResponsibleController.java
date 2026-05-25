package GUI.Controller;

import Logic.DAO.LinkedOrganizationDAO;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.LinkedOrganization;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.Alert.showAlert;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class AddTechnicalResponsibleController {

    @FXML
    public TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<LinkedOrganization> organizationComboBox;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField lastNameMaterField;

    @FXML
    private TextField cargoField;

    @FXML
    private void initialize() {
        loadLinkedOrganization();
        setTypeAndLength(nameField, "Name");
        setTypeAndLength(lastNameField, "Name");
        setTypeAndLength(lastNameMaterField, "Name");
        setTypeAndLength(cargoField, "Text");
        setTypeAndLength(emailField, "Email");
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        showAlert("Registro cancelado", "La operación ha sido cancelada.",
                AlertType.INFORMATION);
        clearFields();
    }

    @FXML
    public void addTechnical(ActionEvent actionEvent) {
        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Por favor, completa todos los campos obligatorios.",
                    AlertType.WARNING);
        } else if (!isValidEmail(emailField.getText())) {
            showAlert("Email inválido", "Por favor, verifica el email ingresado.",
                    AlertType.WARNING);
        } else {
            processRegistration();
        }
    }

    private void loadLinkedOrganization() {
        try {
            LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();
            organizationComboBox.getItems().addAll(linkedOrganizationDAO.findAllActive());
        } catch (ServiceException serviceException) {
            showAlert("Suceso inesperado",
                    "El servicio no se encuentra disponible por el momento" + serviceException.getMessage(),
                    AlertType.ERROR);
        }
    }

    private void processRegistration() {
        try {
            TechnicalSupervisor technicalSupervisor = new TechnicalSupervisor();
            technicalSupervisor.setName(nameField.getText());
            technicalSupervisor.seteMail(emailField.getText());
            technicalSupervisor.setLastName(lastNameField.getText());
            technicalSupervisor.setSecondLastName(lastNameMaterField.getText());
            technicalSupervisor.setPosition(cargoField.getText());
            LinkedOrganization linkedOrganization = organizationComboBox.getValue();
            technicalSupervisor.setIdOrganization(linkedOrganization.getIdLinkedOrganization());
            TechnicalResponsibleDAO technicalResponsibleDAO = new TechnicalResponsibleDAO();

            if (technicalResponsibleDAO.saveTechnicalResponsible(technicalSupervisor)) {
                showAlert("Registro exitoso", "El responsable técnico ha sido registrado exitosamente.",
                        AlertType.INFORMATION);
                clearFields();
            } else {
                showAlert("Registro fallido", "No se pudo registrar el responsable técnico, intenta nuevamente.",
                        AlertType.ERROR);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Suceso inesperado",
                    "El servicio no se encuentra disponible por el momento" + serviceException.getMessage(),
                    AlertType.ERROR);
        }
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        lastNameField.clear();
        lastNameMaterField.clear();
        cargoField.clear();
        organizationComboBox.getSelectionModel().clearSelection();
    }

    private boolean hasEmptyFields() {
        boolean isNameEmpty = nameField.getText().isEmpty();
        boolean isEmailEmpty = emailField.getText().isEmpty();
        boolean isLastNameEmpty = lastNameField.getText().isEmpty();
        boolean isSecondLastNameEmpty = lastNameMaterField.getText().isEmpty();
        boolean isCargoEmpty = cargoField.getText().isEmpty();
        boolean isOrganizationMissing = organizationComboBox.getValue() == null;

        boolean hasEmpty = isNameEmpty || isEmailEmpty || isLastNameEmpty || isSecondLastNameEmpty || isCargoEmpty || isOrganizationMissing;

        return hasEmpty;
    }

}
