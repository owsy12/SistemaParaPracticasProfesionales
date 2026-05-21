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
    private TextField nrcTextField;
    @FXML
    private TextArea objetivoTextArea;

    @FXML
    public void cancelButton(ActionEvent actionEvent) {
        configureProjectInformation();
    }

    @FXML
    public void updateProject(ActionEvent actionEvent) {
        if (hasEmptyFields()) {
            showAlert("Campos vacíos", "Todos los campos deben estar llenos.",
                    Alert.AlertType.WARNING);
        } else {
            updateProjectProcess();
        }
    }

    private void updateProjectProcess() {
        try {
            Project originalSnapshot = buildSnapshot();

            project.setName(nameTextField.getText().trim());
            project.setDescription(descriptionTextField.getText().trim());
            project.setObjetivo(objetivoTextArea.getText().trim());
            project.setMaximumPlaces(Integer.parseInt(capacityTextField.getText().trim()));
            project.setIdProfessor(professorComboBox.getValue().getId());
            project.setIdTechnicalSupervisor(
                    technicalComboBox.getValue().getIdTechnicalSupervisor());

            if (project.equals(originalSnapshot)) {
                showAlert("Sin cambios",
                        "No se detectaron cambios en el proyecto.",
                        Alert.AlertType.INFORMATION);
            } else {
                ProjectDAO projectDAO = new ProjectDAO();
                projectDAO.update(project);
                showAlert("Éxito", "Proyecto actualizado correctamente.",
                        Alert.AlertType.INFORMATION);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    "No se pudo validar la información del proyecto.",
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible",
                    "No se pudo actualizar el proyecto. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private Project buildSnapshot() {
        Project snapshot = new Project();
        snapshot.setName(project.getName());
        snapshot.setDescription(project.getDescription());
        snapshot.setObjetivo(project.getObjetivo());
        snapshot.setMaximumPlaces(project.getMaximumPlaces());
        snapshot.setIdProfessor(project.getIdProfessor());
        snapshot.setIdTechnicalSupervisor(project.getIdTechnicalSupervisor());
        return snapshot;
    }

    private void configureProjectInformation(){

        nrcTextField.setText(String.valueOf(project.getIdProyect()));
        nrcTextField.setDisable(true);
        capacityTextField.setText(String.valueOf(project.getMaximumPlaces()));
        endDate.setValue(project.getEndDate());
        startDate.setValue(project.getStartDate());
        nameTextField.setText(project.getName());
        descriptionTextField.setText(project.getDescription());
        if (project.getObjetivo() != null) {
            objetivoTextArea.setText(project.getObjetivo());
        }
        organizationComboBox.getItems().add(project.getOrganizationName());
        organizationComboBox.getSelectionModel().selectFirst();
        organizationComboBox.setDisable(true);
        professorComboBox.getItems().setAll(getProfessorList());
        technicalComboBox.getItems().setAll(getProjectTechnicalList(project.getIdOrganization()));


        technicalComboBox.setCellFactory(column -> new ListCell<>() {
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

        professorComboBox.setCellFactory(column -> new ListCell<>() {
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
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    "No se pudieron recuperar los responsables técnicos.",
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los responsables. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }

        return technicalSupervisorsList;
    }

    private List<Professor> getProfessorList(){
        List<Professor> professorList = null;

        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            professorList = professorDAO.findActiveProfessors();
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los profesores. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    "No se pudieron recuperar los profesores.",
                    Alert.AlertType.ERROR);
        }

        return professorList;
    }

    private boolean hasEmptyFields() {
        boolean isEmpty = nameTextField.getText().isBlank()
                || descriptionTextField.getText().isBlank()
                || capacityTextField.getText().isBlank()
                || objetivoTextArea.getText().isBlank()
                || professorComboBox.getValue() == null
                || technicalComboBox.getValue() == null;
        return isEmpty;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        configureProjectInformation();
    }

}
