package GUI.Controller;

import GUI.SessionManager.SessionManager;
import GUI.Utils.EvaluationPrerequisiteChecker;
import GUI.Utils.SelfEvaluationGenerator;
import GUI.Utils.ReportGenerationContext;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.InternDAO;
import Logic.DAO.LinkedOrganizationDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.SelfEvaluationDAO;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.Assignment;
import Logic.DTOs.Intern;
import Logic.DTOs.LinkedOrganization;
import Logic.DTOs.Project;
import Logic.DTOs.SelfEvaluation;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class GenerateSelfEvaluationController {

    private static final Logger LOGGER =
            Logger.getLogger(GenerateSelfEvaluationController.class.getName());
    private static final String GENERATED_STATUS = "Generada";
    private static final String SPRING_PERIOD_START_LABEL = "Febrero - Julio ";
    private static final String FALL_PERIOD_LABEL_TEMPLATE = "Agosto %d - Enero %d";
    private static final int FIRST_SPRING_MONTH = 2;
    private static final int LAST_SPRING_MONTH = 7;
    private static final int JANUARY_MONTH = 1;
    private static final int QUESTIONS_COUNT = 10;

    @FXML
    private TextField nameTextField;
    @FXML
    private TextField idTextField;
    @FXML
    private TextField organizationTextField;
    @FXML
    private TextField departmentTextField;
    @FXML
    private TextField responsibleTextField;
    @FXML
    private TextField projectTextField;
    @FXML
    private ToggleGroup question01ToggleGroup;
    @FXML
    private ToggleGroup question02ToggleGroup;
    @FXML
    private ToggleGroup question03ToggleGroup;
    @FXML
    private ToggleGroup question04ToggleGroup;
    @FXML
    private ToggleGroup question05ToggleGroup;
    @FXML
    private ToggleGroup question06ToggleGroup;
    @FXML
    private ToggleGroup question07ToggleGroup;
    @FXML
    private ToggleGroup question08ToggleGroup;
    @FXML
    private ToggleGroup question09ToggleGroup;
    @FXML
    private ToggleGroup question10ToggleGroup;
    @FXML
    private TextField placeAndDateTextField;
    @FXML
    private Button generateDocumentButton;

    @FXML
    private AnchorPane rootPane;

    private ToggleGroup[] questionToggleGroups;
    private Intern currentIntern;
    private Project currentProject;
    private LinkedOrganization currentOrganization;
    private TechnicalSupervisor currentSupervisor;

    @FXML
    private void initialize() {
        groupQuestionToggleGroups();
        loadInternContext();
    }

    @FXML
    public void generateDocument(ActionEvent actionEvent) {
        if (!hasInternContext()) {
            showAlert("Información incompleta",
                    "No se pudieron recuperar los datos de su práctica. Intente más tarde.",
                    AlertType.WARNING);
        } else if (hasUnansweredQuestions()) {
            showAlert("Cuestionario incompleto",
                    "Debe responder todas las preguntas de la escala Likert.",
                    AlertType.WARNING);
        } else if (isPlaceAndDateEmpty()) {
            showAlert("Campos vacíos",
                    "Debe ingresar el lugar y la fecha.",
                    AlertType.WARNING);
        } else {
            processGeneration();
        }
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        showAlert("Operación cancelada",
                "La generación del documento ha sido cancelada.",
                AlertType.INFORMATION);
        clearForm();
    }

    private void groupQuestionToggleGroups() {
        questionToggleGroups = new ToggleGroup[]{
                question01ToggleGroup, question02ToggleGroup, question03ToggleGroup,
                question04ToggleGroup, question05ToggleGroup, question06ToggleGroup,
                question07ToggleGroup, question08ToggleGroup, question09ToggleGroup,
                question10ToggleGroup
        };
    }

    private boolean hasActiveSession() {
        boolean hasSession = SessionManager.getInstance().getUsuario() != null;
        return hasSession;
    }

    private void loadInternContext() {
        if (!hasActiveSession()) {
            disableGenerationButton();
        } else {
            int internId = SessionManager.getInstance().getUsuario().getId();
            loadInternData(internId);
        }
    }

    private void loadInternData(int internId) {
        try {
            InternDAO internDAO = new InternDAO();
            currentIntern = internDAO.findById(internId);

            AssignmentDAO assignmentDAO = new AssignmentDAO();
            Assignment activeAssignment = assignmentDAO.getActiveByIdIntern(internId);

            if (activeAssignment == null) {
                disableGenerationDueToMissingAssignment();
            } else {
                loadProjectData(activeAssignment);
            }
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    "Los datos para la autoevaluación no son válidos.",
                    AlertType.ERROR);
            disableGenerationButton();
        } catch (DuplicateEntryException duplicateEntryException) {
            showAlert("Conflicto de datos",
                    "Existe un conflicto al recuperar la información del practicante.",
                    AlertType.ERROR);
            disableGenerationButton();
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible",
                    "No se pudieron recuperar sus datos. Intente más tarde.",
                    AlertType.ERROR);
            disableGenerationButton();
        }
    }

    private void loadProjectData(Assignment activeAssignment)
            throws ValidationException, DuplicateEntryException, ServiceException {
        ProjectDAO projectDAO = new ProjectDAO();
        currentProject = projectDAO.findById(activeAssignment.getIdProyect());

        LinkedOrganizationDAO organizationDAO = new LinkedOrganizationDAO();
        currentOrganization = organizationDAO.findById(currentProject.getIdOrganization());

        TechnicalResponsibleDAO supervisorDAO = new TechnicalResponsibleDAO();
        currentSupervisor = supervisorDAO.findById(currentProject.getIdTechnicalSupervisor());

        String prerequisiteMessage = EvaluationPrerequisiteChecker.check(
                currentIntern.getId(),
                currentProject.getIdProyect(),
                currentProject.getStartDate(),
                currentProject.getEndDate()
        );

        if (prerequisiteMessage != null) {
            showAlert("Requisitos no cumplidos", prerequisiteMessage, AlertType.WARNING);
            disableGenerationButton();
            openWelcomePage(rootPane);
            return;
        }

        populateReadOnlyFields();
    }

    private void populateReadOnlyFields() {
        nameTextField.setText(buildInternFullName(currentIntern));
        idTextField.setText(currentIntern.getMatricula());
        organizationTextField.setText(currentOrganization.getName());
        departmentTextField.setText(currentOrganization.getSector());
        responsibleTextField.setText(buildSupervisorFullName(currentSupervisor));
        projectTextField.setText(currentProject.getName());
    }

    private void disableGenerationDueToMissingAssignment() {
        disableGenerationButton();
        showAlert("Sin proyecto asignado",
                "No tiene un proyecto activo asignado. No es posible generar la autoevaluación.",
                AlertType.WARNING);
    }

    private void disableGenerationButton() {
        if (generateDocumentButton != null) {
            generateDocumentButton.setDisable(true);
        }
    }

    private boolean hasInternContext() {
        boolean hasContext = currentIntern != null && currentProject != null && currentOrganization != null
                && currentSupervisor != null;
        return hasContext;
    }

    private boolean hasUnansweredQuestions() {
        boolean hasUnanswered = false;
        for (ToggleGroup toggleGroup : questionToggleGroups) {
            if (toggleGroup.getSelectedToggle() == null) {
                hasUnanswered = true;
            }
        }
        return hasUnanswered;
    }

    private boolean isPlaceAndDateEmpty() {
        String value = placeAndDateTextField.getText();
        boolean isEmpty = value == null || value.isBlank();
        return isEmpty;
    }

    private void processGeneration() {
        try {
            SelfEvaluation selfEvaluation = buildSelfEvaluation();
            selfEvaluation.setDocumentPath("");

            SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();
            int rowsAffected = selfEvaluationDAO.save(selfEvaluation);

            if (rowsAffected > 0) {
                String internalPath = generateAndSavePdf(selfEvaluation);
                selfEvaluationDAO.updateDocumentPath(
                        selfEvaluation.getIdSelfEvalation(), internalPath);

                showAlert("Documento generado",
                        "Autoevaluación generada correctamente.",
                        AlertType.INFORMATION);
                clearForm();
            } else {
                showAlert("Registro fallido",
                        "No se pudo registrar la autoevaluación. Intente nuevamente.",
                        AlertType.ERROR);
            }
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(),
                    AlertType.ERROR);
        } catch (DuplicateEntryException duplicateEntryException) {
            showAlert("Registro duplicado",
                    "Ya existe una autoevaluación registrada para este periodo.",
                    AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Servicio no disponible",
                    "No se pudo registrar la autoevaluación. Intente más tarde.",
                    AlertType.ERROR);
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE,
                    "Error al generar el archivo de autoevaluación: {0}",
                    ioException.getMessage());
            showAlert("Error al generar archivo",
                    "No se pudo crear el documento PDF.",
                    AlertType.ERROR);
        }
    }

    private String generateAndSavePdf(SelfEvaluation selfEvaluation) throws IOException {
        ReportGenerationContext generationContext = buildGenerationContext();
        String generatedFilePath = SelfEvaluationGenerator.generate(selfEvaluation, generationContext);
        return generatedFilePath;
    }

    private ReportGenerationContext buildGenerationContext() {
        javafx.stage.Window ownerWindow = resolveOwnerWindow();
        ReportGenerationContext generationContext = new ReportGenerationContext.Builder()
                .internFullName(buildInternFullName(currentIntern))
                .matricula(currentIntern.getMatricula())
                .organizationName(currentOrganization.getName())
                .organizationDepartment(currentOrganization.getSector())
                .technicianName(buildSupervisorFullName(currentSupervisor))
                .projectName(currentProject.getName())
                .ownerWindow(ownerWindow)
                .build();
        return generationContext;
    }

    private javafx.stage.Window resolveOwnerWindow() {
        javafx.stage.Window resolvedWindow = null;
        if (generateDocumentButton != null && generateDocumentButton.getScene() != null) {
            resolvedWindow = generateDocumentButton.getScene().getWindow();
        }
        return resolvedWindow;
    }

    private SelfEvaluation buildSelfEvaluation() {
        SelfEvaluation selfEvaluation = new SelfEvaluation();
        selfEvaluation.setIdIntern(currentIntern.getId());
        selfEvaluation.setIdProyect(currentProject.getIdProyect());
        selfEvaluation.setPeriod(buildAcademicPeriod());
        selfEvaluation.setStatement01(getSelectedValue(question01ToggleGroup));
        selfEvaluation.setStatement02(getSelectedValue(question02ToggleGroup));
        selfEvaluation.setStatement03(getSelectedValue(question03ToggleGroup));
        selfEvaluation.setStatement04(getSelectedValue(question04ToggleGroup));
        selfEvaluation.setStatement05(getSelectedValue(question05ToggleGroup));
        selfEvaluation.setStatement06(getSelectedValue(question06ToggleGroup));
        selfEvaluation.setStatement07(getSelectedValue(question07ToggleGroup));
        selfEvaluation.setStatement08(getSelectedValue(question08ToggleGroup));
        selfEvaluation.setStatement09(getSelectedValue(question09ToggleGroup));
        selfEvaluation.setStatement10(getSelectedValue(question10ToggleGroup));
        selfEvaluation.setFinalScore(calculateFinalScore(selfEvaluation));
        selfEvaluation.setPlaceAndDate(placeAndDateTextField.getText().trim());
        selfEvaluation.setStatus(GENERATED_STATUS);
        return selfEvaluation;
    }

    private int getSelectedValue(ToggleGroup toggleGroup) {
        Toggle selectedToggle = toggleGroup.getSelectedToggle();
        RadioButton selectedRadio = (RadioButton) selectedToggle;
        int value = Integer.parseInt(selectedRadio.getText());
        return value;
    }

    private int calculateFinalScore(SelfEvaluation selfEvaluation) {
        int totalScore = selfEvaluation.getStatement01() + selfEvaluation.getStatement02()
                + selfEvaluation.getStatement03() + selfEvaluation.getStatement04()
                + selfEvaluation.getStatement05() + selfEvaluation.getStatement06()
                + selfEvaluation.getStatement07() + selfEvaluation.getStatement08()
                + selfEvaluation.getStatement09() + selfEvaluation.getStatement10();
        return totalScore;
    }

    private String buildInternFullName(Intern intern) {
        String fullName = intern.getFirstName() + " " + intern.getLastName()
                + " " + intern.getSecondLastName();
        return fullName;
    }

    private String buildSupervisorFullName(TechnicalSupervisor supervisor) {
        String fullName = supervisor.getName() + " " + supervisor.getLastName()
                + " " + supervisor.getSecondLastName();
        return fullName;
    }
    private String buildAcademicPeriod() {
        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();
        int year = now.getYear();
        String period;

        boolean isSpringMonth = month >= FIRST_SPRING_MONTH && month <= LAST_SPRING_MONTH;
        if (isSpringMonth) {
            period = SPRING_PERIOD_START_LABEL + year;
        } else if (month == JANUARY_MONTH) {
            period = String.format(FALL_PERIOD_LABEL_TEMPLATE, year - 1, year);
        } else {
            period = String.format(FALL_PERIOD_LABEL_TEMPLATE, year, year + 1);
        }
        return period;
    }

    private void clearForm() {
        for (ToggleGroup toggleGroup : questionToggleGroups) {
            toggleGroup.selectToggle(null);
        }
        placeAndDateTextField.clear();
    }
}
