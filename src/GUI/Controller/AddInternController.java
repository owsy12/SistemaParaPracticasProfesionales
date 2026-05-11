package GUI.Controller;

import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;

public class AddInternController {

    @FXML
    private TextField idTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField secondLastNameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField firstNameTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField creditTextField;

    @FXML
    private void initialize(){
        setTypeAndLength(idTextField,"ID");
        setTypeAndLength(lastNameTextField,"Name");
        setTypeAndLength(secondLastNameTextField,"Name");
        setTypeAndLength(emailTextField,"Email");
        setTypeAndLength(firstNameTextField,"Name");
        setTypeAndLength(passwordField,"Text");
        setTypeAndLength(confirmPasswordField,"Text");
        setTypeAndLength(creditTextField,"Number");
    }

    @FXML
    public void cancelRegistration(ActionEvent actionEvent) {
        clear();
    }

    @FXML
    public void registerIntern(ActionEvent actionEvent) {
        if (isValid()){
            showAlert("advertencia", "Debe de llenar todos los datos",
                    Alert.AlertType.WARNING);
        }else {
            registrationProcess();
        }
    }

    private void registrationProcess(){
        try{
            InternDAO internDAO = new InternDAO();
            Intern intern = new Intern();
            intern.setMatricula(idTextField.getText());
            intern.setFirstName(firstNameTextField.getText());
            intern.setLastName(lastNameTextField.getText());
            intern.setSecondLastName(secondLastNameTextField.getText());
            intern.setEmail(emailTextField.getText());
            intern.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()));
            intern.setCredits(Integer.parseInt(creditTextField.getText()));
            intern.setStatus("Activo");
            intern.setRole("Practicante");

            if (internDAO.saveIntern(intern)){
                showAlert("Exito","Practicante guradado con exito",
                        Alert.AlertType.INFORMATION);
                clear();
            }else {
                showAlert("Advertenica", "No se lgoro guardar practiante",
                 Alert.AlertType.ERROR);
            }
        } catch (ValidationException e) {
            showAlert("VAlidation Error", "Valide infroamcin ingresada",
                    Alert.AlertType.ERROR);
        } catch (ServiceException e) {
            showAlert("Error", "Servicio no disponible",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean isValid(){
        boolean valid = false;

        if (idTextField.getText().isEmpty() ||
            creditTextField.getText().isEmpty() ||
            lastNameTextField.getText().isEmpty() ||
            secondLastNameTextField.getText().isEmpty() ||
            emailTextField.getText().isEmpty() ||
            passwordField.getText().isEmpty() ||
            confirmPasswordField.getText().isEmpty()){
            valid = true;
        }

        return valid;
    }

    private void clear(){
        idTextField.clear();
        lastNameTextField.clear();
        secondLastNameTextField.clear();
        emailTextField.clear();
        firstNameTextField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        creditTextField.clear();
    }
}
