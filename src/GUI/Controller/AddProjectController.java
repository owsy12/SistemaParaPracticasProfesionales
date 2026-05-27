package GUI.Controller;

import Logic.DAO.EducationalExperienceDAO;
import Logic.DAO.LinkedOrganizationDAO;
import Logic.DAO.ProfessorDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.EducationalExperience;
import Logic.DTOs.LinkedOrganization;
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
import javafx.scene.layout.AnchorPane;

import java.time.LocalDate;
import java.util.List;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ViewsUtils.openWelcomePage;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AddProjectController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextField capacityTextField;

    @FXML
    private DatePicker endDate;

    @FXML
    private ComboBox<LinkedOrganization> organizationComboBox;

    @FXML
    private TextField nameTextField;

    @FXML
    private ComboBox<TechnicalSupervisor> technicalComboBox;

    @FXML
    private TextField descriptionTextField;

    @FXML
    private DatePicker startDate;

    @FXML
    private ComboBox<Professor> professorComboBox;

    @FXML
    private ComboBox<EducationalExperience> educationalExperienceComboBox;

    @FXML
    private TextArea objetivoTextArea;

    @FXML
    private void initialize() {
        setTypeAndLength(capacityTextField, "Number");
        setTypeAndLength(nameTextField, "Name");
        setTypeAndLength(descriptionTextField, "Text");

        loadOrganizations();
        loadProfessors();
        loadEducationalExperiences();
    }

    @FXML
    public void addProject(ActionEvent actionEvent) {
        if (!isInputValid()) {
            showAlert("Alerta", "Por favor verifique la información ingresada.",
                    Alert.AlertType.WARNING);
        } else {
            registrationProcess();
        }
    }

    @FXML
    public void cancelButton(ActionEvent actionEvent) {
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmar cancelación",
                "¿Desea salir? Los datos ingresados no se guardarán.",
                Alert.AlertType.CONFIRMATION);
        boolean isConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isConfirmed) {
            clear();
            openWelcomePage(anchorPane);
        }
    }

    @FXML
    private void handleOrganizationSelection(ActionEvent actionEvent) {
        LinkedOrganization linkedOrganization = organizationComboBox.getValue();
        if (linkedOrganization != null) {
            loadTechnicians(linkedOrganization.getIdLinkedOrganization());
        }
    }

    private void registrationProcess() {
        try {
            Project project = buildProject();
            ProjectDAO projectDAO = new ProjectDAO();

            boolean nrcAlreadyUsed = projectDAO.existsByNrc(project.getNrc());
            if (nrcAlreadyUsed) {
                showAlert("EE con proyecto existente",
                        "Ya existe un proyecto registrado para esta Experiencia Educativa. "
                        + "No es posible registrar otro simultáneamente.",
                        Alert.AlertType.WARNING);
                return;
            }

            if (projectDAO.saveProject(project)) {
                showAlert("Éxito", "El proyecto ha sido guardado exitosamente.",
                        Alert.AlertType.INFORMATION);
                clear();
            } else {
                showAlert("Error", "No se pudo guardar el proyecto. Intente nuevamente más tarde.",
                        Alert.AlertType.ERROR);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error inesperado", "El servicio no se encuentra disponible.",
                    Alert.AlertType.ERROR);
        } catch (NullPointerException nullPointerException) {
            showAlert("Precaución", "Seleccione fechas válidas.",
                    Alert.AlertType.INFORMATION);
        }
    }

    private Project buildProject() {
        LocalDate startdate = startDate.getValue();
        LocalDate enddate = endDate.getValue();
        Project project = new Project();
        project.setNrc(educationalExperienceComboBox.getValue().getNrc());
        project.setName(nameTextField.getText());
        project.setDescription(descriptionTextField.getText());
        project.setIdOrganization(organizationComboBox.getValue().getIdLinkedOrganization());
        project.setIdTechnicalSupervisor(technicalComboBox.getValue().getIdTechnicalSupervisor());
        project.setIdProfessor(professorComboBox.getValue().getId());
        project.setStartDate(startdate);
        project.setEndDate(enddate);
        project.setMaximumPlaces(Integer.parseInt(capacityTextField.getText()));
        project.setAvaliablePlaces(Integer.parseInt(capacityTextField.getText()));
        project.setObjetivo(objetivoTextArea.getText().trim());
        return project;
    }

    private void loadOrganizations() {
        try {
            LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();
            List<LinkedOrganization> linkedOrganizations = linkedOrganizationDAO.findAllActive();
            organizationComboBox.getItems().setAll(linkedOrganizations);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Problema con nuestro servicio, intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadTechnicians(int organizationId) {
        try {
            TechnicalResponsibleDAO technicalResponsibleDAO = new TechnicalResponsibleDAO();
            List<TechnicalSupervisor> technicalSupervisorsList =
                    technicalResponsibleDAO.findByOrganization(organizationId);
            technicalComboBox.getItems().setAll(technicalSupervisorsList);
        } catch (ValidationException | ServiceException loadException) {
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los responsables técnicos. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadProfessors() {
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            List<Professor> professorList = professorDAO.findActiveProfessors();
            professorComboBox.getItems().setAll(professorList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible, intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "Error al validar los profesores.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadEducationalExperiences() {
        try {
            EducationalExperienceDAO educationalExperienceDAO = new EducationalExperienceDAO();
            List<EducationalExperience> experienceList = educationalExperienceDAO.findAll();
            educationalExperienceComboBox.getItems().setAll(experienceList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se pueden cargar las experiencias educativas.",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean isInputValid() {
        boolean isCapacityEmpty = capacityTextField.getText().isEmpty();
        boolean isNameEmpty = nameTextField.getText().isEmpty();
        boolean isDescriptionEmpty = descriptionTextField.getText().isEmpty();
        boolean isOrganizationMissing = organizationComboBox.getValue() == null;
        boolean isTechnicalMissing = technicalComboBox.getValue() == null;
        boolean isStartDateMissing = startDate.getValue() == null;
        boolean isProfessorMissing = professorComboBox.getValue() == null;
        boolean isEndDateMissing = endDate.getValue() == null;
        boolean isEducationalExperienceMissing = educationalExperienceComboBox.getValue() == null;

        boolean hasEmptyFields = isCapacityEmpty || isNameEmpty || isDescriptionEmpty || isOrganizationMissing || isTechnicalMissing
                || isStartDateMissing || isProfessorMissing || isEndDateMissing || isEducationalExperienceMissing;

        boolean noFieldsEmpty = !hasEmptyFields;
        boolean isEndAfterStart =
                noFieldsEmpty && endDate.getValue().isAfter(startDate.getValue());

        return isEndAfterStart;
    }

    private void clear() {
        capacityTextField.clear();
        nameTextField.clear();
        descriptionTextField.clear();
        objetivoTextArea.clear();
        technicalComboBox.getSelectionModel().clearSelection();
        organizationComboBox.getSelectionModel().clearSelection();
        professorComboBox.getSelectionModel().clearSelection();
        educationalExperienceComboBox.getSelectionModel().clearSelection();
        startDate.setValue(null);
        endDate.setValue(null);
    }

}
