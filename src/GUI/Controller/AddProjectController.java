package GUI.Controller;

import Logic.DAO.LinkedOrganizationDAO;
import Logic.DAO.ProfessorDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.LinkedOrganization;
import Logic.DTOs.Professor;
import Logic.DTOs.Project;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;

public class AddProjectController {

    @FXML
    private TextField capacityTextField;
    @FXML
    private DatePicker endDate;
    @FXML
    private ComboBox<LinkedOrganization> organizationComboBox;
    @FXML
    private TextField nameTextField;
    @FXML
    private ComboBox <TechnicalSupervisor> technicalComboBox;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private DatePicker startDate;
    @FXML
    private ComboBox<Professor> professorComboBox;


    @FXML
    private void initialize(){
        setTypeAndLength(capacityTextField,"Number");
        setTypeAndLength(nameTextField,"Name");
        setTypeAndLength(descriptionTextField,"Text");

        loadOrganizations();
        loadProfessors();

        technicalComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(TechnicalSupervisor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        technicalComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TechnicalSupervisor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

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

        organizationComboBox.setOnAction(event ->{
                LinkedOrganization linkedOrganization = organizationComboBox.getValue();
                loadTechnicians(linkedOrganization.getIdLinkedOrganization());
            }
        );
    }

    @FXML
    public void addProject(ActionEvent actionEvent) {
        if (isValid() ){
            showAlert("Alerta", "Por favor verifique la infomcion ingresada ",
                    Alert.AlertType.WARNING);
        }else {
            registrationProcess();
        }
    }

    private void registrationProcess (){
        try{
            LocalDate startdate = startDate.getValue();
            LocalDate enddate = endDate.getValue();
            Project project = new Project();
            project.setName(nameTextField.getText());
            project.setDescription(descriptionTextField.getText());
            project.setIdOrganization(organizationComboBox.getValue().getIdLinkedOrganization());
            project.setIdTechnicalSupervisor(technicalComboBox.getValue().getIdTechnicalSupervisor());
            project.setIdProfessor(professorComboBox.getValue().getId());
            project.setStartDate(startdate);
            project.setEndDate(enddate);
            project.setMaximumPlaces(Integer.parseInt(capacityTextField.getText()));
            project.setAvaliablePlaces(Integer.parseInt(capacityTextField.getText()));
            ProjectDAO projectDAO = new ProjectDAO();

            if (projectDAO.saveProject(project)){
                showAlert("Exito", "El proyecto a sido guardado exitosamente",
                        Alert.AlertType.INFORMATION);
                clear();
            }else {
                showAlert("Error", "No se a podido guaradr el proyecto intente nemnante mas tarde",
                        Alert.AlertType.ERROR);
            }

        }catch (ValidationException e) {
            showAlert("Error de validacion", e.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException e) {
            showAlert("Error inesperado", "El servicio no se encuetra disponible",
                    Alert.AlertType.ERROR);
        }catch (NullPointerException e){
            showAlert("Precaucion", "Seleccione fechas validas",
                    Alert.AlertType.INFORMATION);
        }
    }

    private void loadOrganizations(){
        try {
            LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();
            List<LinkedOrganization> linkedOrganizations = linkedOrganizationDAO.findAllActive();
            organizationComboBox.getItems().setAll(linkedOrganizations);
        } catch (ServiceException e) {
            showAlert("Error", "Problema con nuestro servicio, intente mas tearde", Alert.AlertType.ERROR);
        }
    }

    private void loadTechnicians(int organizationId){
        try {
            TechnicalResponsibleDAO technicalResponsibleDAO = new TechnicalResponsibleDAO();
            List<TechnicalSupervisor> technicalSupervisorsList = technicalResponsibleDAO.findByOrganization(organizationId);
            technicalComboBox.getItems().setAll(technicalSupervisorsList);
        } catch (ValidationException | ServiceException e) {
            showAlert("Error","No se puede obtmer los responsables ene este moento ",
                    Alert.AlertType.WARNING);
        }
    }

    private void loadProfessors(){
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            List<Professor> professorList = professorDAO.findActiveProfessors();
            professorComboBox.getItems().setAll(professorList);
        } catch (ServiceException e) {
            showAlert("Erro", "Sevicio no disponible intentene mas tarde",
                    Alert.AlertType.ERROR);
        } catch (ValidationException e) {
            showAlert("Error", "Error al validar los profesorres",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean isValid(){
        boolean isValid = false;
        LocalDate startdate = startDate.getValue();
        LocalDate enddate = endDate.getValue();

        if (capacityTextField.getText().isEmpty() ||
            nameTextField.getText().isEmpty() ||
            descriptionTextField.getText().isEmpty() ||
                organizationComboBox.getValue() == null ||
                technicalComboBox.getValue() == null ||
                startDate.getValue() == null ||
                endDate.getValue() == null || (!enddate.isAfter(startdate))
            ){

            isValid = false;
        }
        return isValid;
    }

    private void clear(){
        capacityTextField.clear();
        nameTextField.clear();
        descriptionTextField.clear();
        technicalComboBox.getSelectionModel().clearSelection();
        organizationComboBox.getSelectionModel().clearSelection();
        startDate.setValue(null);
        endDate.setValue(null);
    }

    @FXML
    public void cancelButton(ActionEvent actionEvent) {
        clear();
    }

}
