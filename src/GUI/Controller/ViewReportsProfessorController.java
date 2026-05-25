package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ReportDAO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;

public class ViewReportsProfessorController {

    private static final Logger LOGGER =
            Logger.getLogger(ViewReportsProfessorController.class.getName());

    @FXML
    private TableView<Report> reportsTableView;

    @FXML
    private TableColumn<Report, Integer> idColumn;

    @FXML
    private TableColumn<Report, String> typeColumn;

    @FXML
    private TableColumn<Report, String> periodColumn;

    @FXML
    private TableColumn<Report, Integer> hoursColumn;

    @FXML
    private TableColumn<Report, String> statusColumn;

    @FXML
    private TableColumn<Report, String> dateColumn;

    @FXML
    private ComboBox<String> filterStatusComboBox;

    @FXML
    private Label totalLabel;

    @FXML
    private Label detailLabel;

    private ObservableList<Report> allReports = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configureListeners();
        filterStatusComboBox.getItems().setAll(
                "Todos", "Pendiente", "Entregado", "Aprobado", "Rechazado",
                "Entrega tardía", "En prórroga");
        filterStatusComboBox.setValue("Todos");
        loadReports();
    }

    @FXML
    public void refreshReports(ActionEvent actionEvent) {
        loadReports();
    }

    @FXML
    public void clearFilter(ActionEvent actionEvent) {
        filterStatusComboBox.setValue("Todos");
        reportsTableView.setItems(allReports);
        String clearedTotalText = "Total: " + allReports.size();
        totalLabel.setText(clearedTotalText);
    }

    @FXML
    private void handleStatusFilter(ActionEvent actionEvent) {
        applyFilter();
    }

    private void configureListeners() {
        reportsTableView.getSelectionModel().selectedItemProperty()
                .addListener(new ReportSelectionListener());
    }

    private final class ReportSelectionListener implements ChangeListener<Report> {
        @Override
        public void changed(ObservableValue<? extends Report> observable,
                            Report oldValue, Report newValue) {
            if (newValue != null) {
                showDetail(newValue);
            }
        }
    }

    private void loadReports() {
        try {
            int professorId = SessionManager.getInstance().getUsuario().getId();
            ReportDAO reportDAO = new ReportDAO();
            List<Report> reports = reportDAO.getByIdProfessor(professorId);
            allReports.setAll(reports);
            applyFilter();
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar reportes del profesor: {0}",
                    serviceException.getMessage());
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

        String filteredTotalText = "Total: " + reportsTableView.getItems().size();
        totalLabel.setText(filteredTotalText);
    }

    private void showDetail(Report report) {
        String observations = "";
        if (report.getProfessorObservations() != null) {
            observations = "  |  Obs: " + report.getProfessorObservations();
        }
        String detailText = "ID: " + report.getIdReport()
                + "  |  Practicante ID: " + report.getIdIntern()
                + "  |  Proyecto ID: " + report.getIdProyect()
                + "  |  Tipo: " + report.getReportType()
                + "  |  Horas: " + report.getReportedHours()
                + "  |  Estado: " + report.getStatus()
                + observations;
        detailLabel.setText(detailText);
    }

}
