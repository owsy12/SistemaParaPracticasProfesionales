package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ApplicationDAO;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.InitialFormatDAO;
import Logic.DAO.InternActivityDAO;
import Logic.DAO.InternDAO;
import Logic.DAO.OVEvaluationDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ProfessorDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.ReportDAO;
import Logic.DAO.SelfEvaluationDAO;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.Intern;
import Logic.DTOs.Professor;
import Logic.DTOs.Project;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import GUI.Utils.RestrictedTextArea;
import javafx.scene.control.TextField;
import GUI.Utils.RestrictedTextField;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ValidationUtils.applyTextAreaRestriction;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class UpdateProjectController implements ChangeListener<Intern> {

    private static final Logger LOGGER = Logger.getLogger(UpdateProjectController.class.getName());

    @FXML
    private javafx.scene.layout.AnchorPane anchorPane;

    private Project project;
    private Intern selectedIntern;

    @FXML
    private RestrictedTextField capacityTextField;

    @FXML
    private ComboBox<Professor> professorComboBox;

    @FXML
    private DatePicker endDate;

    @FXML
    private ComboBox organizationComboBox;

    @FXML
    private RestrictedTextField nameTextField;

    @FXML
    private ComboBox<TechnicalSupervisor> technicalComboBox;

    @FXML
    private RestrictedTextField descriptionTextField;

    @FXML
    private DatePicker startDate;

    @FXML
    private TextField nrcTextField;

    @FXML
    private RestrictedTextArea objetivoTextArea;

    @FXML
    private TableView<Intern> internsTableView;

    @FXML
    private TableColumn<Intern, String> internNameColumn;

    @FXML
    private TableColumn<Intern, String> internRegistrationNumberColumn;

    @FXML
    private Label internsStatusLabel;

    @FXML
    private Button removeInternButton;

    @FXML
    public void cancelButton(ActionEvent actionEvent) {
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmar cancelación",
                "¿Desea salir? Los cambios no guardados se perderán.",
                Alert.AlertType.CONFIRMATION);
        boolean isConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isConfirmed) {
            openWelcomePage(anchorPane);
        }
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

    @FXML
    public void removeIntern(ActionEvent actionEvent) {
        boolean isInternMissing = selectedIntern == null;
        if (isInternMissing) {
            internsStatusLabel.setText("Seleccione un practicante de la tabla.");
            internsStatusLabel.setStyle("-fx-text-fill: red;");
        } else {

            String confirmMessage = "¿Eliminar la asignación de "
                    + selectedIntern.getFullName() + " de este proyecto?";
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, confirmMessage,
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = confirmation.showAndWait();

            boolean isConfirmed = result.isPresent() && result.get() == ButtonType.YES;
            if (isConfirmed) {
                removeInternProcess();
            }
        }
    }

    private void removeInternProcess() {
        int internId = selectedIntern.getId();
        int projectId = project.getIdProject();

        try {
            deleteInternData(internId, projectId);

            AssignmentDAO assignmentDAO = new AssignmentDAO();
            boolean deleted = assignmentDAO.deleteByInternAndProject(internId, projectId);

            if (deleted) {
                ProjectDAO projectDAO = new ProjectDAO();
                projectDAO.incrementAvailableSlot(projectId);

                showAlert("Practicante eliminado",
                        "La asignación y todos los datos asociados fueron eliminados.",
                        Alert.AlertType.INFORMATION);
                loadInternsForProject();
                selectedIntern = null;
                internsStatusLabel.setText("");
            } else {
                showAlert("Error",
                        "No se encontró la asignación del practicante en este proyecto.",
                        Alert.AlertType.ERROR);
            }

        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al eliminar asignación del practicante {0} del proyecto {1}: {2}",
                    new Object[]{internId, projectId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo eliminar la asignación. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void deleteInternData(int internId, int projectId) throws ServiceException, ValidationException {
        new ReportDAO().deleteByInternAndProject(internId, projectId);
        new InitialFormatDAO().deleteByInternAndProject(internId, projectId);
        new SelfEvaluationDAO().deleteByInternAndProject(internId, projectId);
        new OVEvaluationDAO().deleteByInternAndProject(internId, projectId);
        new InternActivityDAO().deleteByInternAndProject(internId, projectId);
        new PracticeDAO().cancelActiveByInternAndProject(internId, projectId);
        new ApplicationDAO().cancelAcceptedByIntern(internId);
    }

    private void updateProjectProcess() {
        try {
            int newCapacity = Integer.parseInt(capacityTextField.getText().trim());
            int currentInternCount = internsTableView.getItems().size();
            boolean isCapacityTooLow = newCapacity < currentInternCount;

            if (isCapacityTooLow) {
                showAlert("Capacidad inválida",
                        "La capacidad no puede ser menor a la cantidad actual de practicantes asociados al proyecto.",
                        Alert.AlertType.WARNING);
            } else {
                Project originalSnapshot = buildSnapshot();

                project.setName(nameTextField.getText().trim());
                project.setDescription(descriptionTextField.getText().trim());
                project.setObjetivo(objetivoTextArea.getText().trim());
                project.setMaximumPlaces(newCapacity);
                project.setAvaliablePlaces(newCapacity - currentInternCount);
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
                    LOGGER.log(Level.INFO,
                            "Usuario {0} actualizó el proyecto {1}",
                            new Object[]{SessionManager.getInstance().getUser().getId(), project.getName()});
                    showAlert("Éxito", "Proyecto actualizado correctamente.",
                            Alert.AlertType.INFORMATION);
                    openWelcomePage(anchorPane);
                }
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
        snapshot.setAvaliablePlaces(project.getAvaliablePlaces());
        snapshot.setIdProfessor(project.getIdProfessor());
        snapshot.setIdTechnicalSupervisor(project.getIdTechnicalSupervisor());
        return snapshot;
    }

    private void configureListeners() {
        internsTableView.getSelectionModel().selectedItemProperty().addListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends Intern> observable, Intern oldValue, Intern newValue) {
        if (newValue != null) {
            selectedIntern = newValue;
            String selectionText = "Practicante seleccionado: " + newValue.getFullName();
            internsStatusLabel.setText(selectionText);
            internsStatusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    private void configureProjectInformation() {
        nrcTextField.setText(project.getNrc() != null ? project.getNrc() : "");
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
        applyTextAreaRestriction(objetivoTextArea, 500);

        loadInternsForProject();
    }

    private void loadInternsForProject() {
        try {
            InternDAO internDAO = new InternDAO();
            List<Intern> interns = internDAO.findByProject(project.getIdProject());
            internsTableView.setItems(FXCollections.observableArrayList(interns));
            internsStatusLabel.setText("");
            selectedIntern = null;
        } catch (ValidationException validationException) {
            LOGGER.log(Level.SEVERE, "Error de validación al cargar practicantes: {0}",
                    validationException.getMessage());
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar practicantes del proyecto {0}: {1}",
                    new Object[]{project.getIdProject(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los practicantes. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
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
        configureListeners();
        configureProjectInformation();
    }

}
