package GUI.Controller;

import GUI.SessionManager.SessionManager;
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
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;

public class GenerateSelfEvaluationController {

    private static final Logger LOGGER =
            Logger.getLogger(GenerateSelfEvaluationController.class.getName());

    private static final int MAXIMUM_FINAL_SCORE = 50;
    private static final String DOCUMENTS_DIRECTORY = "documents/autoevaluaciones";
    private static final String DOCUMENT_FILE_PREFIX = "autoevaluacion_";
    private static final String DOCUMENT_FILE_EXTENSION = ".txt";
    private static final String FILE_TIMESTAMP_PATTERN = "yyyyMMddHHmmss";
    private static final String GENERATED_STATUS = "Generada";
    private static final String SPRING_PERIOD_START_LABEL = "Febrero - Julio ";
    private static final String FALL_PERIOD_LABEL_TEMPLATE = "Agosto %d - Enero %d";
    private static final int FIRST_SPRING_MONTH = 2;
    private static final int LAST_SPRING_MONTH = 7;
    private static final int JANUARY_MONTH = 1;

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
            return;
        }
        if (hasUnansweredQuestions()) {
            showAlert("Cuestionario incompleto",
                    "Debe responder todas las preguntas de la escala Likert.",
                    AlertType.WARNING);
            return;
        }
        if (isPlaceAndDateEmpty()) {
            showAlert("Campos vacíos",
                    "Debe ingresar el lugar y la fecha.",
                    AlertType.WARNING);
            return;
        }
        processGeneration();
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
        return SessionManager.getInstance().getUsuario() != null;
    }

    private void loadInternContext() {
        if (!hasActiveSession()) {
            disableGenerationButton();
            return;
        }
        int internId = SessionManager.getInstance().getUsuario().getId();

        try {
            InternDAO internDAO = new InternDAO();
            currentIntern = internDAO.findById(internId);

            AssignmentDAO assignmentDAO = new AssignmentDAO();
            Assignment activeAssignment = assignmentDAO.getActiveByIdIntern(internId);

            if (activeAssignment == null) {
                disableGenerationDueToMissingAssignment();
                return;
            }

            ProjectDAO projectDAO = new ProjectDAO();
            currentProject = projectDAO.findById(activeAssignment.getIdProyect());

            LinkedOrganizationDAO organizationDAO = new LinkedOrganizationDAO();
            currentOrganization = organizationDAO.findById(currentProject.getIdOrganization());

            TechnicalResponsibleDAO supervisorDAO = new TechnicalResponsibleDAO();
            currentSupervisor = supervisorDAO.findById(currentProject.getIdTechnicalSupervisor());

            populateReadOnlyFields();
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
        return currentIntern != null
                && currentProject != null
                && currentOrganization != null
                && currentSupervisor != null;
    }

    private boolean hasUnansweredQuestions() {
        boolean hasUnanswered = false;

        for (ToggleGroup toggleGroup : questionToggleGroups) {
            if (toggleGroup.getSelectedToggle() == null) {
                hasUnanswered = true;
                break;
            }
        }

        return hasUnanswered;
    }

    private boolean isPlaceAndDateEmpty() {
        String placeAndDateValue = placeAndDateTextField.getText();
        return placeAndDateValue == null || placeAndDateValue.isBlank();
    }

    private void processGeneration() {
        try {
            SelfEvaluation selfEvaluation = buildSelfEvaluation();
            String documentPath = writeDocumentToFile(selfEvaluation);
            selfEvaluation.setDocumentPath(documentPath);

            SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();
            int rowsAffected = selfEvaluationDAO.save(selfEvaluation);

            if (rowsAffected > 0) {
                showAlert("Documento generado",
                        "Su autoevaluación se generó correctamente en:\n" + documentPath,
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
                    "Error al escribir el archivo de autoevaluación: {0}",
                    ioException.getMessage());
            showAlert("Error al generar archivo",
                    "No se pudo crear el documento en disco.",
                    AlertType.ERROR);
        }
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
        return Integer.parseInt(selectedRadio.getText());
    }

    private int calculateFinalScore(SelfEvaluation selfEvaluation) {
        return selfEvaluation.getStatement01() + selfEvaluation.getStatement02()
                + selfEvaluation.getStatement03() + selfEvaluation.getStatement04()
                + selfEvaluation.getStatement05() + selfEvaluation.getStatement06()
                + selfEvaluation.getStatement07() + selfEvaluation.getStatement08()
                + selfEvaluation.getStatement09() + selfEvaluation.getStatement10();
    }

    private String writeDocumentToFile(SelfEvaluation selfEvaluation) throws IOException {
        Path documentsDirectory = Paths.get(DOCUMENTS_DIRECTORY);
        Files.createDirectories(documentsDirectory);

        String fileName = buildDocumentFileName();
        Path documentFile = documentsDirectory.resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(documentFile, StandardCharsets.UTF_8)) {
            writer.write(buildDocumentContent(selfEvaluation));
        }

        return documentFile.toAbsolutePath().toString();
    }

    private String buildDocumentFileName() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(FILE_TIMESTAMP_PATTERN));
        return DOCUMENT_FILE_PREFIX + currentIntern.getMatricula() + "_" + timestamp
                + DOCUMENT_FILE_EXTENSION;
    }

    private String buildDocumentContent(SelfEvaluation selfEvaluation) {
        StringBuilder content = new StringBuilder();
        content.append("AUTOEVALUACIÓN DE PRÁCTICAS PROFESIONALES").append(System.lineSeparator());
        content.append("=========================================").append(System.lineSeparator())
                .append(System.lineSeparator());
        content.append("Practicante: ").append(buildInternFullName(currentIntern))
                .append(System.lineSeparator());
        content.append("Matrícula: ").append(currentIntern.getMatricula())
                .append(System.lineSeparator());
        content.append("Organización: ").append(currentOrganization.getName())
                .append(System.lineSeparator());
        content.append("Departamento: ").append(currentOrganization.getSector())
                .append(System.lineSeparator());
        content.append("Responsable: ").append(buildSupervisorFullName(currentSupervisor))
                .append(System.lineSeparator());
        content.append("Proyecto: ").append(currentProject.getName())
                .append(System.lineSeparator());
        content.append("Periodo: ").append(selfEvaluation.getPeriod())
                .append(System.lineSeparator()).append(System.lineSeparator());

        content.append("RESPUESTAS DE LA ESCALA LIKERT (1-5)").append(System.lineSeparator());
        content.append("------------------------------------").append(System.lineSeparator());
        content.append("1.  Apliqué conocimientos teóricos en la práctica: ")
                .append(selfEvaluation.getStatement01()).append(System.lineSeparator());
        content.append("2.  Cumplí con los objetivos establecidos: ")
                .append(selfEvaluation.getStatement02()).append(System.lineSeparator());
        content.append("3.  Trabajé de forma colaborativa con el equipo: ")
                .append(selfEvaluation.getStatement03()).append(System.lineSeparator());
        content.append("4.  Administré adecuadamente mi tiempo: ")
                .append(selfEvaluation.getStatement04()).append(System.lineSeparator());
        content.append("5.  Demostré iniciativa y proactividad: ")
                .append(selfEvaluation.getStatement05()).append(System.lineSeparator());
        content.append("6.  Resolví problemas de manera efectiva: ")
                .append(selfEvaluation.getStatement06()).append(System.lineSeparator());
        content.append("7.  Recibí retroalimentación constructiva: ")
                .append(selfEvaluation.getStatement07()).append(System.lineSeparator());
        content.append("8.  Mejoré mis habilidades técnicas: ")
                .append(selfEvaluation.getStatement08()).append(System.lineSeparator());
        content.append("9.  Me comuniqué efectivamente con mi responsable: ")
                .append(selfEvaluation.getStatement09()).append(System.lineSeparator());
        content.append("10. La práctica fue relevante para mi formación: ")
                .append(selfEvaluation.getStatement10())
                .append(System.lineSeparator()).append(System.lineSeparator());

        content.append("Puntuación final: ").append(selfEvaluation.getFinalScore())
                .append(" / ").append(MAXIMUM_FINAL_SCORE)
                .append(System.lineSeparator()).append(System.lineSeparator());
        content.append("Lugar y fecha: ").append(selfEvaluation.getPlaceAndDate())
                .append(System.lineSeparator());

        return content.toString();
    }

    private String buildInternFullName(Intern intern) {
        return intern.getFirstName() + " " + intern.getLastName()
                + " " + intern.getSecondLastName();
    }

    private String buildSupervisorFullName(TechnicalSupervisor supervisor) {
        return supervisor.getName() + " " + supervisor.getLastName()
                + " " + supervisor.getSecondLastName();
    }

    private String buildAcademicPeriod() {
        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        if (month >= FIRST_SPRING_MONTH && month <= LAST_SPRING_MONTH) {
            return SPRING_PERIOD_START_LABEL + year;
        }
        if (month == JANUARY_MONTH) {
            return String.format(FALL_PERIOD_LABEL_TEMPLATE, year - 1, year);
        }
        return String.format(FALL_PERIOD_LABEL_TEMPLATE, year, year + 1);
    }

    private void clearForm() {
        for (ToggleGroup toggleGroup : questionToggleGroups) {
            toggleGroup.selectToggle(null);
        }
        placeAndDateTextField.clear();
    }
}