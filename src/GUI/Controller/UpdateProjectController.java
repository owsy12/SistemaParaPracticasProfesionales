package GUI.Controller;

import Logic.DAO.ProfessorDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.Professor;
import Logic.DTOs.Project;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Objects;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;

public class UpdateProjectController {
    private Project project;
    @FXML
    private TextField capacityTextField;
    @FXML
    private ComboBox<Professor> professorComboBox;
    @FXML
    private DatePicker endDate;
    @FXML
    private ComboBox organizationComboBox;
    @FXML
    private TextField nameTextField;
    @FXML
    private ComboBox<TechnicalSupervisor> technicalComboBox;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private DatePicker startDate;

    @FXML
    public void cancelButton(ActionEvent actionEvent) {
    }

    @FXML
    public void updateProject(ActionEvent actionEvent) {
        if (!noEmptyValues() && (Integer.parseInt(capacityTextField.getText()) >= project.getMaximumPlaces())){
            updateProjectProcess();
        } else {
            showAlert("Advetencia", "Todos los campos deben de estar llenos",
                    Alert.AlertType.WARNING);
        }
    }

    private void updateProjectProcess(){
        try {
            Project updateProject = project;
            updateProject.setName(nameTextField.getText());
            updateProject.setDescription(descriptionTextField.getText());
            updateProject.setMaximumPlaces(Integer.parseInt(capacityTextField.getText()));
            updateProject.setIdProfessor(professorComboBox.getValue().getId());
            updateProject.setIdTechnicalSupervisor(technicalComboBox.getValue().getIdTechnicalSupervisor());

            if (updateProject.equals(project)){
                ProjectDAO projectDAO = new ProjectDAO();
                projectDAO.update(updateProject);
                showAlert("Exito", "Proyecto actualizad correctamente",
                        Alert.AlertType.INFORMATION);
            } else {
                showAlert("Informacion", "No se encontraron cambios a realizar",
                        Alert.AlertType.INFORMATION);
            }

        } catch (ValidationException e) {
            showAlert("Error","No se logro comprobar el proyecto a modiifcar",
                    Alert.AlertType.ERROR);
        } catch (ServiceException e) {
            showAlert("Error", "servicio No disponible ",
                    Alert.AlertType.ERROR);
        }
    }

    private void configureProjectInformation(){

            capacityTextField.setText(String.valueOf(project.getMaximumPlaces()));
            endDate.setValue(project.getEndDate());
            startDate.setValue(project.getStartDate());
            nameTextField.setText(project.getName());
            descriptionTextField.setText(project.getDescription());
            organizationComboBox.getItems().add(project.getOrganizationName());
            organizationComboBox.getSelectionModel().selectFirst();
            professorComboBox.getItems().setAll(getProfessorList());
            technicalComboBox.getItems().setAll(getProjectTechnicalList(project.getIdOrganization()));


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

        professorComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Professor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFirstName());
            }
        });

        professorComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Professor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFirstName());
            }
        });

        technicalComboBox.getItems().stream()
                .filter(technicalSupervisor -> technicalSupervisor.getIdTechnicalSupervisor()
                        == project.getIdTechnicalSupervisor())
                .findFirst()
                .ifPresent(technicalSupervisor ->
                        technicalComboBox.getSelectionModel().select(technicalSupervisor));

        professorComboBox.getItems().stream()
                .filter(professor -> professor.getId()
                        == project.getIdProfessor())
                .findFirst()
                .ifPresent(professor ->
                        professorComboBox.getSelectionModel().select(professor));

        endDate.setDisable(true);
        startDate.setDisable(true);
        setTypeAndLength(nameTextField, "Name");
        setTypeAndLength(descriptionTextField, "Text");
        setTypeAndLength(capacityTextField, "Number");
    }

    private List<TechnicalSupervisor> getProjectTechnicalList(int idOrganization){
        List<TechnicalSupervisor> technicalSupervisorsList = null;
        try {
            TechnicalResponsibleDAO technicalResponsibleDAO = new TechnicalResponsibleDAO();
            technicalSupervisorsList = technicalResponsibleDAO.findByOrganization(idOrganization);
        } catch (ValidationException e) {
            showAlert("error", "no se logro validar la existecia ",
                    Alert.AlertType.ERROR);
        } catch (ServiceException e) {
            showAlert("Error", "servicio no dippnible intente mas tarde",
                    Alert.AlertType.ERROR);
        }

        return technicalSupervisorsList;
    }

    private List<Professor> getProfessorList(){
        List<Professor> professorList = null;

        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            professorList = professorDAO.findActiveProfessors();
        } catch (ServiceException e) {
            showAlert("Error", "servicio no disponivle",
                    Alert.AlertType.ERROR);
        } catch (ValidationException e) {
            showAlert("Error", "suceso inesperado intente luego",
                    Alert.AlertType.ERROR);
        }

        return professorList;
    }

    private boolean noEmptyValues(){
        boolean isEmpty = false;

        if (nameTextField.getText().isEmpty() ||
                descriptionTextField.getText().isEmpty() ||
            capacityTextField.getText().isEmpty() ||
            professorComboBox.getValue() == null ||
                technicalComboBox.getValue() == null){
            isEmpty = true;
        }

        return isEmpty;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        configureProjectInformation();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UpdateProjectController that = (UpdateProjectController) o;
        return Objects.equals(project, that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(project);
    }
}
