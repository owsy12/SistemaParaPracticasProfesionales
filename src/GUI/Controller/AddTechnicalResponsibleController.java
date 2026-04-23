package GUI.Controller;

import Logic.DAO.LinkedOrganizationDAO;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.LinkedOrganization;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.ValidationUtils.setTypeAndLenght;
import static GUI.Utils.Alert.showAlert;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;

public class AddTechnicalResponsibleController {
    @javafx.fxml.FXML
    public TextField nameField;
    @javafx.fxml.FXML
    private TextField emailField;
    @javafx.fxml.FXML
    private ComboBox<LinkedOrganization> organizationComboBox;
    @javafx.fxml.FXML
    private TextField lastNameField;
    @javafx.fxml.FXML
    private TextField lastNameMaterField;
    @javafx.fxml.FXML
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

        setTypeAndLenght(nameField, "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*", 30);
        setTypeAndLenght(lastNameField, "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*", 30);
        setTypeAndLenght(lastNameMaterField, "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*", 30);
        setTypeAndLenght(cargoField, "[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]*", 45);
        setTypeAndLenght(emailField, "[a-zA-Z0-9@._%+\\-]*", 50);
    }

    @javafx.fxml.FXML
    public void cancel(ActionEvent actionEvent) {
        showAlert("Registro cancelado", "La operación ha sido cancelada.", AlertType.INFORMATION);
        clearFields();

    }

    @javafx.fxml.FXML
    public void addTechnical(ActionEvent actionEvent) {
        if (hasEmptyFields() || !isValidEmail(emailField.getText())) {
            showAlert("Campos vacíos o email inválido", "Por favor, completa todos los campos obligatorios y verifica el email.", AlertType.WARNING);
        } else {
            processRegistration();
        }
    }

    private void loadLinkedOrganization (){
        try{
            LinkedOrganizationDAO linkedOrganization = new LinkedOrganizationDAO();
            organizationComboBox.getItems().addAll(linkedOrganization.findAllActive());
        } catch (ServiceException e) {
            showAlert("Suceso inesperado", "El servicio no se encuentra disponible por el momento" + e.getMessage(), AlertType.ERROR);
        }
    }

    private void processRegistration() {
        TechnicalSupervisor tecnico = new TechnicalSupervisor();
        tecnico.setName(nameField.getText());
        tecnico.seteMail(emailField.getText());
        tecnico.setLastName(lastNameField.getText());
        tecnico.setSecondLastName(lastNameMaterField.getText());
        tecnico.setPosition(cargoField.getText());
        LinkedOrganization linkedOrganization = organizationComboBox.getValue();
        tecnico.setIdOrganization(linkedOrganization.getIdLinkedOrganization());

        try{
            TechnicalResponsibleDAO technicalResponsibleDAO = new TechnicalResponsibleDAO();
            if (technicalResponsibleDAO.saveTechnicalResponsible(tecnico)){
                showAlert("Registro exitoso", "El responsable técnico ha sido registrado exitosamente.", AlertType.INFORMATION);
                clearFields();
            } else {
                showAlert("Registro fallido", "No se pudo registrar el responsable técnico, intenta nuevamente.", AlertType.ERROR);
            }
        } catch (ValidationException e) {
            showAlert("Error de validación", e.getMessage(), AlertType.ERROR);
        } catch (ServiceException e) {
            showAlert("Suceso inesperado", "El servicio no se encuentra disponible por el momento" + e.getMessage(), AlertType.ERROR);
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
        return  nameField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                lastNameField.getText().isEmpty() ||
                lastNameMaterField.getText().isEmpty() ||
                cargoField.getText().isEmpty() ||
                organizationComboBox.getValue() == null;
    }
}
