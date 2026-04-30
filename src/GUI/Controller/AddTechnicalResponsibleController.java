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
import javafx.scene.control.ListCell;

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
        organizationComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(LinkedOrganization item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        organizationComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LinkedOrganization item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

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
        if (hasEmptyFields() || !isValidEmail(emailField.getText())) {
            showAlert("Campos vacíos o email inválido", "Por favor, completa todos los campos obligatorios y verifica el email.",
                    AlertType.WARNING);
        } else {
            processRegistration();
        }
    }

    private void loadLinkedOrganization (){
        try{
            LinkedOrganizationDAO linkedOrganization = new LinkedOrganizationDAO();
            organizationComboBox.getItems().addAll(linkedOrganization.findAllActive());
        } catch (ServiceException e) {
            showAlert("Suceso inesperado", "El servicio no se encuentra disponible por el momento" + e.getMessage(),
                    AlertType.ERROR);
        }
    }

    private void processRegistration() {

        try{

            TechnicalSupervisor technicalSupervisor = new TechnicalSupervisor();
            technicalSupervisor.setName(nameField.getText());
            technicalSupervisor.seteMail(emailField.getText());
            technicalSupervisor.setLastName(lastNameField.getText());
            technicalSupervisor.setSecondLastName(lastNameMaterField.getText());
            technicalSupervisor.setPosition(cargoField.getText());
            LinkedOrganization linkedOrganization = organizationComboBox.getValue();
            technicalSupervisor.setIdOrganization(linkedOrganization.getIdLinkedOrganization());
            TechnicalResponsibleDAO technicalResponsibleDAO = new TechnicalResponsibleDAO();

            if (technicalResponsibleDAO.saveTechnicalResponsible(technicalSupervisor)){
                showAlert("Registro exitoso", "El responsable técnico ha sido registrado exitosamente.",
                        AlertType.INFORMATION);
                clearFields();
            } else {
                showAlert("Registro fallido", "No se pudo registrar el responsable técnico, intenta nuevamente.",
                        AlertType.ERROR);
            }
        } catch (ValidationException e) {
            showAlert("Error de validación", e.getMessage(),
                    AlertType.ERROR);
        } catch (ServiceException e) {
            showAlert("Suceso inesperado", "El servicio no se encuentra disponible por el momento" + e.getMessage(),
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
        boolean isEmpty = false;

        if (nameField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                lastNameField.getText().isEmpty() ||
                lastNameMaterField.getText().isEmpty() ||
                cargoField.getText().isEmpty() ||
                organizationComboBox.getValue() == null
        ){
            isEmpty = true;
        }
        return  isEmpty;
    }
}
