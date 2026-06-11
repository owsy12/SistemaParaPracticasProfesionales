package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.LinkedOrganizationDAO;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.DTOs.LinkedOrganization;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ValidationUtils.isValidEmail;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import GUI.Utils.RestrictedTextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddLinkedOrganizationController {

    private static final Logger LOGGER = Logger.getLogger(AddLinkedOrganizationController.class.getName());
    private static final String SECTOR_PUBLIC = "Público";
    private static final String SECTOR_PRIVATE = "Privado";

    @FXML
    private AnchorPane anchorPane;

    @FXML
    public RestrictedTextField organizationNameTextField;

    @FXML
    public RestrictedTextField organizationEmailTextField;

    @FXML
    public RestrictedTextField organizationAddressTextField;

    @FXML
    public ComboBox<String> sectorComboBox;

    @FXML
    public void initialize() {
        setTypeAndLength(organizationNameTextField, "Name");
        setTypeAndLength(organizationAddressTextField, "Address");
        setTypeAndLength(organizationEmailTextField, "Email");
        sectorComboBox.getItems().setAll(SECTOR_PUBLIC, SECTOR_PRIVATE);
    }

    public void addOrganization(ActionEvent actionEvent) {
        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Por favor, completa todos los campos obligatorios.",
                    Alert.AlertType.WARNING);
        } else if (!isValidEmail(organizationEmailTextField.getText())) {
            showAlert("Correo electrónico inválido", "Por favor, ingresa un correo electrónico válido.",
                    Alert.AlertType.WARNING);
        } else {
            processRegistration();
        }
    }

    public void cancel(ActionEvent actionEvent) {
        Optional<ButtonType> response = showAlertAndWait("Confirmar cancelación",
                "¿Desea salir? Los datos ingresados no se guardarán.",
                Alert.AlertType.CONFIRMATION);
        boolean isConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isConfirmed) {
            clearFields();
            openWelcomePage(anchorPane);
        }
    }

    private void processRegistration() {
        LinkedOrganization organization = new LinkedOrganization();
        organization.setName(organizationNameTextField.getText());
        organization.setEmail(organizationEmailTextField.getText());
        organization.setAddress(organizationAddressTextField.getText());
        organization.setSector(sectorComboBox.getValue());

        try {
            LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();

            if (linkedOrganizationDAO.saveLinkedOrganization(organization)) {
                LOGGER.log(Level.INFO,
                        "Usuario {0} registró la organización vinculada {1}",
                        new Object[]{SessionManager.getInstance().getUser().getId(),
                                organization.getName()});
                showAlert("Registro exitoso", "La organización ha sido registrada exitosamente.",
                        AlertType.INFORMATION);
                clearFields();
            } else {
                showAlert("Registro fallido", "No se pudo registrar la organización, intenta nuevamente.",
                        AlertType.ERROR);
            }

        } catch (ServiceException serviceException) {
            showAlert("Suceso inesperado",
                    "El servicio no se encuentra disponible por el momento" + serviceException.getMessage(),
                    AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(), AlertType.ERROR);
        }
    }

    private boolean hasEmptyFields() {
        boolean isNameEmpty = organizationNameTextField.getText().isEmpty();
        boolean isEmailEmpty = organizationEmailTextField.getText().isEmpty();
        boolean isAddressEmpty = organizationAddressTextField.getText().isEmpty();
        boolean isSectorEmpty = sectorComboBox.getValue() == null;

        boolean hasEmpty = isNameEmpty || isEmailEmpty || isAddressEmpty || isSectorEmpty;

        return hasEmpty;
    }

    private void clearFields() {
        organizationNameTextField.clear();
        organizationEmailTextField.clear();
        organizationAddressTextField.clear();
        sectorComboBox.getSelectionModel().clearSelection();
    }

}
