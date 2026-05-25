package GUI.Controller;

import Logic.DAO.LinkedOrganizationDAO;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.Alert.showFormAlert;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class AddLinkedOrganizationController {

    @FXML public TextField organizationNameTextField;
    @FXML public TextField organizationEmailTextField;
    @FXML public TextField organizationAddressTextField;
    @FXML public TextField sectorOrganizacionTextField;
    @FXML private AnchorPane anchorPane;

    @FXML
    public void initialize() {
        setTypeAndLength(organizationNameTextField, "Name");
        setTypeAndLength(organizationAddressTextField, "Text");
        setTypeAndLength(sectorOrganizacionTextField, "Name");
        setTypeAndLength(organizationEmailTextField, "Email");
    }

    public void addOrganization(ActionEvent actionEvent) {
        if (hasEmptyFields()) {
            showFormAlert(AlertType.WARNING,
                    "Campos incompletos",
                    "Campos obligatorios vac\u00EDos",
                    "Por favor, completa todos los campos obligatorios antes de continuar.");
        } else if (!isValidEmail(organizationEmailTextField.getText())) {
            showFormAlert(AlertType.WARNING,
                    "Correo inv\u00E1lido",
                    "Formato de correo incorrecto",
                    "Por favor, ingresa un correo electr\u00F3nico v\u00E1lido.");
        } else {
            processRegistration();
        }
    }

    public void cancel(ActionEvent actionEvent) {
        showFormAlert(AlertType.INFORMATION,
                "Registro cancelado",
                "Operaci\u00F3n cancelada",
                "El registro de la organizaci\u00F3n ha sido cancelado. Ser\u00E1s redirigido al inicio.");
        clearFields();
        openWelcomePage(anchorPane);
    }

    private void processRegistration() {
        LinkedOrganization organization = buildOrganization();
        try {
            LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();
            if (linkedOrganizationDAO.saveLinkedOrganization(organization)) {
                showFormAlert(AlertType.INFORMATION,
                        "Registro exitoso",
                        "Organizaci\u00F3n registrada",
                        "La organizaci\u00F3n ha sido registrada exitosamente en el sistema.");
                clearFields();
            } else {
                showFormAlert(AlertType.ERROR,
                        "Registro fallido",
                        "No se pudo registrar",
                        "No se pudo registrar la organizaci\u00F3n. Intenta nuevamente.");
            }
        } catch (ServiceException serviceException) {
            showFormAlert(AlertType.ERROR,
                    "Servicio no disponible",
                    "Error de servicio",
                    "El servicio no se encuentra disponible en este momento. Intenta m\u00E1s tarde.");
        } catch (ValidationException validationException) {
            showFormAlert(AlertType.ERROR,
                    "Error de validaci\u00F3n",
                    "Datos inv\u00E1lidos",
                    validationException.getMessage());
        }
    }

    private LinkedOrganization buildOrganization() {
        LinkedOrganization organization = new LinkedOrganization();
        organization.setName(organizationNameTextField.getText());
        organization.setEmail(organizationEmailTextField.getText());
        organization.setAddress(organizationAddressTextField.getText());
        organization.setSector(sectorOrganizacionTextField.getText());
        return organization;
    }

    private boolean hasEmptyFields() {
        boolean isNameEmpty = organizationNameTextField.getText().isEmpty();
        boolean isEmailEmpty = organizationEmailTextField.getText().isEmpty();
        boolean isAddressEmpty = organizationAddressTextField.getText().isEmpty();
        boolean isSectorEmpty = sectorOrganizacionTextField.getText().isEmpty();
        return isNameEmpty || isEmailEmpty || isAddressEmpty || isSectorEmpty;
    }

    private void clearFields() {
        organizationNameTextField.clear();
        organizationEmailTextField.clear();
        organizationAddressTextField.clear();
        sectorOrganizacionTextField.clear();
    }

}