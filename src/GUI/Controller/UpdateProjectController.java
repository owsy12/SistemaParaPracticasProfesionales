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
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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

    private void configureProjectInformation() {
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

        preselectTechnicalSupervisor();
        preselectProfessor();

        endDate.setDisable(true);
        startDate.setDisable(true);
        setTypeAndLength(nameTextField, "Name");
        setTypeAndLength(descriptionTextField, "Text");
        setTypeAndLength(capacityTextField, "Number");
    }

    private void preselectTechnicalSupervisor() {
        List<TechnicalSupervisor> technicalList = technicalComboBox.getItems();
        for (TechnicalSupervisor technicalSupervisor : technicalList) {
            boolean matchesProject =
                    technicalSupervisor.getIdTechnicalSupervisor() == project.getIdTechnicalSupervisor();
            if (matchesProject) {
                technicalComboBox.getSelectionModel().select(technicalSupervisor);
                break;
            }
        }
    }

    private void preselectProfessor() {
        List<Professor> professorList = professorComboBox.getItems();
        for (Professor professor : professorList) {
            boolean matchesProject = professor.getId() == project.getIdProfessor();
            if (matchesProject) {
                professorComboBox.getSelectionModel().select(professor);
                break;
            }
        }
    }

    private List<TechnicalSupervisor> getProjectTechnicalList(int idOrganization) {
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

    private List<Professor> getProfessorList() {
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
        boolean isNameEmpty = nameTextField.getText().isBlank();
        boolean isDescriptionEmpty = descriptionTextField.getText().isBlank();
        boolean isCapacityEmpty = capacityTextField.getText().isBlank();
        boolean isObjectiveEmpty = objetivoTextArea.getText().isBlank();
        boolean isProfessorMissing = professorComboBox.getValue() == null;
        boolean isTechnicalMissing = technicalComboBox.getValue() == null;

        boolean hasEmpty = isNameEmpty || isDescriptionEmpty || isCapacityEmpty || isObjectiveEmpty
                || isProfessorMissing || isTechnicalMissing;

        return hasEmpty;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        configureProjectInformation();
    }

}
