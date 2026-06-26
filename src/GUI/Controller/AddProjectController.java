package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.EducationalExperienceDAO;
import Logic.DAO.LinkedOrganizationDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.EducationalExperience;
import Logic.DTOs.LinkedOrganization;
import Logic.DTOs.Project;
import Logic.DTOs.TechnicalResponsible;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import GUI.Utils.RestrictedTextArea;
import GUI.Utils.RestrictedTextField;
import javafx.scene.layout.AnchorPane;

import java.util.List;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ValidationUtils.applyTextAreaRestriction;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ViewsUtils.openWelcomePage;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class AddProjectController {

    private static final Logger LOGGER = Logger.getLogger(AddProjectController.class.getName());

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private RestrictedTextField capacityTextField;

    @FXML
    private DatePicker endDate;

    @FXML
    private ComboBox<LinkedOrganization> organizationComboBox;

    @FXML
    private RestrictedTextField nameTextField;

    @FXML
    private ComboBox<TechnicalResponsible> technicalComboBox;

    @FXML
    private RestrictedTextField descriptionTextField;

    @FXML
    private DatePicker startDate;

    @FXML
    private ComboBox<EducationalExperience> educationalExperienceComboBox;

    @FXML
    private RestrictedTextArea objetivoTextArea;

    @FXML
    private void initialize() {
        setTypeAndLength(capacityTextField, "Number");
        setTypeAndLength(nameTextField, "Name");
        setTypeAndLength(descriptionTextField, "Text");
        applyTextAreaRestriction(objetivoTextArea, 500);

        loadOrganizations();
        loadEducationalExperiences();
    }

    @FXML
    public void addProject(ActionEvent actionEvent) {
        boolean inputIsValid = hasValidInput();
        if (inputIsValid) {
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

            if (projectDAO.saveProject(project)) {
                LOGGER.log(Level.INFO,
                        "User {0} registered project {1}",
                        new Object[]{SessionManager.getInstance().getUser().getIdUser(), project.getName()});
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
        }
    }

    private Project buildProject() {
        EducationalExperience selectedEE = educationalExperienceComboBox.getValue();
        Project project = new Project();
        project.setNrc(selectedEE.getNrc());
        project.setPeriod(selectedEE.getPeriod());
        project.setName(nameTextField.getText().trim());
        project.setDescription(descriptionTextField.getText().trim());
        project.setIdOrganization(organizationComboBox.getValue().getIdLinkedOrganization());
        project.setIdTechnicalResponsible(technicalComboBox.getValue().getIdTechnicalResponsible());
        project.setIdProfessor(selectedEE.getIdProfessor());
        project.setStartDate(startDate.getValue());
        project.setEndDate(endDate.getValue());
        project.setMaximumPlaces(Integer.parseInt(capacityTextField.getText().trim()));
        project.setAvailablePlaces(Integer.parseInt(capacityTextField.getText().trim()));
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
            List<TechnicalResponsible> technicalSupervisorsList =
                    technicalResponsibleDAO.findByOrganization(organizationId);
            technicalComboBox.getItems().setAll(technicalSupervisorsList);
        } catch (ValidationException | ServiceException loadException) {
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los responsables técnicos. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadEducationalExperiences() {
        try {
            EducationalExperienceDAO educationalExperienceDAO = new EducationalExperienceDAO();
            List<EducationalExperience> allExperiences = educationalExperienceDAO.findAll();
            educationalExperienceComboBox.getItems().setAll(allExperiences);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se pueden cargar las experiencias educativas.",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean hasValidInput() {
        boolean isEEMissing = educationalExperienceComboBox.getValue() == null;
        boolean isOrganizationMissing = organizationComboBox.getValue() == null;
        boolean isTechnicalMissing = technicalComboBox.getValue() == null;
        boolean isNameEmpty = nameTextField.getText().isBlank();
        boolean isDescriptionEmpty = descriptionTextField.getText().isBlank();
        boolean isCapacityEmpty = capacityTextField.getText().isBlank();
        boolean isObjectiveEmpty = objetivoTextArea.getText().isBlank();
        boolean isStartDateMissing = startDate.getValue() == null;
        boolean isEndDateMissing = endDate.getValue() == null;
        boolean hasAllDates = !isStartDateMissing && !isEndDateMissing;
        boolean isDateOrderInvalid = hasAllDates
                && !endDate.getValue().isAfter(startDate.getValue());

        if (isEEMissing) {
            showAlert("Experiencia Educativa requerida",
                    "Seleccione una Experiencia Educativa.", Alert.AlertType.WARNING);
        } else if (isOrganizationMissing) {
            showAlert("Organización requerida",
                    "Seleccione una organización vinculada.", Alert.AlertType.WARNING);
        } else if (isTechnicalMissing) {
            showAlert("Responsable técnico requerido",
                    "Seleccione un responsable técnico.", Alert.AlertType.WARNING);
        } else if (isNameEmpty) {
            showAlert("Nombre requerido",
                    "Ingrese el nombre del proyecto.", Alert.AlertType.WARNING);
        } else if (isDescriptionEmpty) {
            showAlert("Descripción requerida",
                    "Ingrese la descripción del proyecto.", Alert.AlertType.WARNING);
        } else if (isCapacityEmpty) {
            showAlert("Cupo requerido",
                    "Ingrese el cupo máximo del proyecto.", Alert.AlertType.WARNING);
        } else if (isObjectiveEmpty) {
            showAlert("Objetivo requerido",
                    "Ingrese el objetivo del proyecto.", Alert.AlertType.WARNING);
        } else if (isStartDateMissing) {
            showAlert("Fecha de inicio requerida",
                    "Seleccione la fecha de inicio del proyecto.", Alert.AlertType.WARNING);
        } else if (isEndDateMissing) {
            showAlert("Fecha de fin requerida",
                    "Seleccione la fecha de fin del proyecto.", Alert.AlertType.WARNING);
        } else if (isDateOrderInvalid) {
            showAlert("Período incorrecto",
                    "La fecha de fin debe ser posterior a la fecha de inicio.",
                    Alert.AlertType.WARNING);
        }

        boolean isValid = !isEEMissing && !isOrganizationMissing && !isTechnicalMissing
                && !isNameEmpty && !isDescriptionEmpty && !isCapacityEmpty && !isObjectiveEmpty
                && !isStartDateMissing && !isEndDateMissing && !isDateOrderInvalid;

        return isValid;
    }

    private void clear() {
        capacityTextField.clear();
        nameTextField.clear();
        descriptionTextField.clear();
        objetivoTextArea.clear();
        technicalComboBox.getSelectionModel().clearSelection();
        organizationComboBox.getSelectionModel().clearSelection();
        educationalExperienceComboBox.getSelectionModel().clearSelection();
        startDate.setValue(null);
        endDate.setValue(null);
    }

}
