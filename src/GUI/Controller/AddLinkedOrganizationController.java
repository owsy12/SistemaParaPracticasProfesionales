package GUI.Controller;

import Logic.DAO.LinkedOrganizationDAO;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.DTOs.LinkedOrganization;
import static GUI.Utils.ValidationUtils.setTypeAndLenght;
import static GUI.Utils.ValidationUtils.isValidEmail;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class AddLinkedOrganizationController {
    @FXML
    public TextField organizationNameField;
    @FXML
    public TextField organizationEmailField;
    @FXML
    public TextField organizationAddressField;
    @FXML
    public TextField sectorOrganizacionField;

    @FXML
    public void initialize() {
        setTypeAndLenght(organizationNameField, "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*", 30);
        setTypeAndLenght(organizationAddressField, "[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ.,#\\- ]*", 100);
        setTypeAndLenght(sectorOrganizacionField, "[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*", 30);
    }

    public void addOrganization(ActionEvent actionEvent) {
        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Por favor, completa todos los campos obligatorios.", Alert.AlertType.WARNING);
        }else if (!isValidEmail(organizationEmailField.getText())) {
            showAlert("Correo electrónico inválido", "Por favor, ingresa un correo electrónico válido.", Alert.AlertType.WARNING);
        } else {
            processRegistration();
        }
    }

    public void cancel(ActionEvent actionEvent) {
        showAlert("Registro cancelado", "La operación ha sido cancelada.", AlertType.INFORMATION);
        clearFields();
    }

    private void processRegistration() {
        LinkedOrganization organization = new LinkedOrganization();
        organization.setName(organizationNameField.getText());
        organization.setEmail(organizationEmailField.getText());
        organization.setAdress(organizationAddressField.getText());
        organization.setSector(sectorOrganizacionField.getText());

        try {
            LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();

            if (linkedOrganizationDAO.saveLinkedOrganization(organization)){
                showAlert("Registro exitoso", "La organización ha sido registrada exitosamente.", AlertType.INFORMATION);
                clearFields();
            }else {
                showAlert("Registro fallido", "No se pudo registrar la organización, intenta nuevamente.", AlertType.ERROR);
            }

        }catch (ServiceException serviceException) {
            showAlert("Suceso inesperado", "El servicio no se encuentra disponible por el momento" + serviceException.getMessage(), AlertType.ERROR);
        } catch (ValidationException e) {
            showAlert("Error de validación", e.getMessage(), AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {
        return  organizationNameField.getText().isEmpty() ||
                organizationEmailField.getText().isEmpty() ||
                organizationAddressField.getText().isEmpty() ||
                sectorOrganizacionField.getText().isEmpty();
    }

    private void clearFields() {
        organizationNameField.clear();
        organizationEmailField.clear();
        organizationAddressField.clear();
        sectorOrganizacionField.clear();
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
