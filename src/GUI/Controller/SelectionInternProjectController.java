package GUI.Controller;

import Logic.DAO.InitialFormatDAO;
import Logic.DAO.InternDAO;
import Logic.DAO.OVEvaluationDAO;
import Logic.DAO.PracticeDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.ReportDAO;
import Logic.DAO.SelfEvaluationDAO;
import Logic.DTOs.EducationalExperience;
import Logic.DTOs.InitialFormat;
import Logic.DTOs.Intern;
import Logic.DTOs.OVEvaluation;
import Logic.DTOs.Project;
import Logic.DTOs.Report;
import Logic.DTOs.SelfEvaluation;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ViewsUtils.findContentPane;
import static GUI.Utils.ViewsUtils.wrapInScrollableContent;

public class SelectionInternProjectController {

    private static final Logger LOGGER =
            Logger.getLogger(SelectionInternProjectController.class.getName());

    private static final String FILTER_ALL = "Todos";
    private static final String DOCUMENT_TYPE_SELF_EVALUATION = "Autoevaluación";
    private static final String DOCUMENT_TYPE_OV_EVALUATION = "Evaluación OV";
    private static final String DOCUMENT_TYPE_CLOSURE_RECORD = "Acta de cierre";
    private static final String STATUS_CLOSURE_PENDING = "Pendiente de validación";
    private static final String STATUS_CLOSURE_VALIDATED = "Validada";
    private static final int DOCUMENT_TYPE_INDEX = 0;
    private static final int DOCUMENT_STATUS_INDEX = 1;

    @FXML
    private ComboBox<String> documentTypeComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Label experienceContextLabel;

    @FXML
    private TableView<Intern> internsTable;

    @FXML
    private TableColumn<Intern, String> registrationNumberColumn;

    @FXML
    private TableColumn<Intern, String> fullNameColumn;

    @FXML
    private TableColumn<Intern, String> projectColumn;

    private EducationalExperience currentExperience;
    private final Map<Integer, Project> internProjectMap = new HashMap<>();
    private final Map<Integer, List<String[]>> internDocumentsMap = new HashMap<>();
    private List<Intern> allInterns = new ArrayList<>();

    public void setReviewContext(EducationalExperience experience) {
        currentExperience = experience;
        experienceContextLabel.setText("EE: " + experience.toString());
        loadInterns(experience);
    }

    @FXML
    public void openInternReports(ActionEvent actionEvent) {
        Intern selectedIntern = internsTable.getSelectionModel().getSelectedItem();
        boolean isInternMissing = selectedIntern == null;
        if (isInternMissing) {
            showAlert("Sin selección",
                    "Seleccione un practicante de la tabla.", Alert.AlertType.WARNING);
        } else {
            Project project = internProjectMap.get(selectedIntern.getIdUser());
            openReportReview(project, selectedIntern);
        }
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        navigateTo("/GUI/View/GUISelectEducationalExperience.fxml");
    }

    private void loadInterns(EducationalExperience experience) {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            InternDAO internDAO = new InternDAO();
            List<Project> projects = projectDAO.findByEducationalExperience(
                    experience.getNrc(), experience.getPeriod());

            allInterns = new ArrayList<>();
            internProjectMap.clear();
            internDocumentsMap.clear();
            for (Project project : projects) {
                List<Intern> interns = internDAO.findByProject(project.getIdProject());
                for (Intern intern : interns) {
                    intern.setProjectName(project.getName());
                    internProjectMap.put(intern.getIdUser(), project);
                    internDocumentsMap.put(intern.getIdUser(), collectInternDocuments(intern, project));
                    allInterns.add(intern);
                }
            }

            populateFilterOptions();
            filterInterns();
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error loading interns for educational experience {0}: {1}",
                    new Object[]{experience.getNrc(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los practicantes. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void applyFilters(ActionEvent actionEvent) {
        filterInterns();
    }

    private void filterInterns() {
        String selectedType = documentTypeComboBox.getValue();
        String selectedStatus = statusComboBox.getValue();
        List<Intern> filteredInterns = new ArrayList<>();

        for (Intern intern : allInterns) {
            boolean matchesFilters = internHasMatchingDocument(intern, selectedType, selectedStatus);
            if (matchesFilters) {
                filteredInterns.add(intern);
            }
        }

        internsTable.setItems(FXCollections.observableArrayList(filteredInterns));
    }

    private boolean internHasMatchingDocument(Intern intern, String selectedType, String selectedStatus) {
        boolean typeFilterActive = selectedType != null && !FILTER_ALL.equals(selectedType);
        boolean statusFilterActive = selectedStatus != null && !FILTER_ALL.equals(selectedStatus);
        boolean hasMatch = !typeFilterActive && !statusFilterActive;

        List<String[]> documents = internDocumentsMap.get(intern.getIdUser());
        boolean shouldInspectDocuments = !hasMatch && documents != null;
        if (shouldInspectDocuments) {
            for (String[] document : documents) {
                boolean matchesType = !typeFilterActive
                        || selectedType.equals(document[DOCUMENT_TYPE_INDEX]);
                boolean matchesStatus = !statusFilterActive
                        || selectedStatus.equals(document[DOCUMENT_STATUS_INDEX]);
                if (matchesType && matchesStatus) {
                    hasMatch = true;
                }
            }
        }

        return hasMatch;
    }

    private List<String[]> collectInternDocuments(Intern intern, Project project)
            throws ServiceException, ValidationException {
        List<String[]> documents = new ArrayList<>();

        ReportDAO reportDAO = new ReportDAO();
        List<Report> reports = reportDAO.getByInternAndProject(intern.getIdUser(), project.getIdProject());
        for (Report report : reports) {
            documents.add(new String[]{report.getReportType(), report.getStatus()});
        }

        InitialFormatDAO initialFormatDAO = new InitialFormatDAO();
        List<InitialFormat> initialFormats = initialFormatDAO.getByIdIntern(intern.getIdUser());
        for (InitialFormat initialFormat : initialFormats) {
            documents.add(new String[]{initialFormat.getFormatType(), initialFormat.getStatus()});
        }

        SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();
        SelfEvaluation selfEvaluation = selfEvaluationDAO.findByIdIntern(intern.getIdUser());
        if (selfEvaluation != null) {
            documents.add(new String[]{DOCUMENT_TYPE_SELF_EVALUATION, selfEvaluation.getStatus()});
        }

        OVEvaluationDAO ovEvaluationDAO = new OVEvaluationDAO();
        OVEvaluation ovEvaluation = ovEvaluationDAO.findByInternAndProject(
                intern.getIdUser(), project.getIdProject());
        if (ovEvaluation != null) {
            documents.add(new String[]{DOCUMENT_TYPE_OV_EVALUATION, ovEvaluation.getStatus()});
        }

        PracticeDAO practiceDAO = new PracticeDAO();
        String closureRecordPath = practiceDAO.findClosureRecordPath(intern.getIdUser());
        boolean isClosureRecordSubmitted = closureRecordPath != null && !closureRecordPath.isBlank();
        if (isClosureRecordSubmitted) {
            String closureStatus = STATUS_CLOSURE_PENDING;
            boolean isPracticeConcluded = practiceDAO.hasConcludedPractice(intern.getIdUser());
            if (isPracticeConcluded) {
                closureStatus = STATUS_CLOSURE_VALIDATED;
            }
            documents.add(new String[]{DOCUMENT_TYPE_CLOSURE_RECORD, closureStatus});
        }

        return documents;
    }

    private void populateFilterOptions() {
        TreeSet<String> documentTypes = new TreeSet<>();
        TreeSet<String> documentStatuses = new TreeSet<>();

        for (List<String[]> documents : internDocumentsMap.values()) {
            for (String[] document : documents) {
                String documentType = document[DOCUMENT_TYPE_INDEX];
                boolean hasType = documentType != null && !documentType.isBlank();
                if (hasType) {
                    documentTypes.add(documentType);
                }
                String documentStatus = document[DOCUMENT_STATUS_INDEX];
                boolean hasStatus = documentStatus != null && !documentStatus.isBlank();
                if (hasStatus) {
                    documentStatuses.add(documentStatus);
                }
            }
        }

        refreshFilterComboBox(documentTypeComboBox, documentTypes);
        refreshFilterComboBox(statusComboBox, documentStatuses);
    }

    private void refreshFilterComboBox(ComboBox<String> comboBox, Collection<String> values) {
        String previousSelection = comboBox.getValue();
        List<String> options = new ArrayList<>();
        options.add(FILTER_ALL);
        options.addAll(values);

        comboBox.getItems().setAll(options);

        boolean keepsSelection = previousSelection != null && options.contains(previousSelection);
        if (keepsSelection) {
            comboBox.setValue(previousSelection);
        } else {
            comboBox.setValue(FILTER_ALL);
        }
    }

    private void openReportReview(Project project, Intern intern) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/View/GUIEvaluateReport.fxml"));
            Parent view = loader.load();

            EvaluateReportController controller = loader.getController();
            controller.setReviewContext(currentExperience, project, intern);

            Pane contentPane = findContentPane(internsTable);
            if (contentPane == null) {
                showAlert("Error de navegación",
                        "No se pudo abrir la vista de reportes.", Alert.AlertType.ERROR);
            } else {
                contentPane.getChildren().setAll(wrapInScrollableContent(view));
            }
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error loading reports view: {0}",
                    ioException.getMessage());
            showAlert("Error de navegación",
                    "No se pudo abrir la vista de reportes.", Alert.AlertType.ERROR);
        }
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            Pane contentPane = findContentPane(internsTable);
            if (contentPane == null) {
                showAlert("Error de navegación",
                        "No se pudo regresar a la vista anterior.", Alert.AlertType.ERROR);
            } else {
                contentPane.getChildren().setAll(wrapInScrollableContent(view));
            }
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error returning to previous view: {0}",
                    ioException.getMessage());
            showAlert("Error de navegación",
                    "No se pudo regresar a la vista anterior.", Alert.AlertType.ERROR);
        }
    }
}
