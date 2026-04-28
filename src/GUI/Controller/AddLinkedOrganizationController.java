package GUI.Controller;

import Logic.DAO.LinkedOrganizationDAO;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.DTOs.LinkedOrganization;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.Alert.showAlert;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class AddLinkedOrganizationController {
    @FXML
    public TextField organizationNameTextField;
    @FXML
    public TextField organizationEmailTextField;
    @FXML
    public TextField organizationAddressTextField;
    @FXML
    public TextField sectorOrganizacionTextField;

    @FXML
    public void initialize() {
        setTypeAndLength(organizationNameTextField, "Name");
        setTypeAndLength(organizationAddressTextField, "Text");
        setTypeAndLength(sectorOrganizacionTextField, "Name");
        setTypeAndLength(organizationEmailTextField, "Email");
    }

    public void addOrganization(ActionEvent actionEvent) {
        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Por favor, completa todos los campos obligatorios.",
                    Alert.AlertType.WARNING);
        }else if (!isValidEmail(organizationEmailTextField.getText())) {
            showAlert("Correo electrónico inválido", "Por favor, ingresa un correo electrónico válido.",
                    Alert.AlertType.WARNING);
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
        organization.setName(organizationNameTextField.getText());
        organization.setEmail(organizationEmailTextField.getText());
        organization.setAdress(organizationAddressTextField.getText());
        organization.setSector(sectorOrganizacionTextField.getText());

        try {
            LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();

            if (linkedOrganizationDAO.saveLinkedOrganization(organization)){
                showAlert("Registro exitoso", "La organización ha sido registrada exitosamente.",
                        AlertType.INFORMATION);
                clearFields();
            }else {
                showAlert("Registro fallido", "No se pudo registrar la organización, intenta nuevamente.",
                        AlertType.ERROR);
            }

        }catch (ServiceException serviceException) {
            showAlert("Suceso inesperado", "El servicio no se encuentra disponible por el momento" + serviceException.getMessage(),
                    AlertType.ERROR);
        } catch (ValidationException e) {
            showAlert("Error de validación", e.getMessage(), AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {

        boolean isEmpty = false;

        if (organizationNameTextField.getText().isEmpty() ||
            organizationEmailTextField.getText().isEmpty() ||
            organizationAddressTextField.getText().isEmpty() ||
            sectorOrganizacionTextField.getText().isEmpty()) {
                isEmpty = true;
        }

        return  isEmpty;
    }

    private void clearFields() {
        organizationNameTextField.clear();
        organizationEmailTextField.clear();
        organizationAddressTextField.clear();
        sectorOrganizacionTextField.clear();
    }
}
