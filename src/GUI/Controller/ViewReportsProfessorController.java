package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.InternDAO;
import Logic.DAO.ProjectDAO;
import Logic.DAO.ReportDAO;
import Logic.DTOs.Intern;
import Logic.DTOs.Project;
import Logic.DTOs.Report;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;

public class ViewReportsProfessorController implements ChangeListener<Object> {
    private static final String STATUS_APPROVED = "Aprobado";
    private static final String STATUS_PENDING = "Pendiente";
    private static final String STATUS_REJECTED = "Rechazado";
    private static final String STATUS_SUBMITTED = "Entregado";


    private static final Logger LOGGER =
            Logger.getLogger(ViewReportsProfessorController.class.getName());

    @FXML
    private ComboBox<Project> projectComboBox;

    @FXML
    private ComboBox<Intern> internComboBox;

    @FXML
    private ComboBox<String> filterStatusComboBox;

    @FXML
    private TableView<Report> reportsTableView;

    @FXML
    private TableColumn<Report, String> idColumn;

    @FXML
    private TableColumn<Report, String> typeColumn;

    @FXML
    private TableColumn<Report, String> periodColumn;

    @FXML
    private TableColumn<Report, String> hoursColumn;

    @FXML
    private TableColumn<Report, String> statusColumn;

    @FXML
    private TableColumn<Report, String> dateColumn;

    @FXML
    private Label totalLabel;

    @FXML
    private Label detailLabel;

    private ObservableList<Report> allReports = FXCollections.observableArrayList();
    private int currentProfessorId;

    @FXML
    private void initialize() {
        currentProfessorId = SessionManager.getInstance().getUsuario().getId();
        configureTableColumns();
        configureListeners();
        filterStatusComboBox.getItems().setAll(
                "Todos", STATUS_PENDING, STATUS_SUBMITTED, STATUS_APPROVED, STATUS_REJECTED,
                "Entrega tardía", "En prórroga");
        filterStatusComboBox.setValue("Todos");
        loadProjects();
    }

    @FXML
    public void refreshReports(ActionEvent actionEvent) {
        Intern selectedIntern = internComboBox.getValue();
        boolean hasIntern = selectedIntern != null;
        if (hasIntern) {
            loadReportsForIntern(selectedIntern);
        }
    }

    @FXML
    public void clearFilter(ActionEvent actionEvent) {
        filterStatusComboBox.setValue("Todos");
        reportsTableView.setItems(allReports);
        String totalText = "Total: " + allReports.size();
        totalLabel.setText(totalText);
    }

    @FXML
    private void handleStatusFilter(ActionEvent actionEvent) {
        applyFilter();
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idReportDisplay"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("period"));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("reportedHoursDisplay"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("displayStatus"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateDisplay"));
    }

    private void configureListeners() {
        projectComboBox.getSelectionModel().selectedItemProperty().addListener(this);
        internComboBox.getSelectionModel().selectedItemProperty().addListener(this);
        reportsTableView.getSelectionModel().selectedItemProperty().addListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends Object> observable,
                        Object oldValue, Object newValue) {
        if (newValue != null) {
            if (observable == projectComboBox.getSelectionModel().selectedItemProperty()) {
                loadInternsForProject((Project) newValue);
            } else if (observable == internComboBox.getSelectionModel().selectedItemProperty()) {
                loadReportsForIntern((Intern) newValue);
            } else if (observable == reportsTableView.getSelectionModel().selectedItemProperty()) {
                showDetail((Report) newValue);
            }
        }
    }

    private void loadProjects() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> allProjects = projectDAO.findAll();
            List<Project> professorProjects = new ArrayList<>();

            for (Project project : allProjects) {
                if (project.getIdProfessor() == currentProfessorId) {
                    professorProjects.add(project);
                }
            }

            projectComboBox.setItems(FXCollections.observableArrayList(professorProjects));
            internComboBox.setDisable(true);

        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar proyectos del profesor {0}: {1}",
                    new Object[]{currentProfessorId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los proyectos. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadInternsForProject(Project project) {
        try {
            InternDAO internDAO = new InternDAO();
            List<Intern> interns = internDAO.findByProject(project.getIdProyect());
            internComboBox.setItems(FXCollections.observableArrayList(interns));
            internComboBox.setDisable(false);
            allReports.clear();
            reportsTableView.getItems().clear();
            clearDetail();
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar practicantes del proyecto {0}: {1}",
                    new Object[]{project.getIdProyect(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los practicantes. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void loadReportsForIntern(Intern intern) {
        try {
            ReportDAO reportDAO = new ReportDAO();
            List<Report> reports = reportDAO.getByInternAndProfessor(
                    intern.getId(), currentProfessorId);
            allReports.setAll(reports);
            applyFilter();
            clearDetail();
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar reportes del practicante {0}: {1}",
                    new Object[]{intern.getId(), serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudieron cargar los reportes. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private void applyFilter() {
        String filter = filterStatusComboBox.getValue();
        boolean isNoFilter = filter == null || "Todos".equals(filter);
        boolean isLateFilter = "Entrega tardía".equals(filter);

        if (isNoFilter) {
            reportsTableView.setItems(allReports);
        } else if (isLateFilter) {
            List<Report> filteredReports = new ArrayList<>();
            for (Report reportItem : allReports) {
                if (reportItem.isEntregaTardia()) {
                    filteredReports.add(reportItem);
                }
            }
            reportsTableView.setItems(FXCollections.observableArrayList(filteredReports));
        } else {
            List<Report> filteredReports = new ArrayList<>();
            for (Report reportItem : allReports) {
                if (filter.equals(reportItem.getStatus())) {
                    filteredReports.add(reportItem);
                }
            }
            reportsTableView.setItems(FXCollections.observableArrayList(filteredReports));
        }

        String totalText = "Total: " + reportsTableView.getItems().size();
        totalLabel.setText(totalText);
    }

    private void showDetail(Report report) {
        String observations = "";
        boolean hasObservations = report.getProfessorObservations() != null;
        if (hasObservations) {
            observations = "  |  Obs: " + report.getProfessorObservations();
        }
        String tardyIndicator = "";
        boolean isTardy = report.isEntregaTardia();
        if (isTardy) {
            tardyIndicator = "  ENTREGA TARDÍA";
        }
        String detailText = "ID: " + report.getIdReport()
                + "  |  Tipo: " + report.getReportType()
                + "  |  Período: " + report.getPeriod()
                + "  |  Horas: " + report.getReportedHours()
                + "  |  Estado: " + report.getStatus()
                + tardyIndicator
                + observations;
        detailLabel.setText(detailText);
    }

    private void clearDetail() {
        detailLabel.setText("");
        reportsTableView.getSelectionModel().clearSelection();
    }


}
