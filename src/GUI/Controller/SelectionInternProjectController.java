package GUI.Controller;

import Logic.DAO.InternDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.EducationalExperience;
import Logic.DTOs.Intern;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ViewsUtils.findContentPane;
import static GUI.Utils.ViewsUtils.wrapInScrollableContent;

public class SelectionInternProjectController
        implements Callback<CellDataFeatures<Intern, String>, ObservableValue<String>> {

    private static final Logger LOGGER =
            Logger.getLogger(SelectionInternProjectController.class.getName());

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

    @FXML
    private void initialize() {
        projectColumn.setCellValueFactory(this);
    }

    @Override
    public ObservableValue<String> call(CellDataFeatures<Intern, String> cellData) {
        Intern intern = cellData.getValue();
        Project project = internProjectMap.get(intern.getId());
        String projectName = "";
        if (project != null) {
            projectName = project.getName();
        }
        return new ReadOnlyStringWrapper(projectName);
    }

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
            Project project = internProjectMap.get(selectedIntern.getId());
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

            List<Intern> allInterns = new ArrayList<>();
            internProjectMap.clear();
            for (Project project : projects) {
                List<Intern> interns = internDAO.findByProject(project.getIdProject());
                for (Intern intern : interns) {
                    internProjectMap.put(intern.getId(), project);
                    allInterns.add(intern);
                }
            }

            internsTable.setItems(FXCollections.observableArrayList(allInterns));
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error al cargar practicantes de la experiencia educativa {0}: {1}",
                    new Object[]{experience.getNrc(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los practicantes. Intente más tarde.",
                    Alert.AlertType.ERROR);
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
            LOGGER.log(Level.SEVERE, "Error al cargar la vista de reportes: {0}",
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
            LOGGER.log(Level.SEVERE, "Error al regresar a la vista anterior: {0}",
                    ioException.getMessage());
            showAlert("Error de navegación",
                    "No se pudo regresar a la vista anterior.", Alert.AlertType.ERROR);
        }
    }
}
