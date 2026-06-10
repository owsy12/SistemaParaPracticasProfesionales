package GUI.Controller;

import GUI.SessionManager.SessionManager;
import GUI.Utils.AuditLog;
import GUI.DocumentGeneration.FinalReportGenerator;
import GUI.DocumentGeneration.InternContext;
import GUI.DocumentGeneration.InternContextLoader;
import GUI.DocumentGeneration.MonthlyReportGenerator;
import GUI.DocumentGeneration.PartialReportGenerator;
import GUI.DocumentGeneration.ReportContent;
import GUI.DocumentGeneration.ReportGenerationContext;
import GUI.DocumentGeneration.ReportGenerationContextBuilder;
import Logic.DAO.MonthlyReportDAO;
import Logic.DAO.PartialAndFinalReportDAO;
import Logic.DAO.ReportActivityDAO;
import Logic.DAO.ReportDAO;
import Logic.DTOs.Activity;
import Logic.DTOs.Intern;
import Logic.DTOs.LinkedOrganization;
import Logic.DTOs.MonthlyReport;
import Logic.DTOs.PartialAndFinalReport;
import Logic.DTOs.Professor;
import Logic.DTOs.Project;
import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportDeliverable;
import Logic.DTOs.TechnicalSupervisor;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import GUI.Utils.RestrictedTextArea;
import GUI.Utils.RestrictedTextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.applyTextAreaRestriction;
import static GUI.Utils.ValidationUtils.applyTextFieldRestriction;
import static GUI.Utils.ValidationUtils.setTypeAndLength;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class GenerateReportController {
    private static final String STATUS_PENDING = "Pendiente";


    private static final Logger LOGGER = Logger.getLogger(GenerateReportController.class.getName());

    private static final int PARTIAL_MIN_HOURS = 210;
    private static final int FINAL_MIN_HOURS = 420;
    private static final int SPRING_FIRST_MONTH = 2;
    private static final int SPRING_LAST_MONTH = 7;
    private static final int JANUARY_MONTH_NUMBER = 1;
    private static final String SPRING_PERIOD_LABEL = "Febrero - Julio ";
    private static final String FALL_PERIOD_TEMPLATE = "Agosto %d - Enero %d";
    private static final String REPORT_TYPE_MONTHLY = "Mensual";
    private static final String REPORT_TYPE_PARTIAL = "Parcial";
    private static final String REPORT_TYPE_FINAL = "Final";
    private static final List<String> MONTHS = Arrays.asList("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Label internNameLabel;

    @FXML
    private Label projectLabel;

    @FXML
    private Label approvedHoursLabel;

    @FXML
    private ComboBox<String> reportTypeComboBox;

    @FXML
    private ComboBox<String> monthComboBox;

    @FXML
    private RestrictedTextField reportedHoursTextField;

    @FXML
    private RestrictedTextField reportNumberTextField;

    @FXML
    private RestrictedTextArea methodologyTextArea;

    @FXML
    private RestrictedTextArea resultsTextArea;

    @FXML
    private RestrictedTextArea observationsTextArea;

    @FXML
    private Button generateButton;

    @FXML
    private TableView<Activity> projectActivitiesTable;

    @FXML
    private TableColumn<Activity, String> columnActName;

    @FXML
    private TableColumn<Activity, String> columnActDesc;

    @FXML
    private TableColumn<Activity, String> columnActPlanStart;

    @FXML
    private TableColumn<Activity, String> columnActPlanEnd;

    @FXML
    private TableView<ReportActivity> reportActivitiesTable;

    @FXML
    private TableColumn<ReportActivity, String> columnRaName;

    @FXML
    private TableColumn<ReportActivity, String> columnRangePeriod;

    @FXML
    private TableColumn<ReportActivity, String> columnRangeDetail;

    @FXML
    private Label deliverablesLabel;

    @FXML
    private Button addDeliverableButton;

    @FXML
    private Button removeDeliverableButton;

    @FXML
    private TableView<ReportDeliverable> reportDeliverablesTable;

    @FXML
    private TableColumn<ReportDeliverable, String> columnRreportdDeliverableResult;

    @FXML
    private TableColumn<ReportDeliverable, String> columnReportDeliverableDesc;

    @FXML
    private TableColumn<ReportDeliverable, String> columnReportdDeliverableAdvance;

    @FXML
    private TableColumn<ReportDeliverable, String> columnReportDeliverableObseervations;

    private Intern currentIntern;
    private Project currentProject;
    private LinkedOrganization currentOrganization;
    private TechnicalSupervisor currentSupervisor;
    private Professor currentProfessor;
    private int approvedHours;

    private final ObservableList<Activity> projectActivities = FXCollections.observableArrayList();
    private final ObservableList<ReportActivity> reportActivities = FXCollections.observableArrayList();
    private final ObservableList<ReportDeliverable> reportDeliverables = FXCollections.observableArrayList();
    private List<Activity> allProjectActivities = new ArrayList<>();

    @FXML
    private void initialize() {
        setTypeAndLength(reportedHoursTextField, "Number");
        setTypeAndLength(reportNumberTextField, "Number");
        applyTextAreaRestriction(methodologyTextArea, 300);
        applyTextAreaRestriction(resultsTextArea, 300);
        applyTextAreaRestriction(observationsTextArea, 200);

        reportTypeComboBox.getItems().setAll(REPORT_TYPE_MONTHLY, REPORT_TYPE_PARTIAL, REPORT_TYPE_FINAL);

        projectActivitiesTable.setItems(projectActivities);
        reportActivitiesTable.setItems(reportActivities);
        reportDeliverablesTable.setItems(reportDeliverables);

        loadInternContext();
    }

    private void handleContextLoaded(InternContext context) {
        boolean hasAssignment = context.hasAssignment();
        if (hasAssignment) {
            currentIntern = context.getIntern();
            currentProject = context.getProject();
            currentOrganization = context.getOrganization();
            currentSupervisor = context.getSupervisor();
            currentProfessor = context.getProfessor();
            approvedHours = context.getApprovedHours();
            allProjectActivities = context.getActivities();
            populateReadOnlyInfo();
            filterMonthsToProjectPeriod();
            applyActivityFilter();
        } else {
            showAlert("Sin proyecto asignado", "No tiene un proyecto activo. No puede generar reportes.",
                    Alert.AlertType.WARNING);
            disableGeneration();
        }
    }

    private void handleContextLoadFailed(Exception loadException) {
        LOGGER.log(Level.SEVERE, "Error al cargar contexto del practicante: {0}",
                loadException.getMessage());
        showAlert("Servicio no disponible",
                "No se pudo cargar su información. Intente más tarde.",
                Alert.AlertType.ERROR);
        disableGeneration();
    }

    @FXML
    public void addActivityToReport(ActionEvent actionEvent) {
        Activity selected = projectActivitiesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selección requerida", "Seleccione una actividad del proyecto.",
                    Alert.AlertType.WARNING);
        } else {
            tryAddActivity(selected);
        }
    }

    @FXML
    public void removeActivityFromReport(ActionEvent actionEvent) {
        ReportActivity selected = reportActivitiesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            reportActivities.remove(selected);
        }
    }

    @FXML
    public void addDeliverableToReport(ActionEvent actionEvent) {
        Optional<ReportDeliverable> result = showDeliverableDialog();
        if (result.isPresent()) {
            reportDeliverables.add(result.get());
        }
    }

    @FXML
    public void removeDeliverableFromReport(ActionEvent actionEvent) {
        ReportDeliverable selected = reportDeliverablesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            reportDeliverables.remove(selected);
        }
    }

    @FXML
    public void generateReport(ActionEvent actionEvent) {
        String reportType = reportTypeComboBox.getValue();
        boolean isReportTypeMissing = reportType == null;
        boolean isActivitiesEmpty = reportActivities.isEmpty();

        if (isReportTypeMissing) {
            showAlert("Tipo de reporte", "Seleccione el tipo de reporte.",
                    Alert.AlertType.WARNING);
        } else if (isActivitiesEmpty) {
            showAlert("Actividades requeridas", "Agregue al menos una actividad al reporte.",
                    Alert.AlertType.WARNING);
        } else {
            tryProcessGeneration(reportType);
        }
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        clearForm();
    }

    @FXML
    private void handleReportTypeChange(ActionEvent actionEvent) {
        applyReportTypeVisibility();
        applyActivityFilter();
    }

    @FXML
    private void handleMonthChange(ActionEvent actionEvent) {
        applyActivityFilter();
    }

    private void loadInternContext() {
        if (SessionManager.getInstance().getUser() == null) {
            disableGeneration();
        } else {
            tryLoadInternData();
        }
    }

    private void tryLoadInternData() {
        int internId = SessionManager.getInstance().getUser().getId();
        try {
            InternContext context = InternContextLoader.load(internId);
            handleContextLoaded(context);
        } catch (ValidationException | ServiceException loadException) {
            handleContextLoadFailed(loadException);
        }
    }

    private void tryAddActivity(Activity selected) {
        String reportType = reportTypeComboBox.getValue();
        boolean isReportTypeMissing = reportType == null;
        boolean isAlreadyAdded = isActivityAlreadyAdded(selected);
        boolean isOutsideProjectRange = isActivityOutsideProjectRange(selected);

        if (isReportTypeMissing) {
            showAlert("Tipo de reporte", "Seleccione primero el tipo de reporte.",
                    Alert.AlertType.WARNING);
        } else if (isAlreadyAdded) {
            showAlert("Actividad duplicada", "Esta actividad ya fue agregada al reporte.",
                    Alert.AlertType.WARNING);
        } else if (isOutsideProjectRange) {
            showAlert("Actividad fuera del rango del proyecto",
                    "Las fechas de esta actividad están fuera del período del proyecto.",
                    Alert.AlertType.WARNING);
        } else {
            Optional<ReportActivity> result = showActivityProgressDialog(selected, reportType);
            if (result.isPresent()) {
                reportActivities.add(result.get());
            }
        }
    }

    private boolean isActivityAlreadyAdded(Activity activity) {
        boolean alreadyAdded = false;
        for (ReportActivity reportActivity : reportActivities) {
            if (reportActivity.getIdActivity() == activity.getIdActivity()) {
                alreadyAdded = true;
            }
        }
        return alreadyAdded;
    }

    private boolean isActivityOutsideProjectRange(Activity activity) {
        boolean hasProjectStart = currentProject != null && currentProject.getStartDate() != null;
        boolean hasProjectEnd = currentProject != null && currentProject.getEndDate() != null;

        boolean startBeforeProject = hasProjectStart && activity.getStartDate() != null
                && activity.getStartDate().isBefore(currentProject.getStartDate());
        boolean endAfterProject = hasProjectEnd && activity.getEndDate() != null
                && activity.getEndDate().isAfter(currentProject.getEndDate());

        boolean isOutOfRange = startBeforeProject || endAfterProject;
        return isOutOfRange;
    }

    private void tryProcessGeneration(String reportType) {
        try {
            boolean rulesValid = validateBusinessRules(reportType);
            if (rulesValid) {
                processGeneration(reportType);
            }
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al verificar reglas de negocio: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudo verificar las condiciones para generar el reporte.",
                    Alert.AlertType.ERROR);
        }
    }

    private void applyReportTypeVisibility() {
        String type = reportTypeComboBox.getValue();
        if (type != null) {
            boolean isMonthly = REPORT_TYPE_MONTHLY.equals(type);
            boolean isFinal = REPORT_TYPE_FINAL.equals(type);
            boolean isPartialOrFinal = !isMonthly;

            monthComboBox.setVisible(isMonthly);
            reportedHoursTextField.setVisible(isMonthly);
            reportNumberTextField.setVisible(true);
            methodologyTextArea.setVisible(isPartialOrFinal);
            resultsTextArea.setVisible(isPartialOrFinal);
            observationsTextArea.setVisible(isPartialOrFinal);

            deliverablesLabel.setVisible(isFinal);
            reportDeliverablesTable.setVisible(isFinal);
            addDeliverableButton.setVisible(isFinal);
            removeDeliverableButton.setVisible(isFinal);
        }
    }

    private void filterMonthsToProjectPeriod() {
        LocalDate projectStart = currentProject.getStartDate();
        LocalDate projectEnd = currentProject.getEndDate();
        boolean hasValidRange = projectStart != null && projectEnd != null;
        if (hasValidRange) {
            List<String> validMonths = new ArrayList<>();
            YearMonth startYearMonth = YearMonth.from(projectStart);
            YearMonth endYearMonth = YearMonth.from(projectEnd);
            YearMonth currentYearMonth = startYearMonth;
            while (!currentYearMonth.isAfter(endYearMonth)) {
                String monthName = MONTHS.get(currentYearMonth.getMonthValue() - 1);
                boolean isNotDuplicate = !validMonths.contains(monthName);
                if (isNotDuplicate) {
                    validMonths.add(monthName);
                }
                currentYearMonth = currentYearMonth.plusMonths(1);
            }
            monthComboBox.getItems().setAll(validMonths);
        } else {
            monthComboBox.getItems().setAll(MONTHS);
        }
    }

    private void applyActivityFilter() {
        String reportType = reportTypeComboBox.getValue();
        boolean isMonthly = REPORT_TYPE_MONTHLY.equals(reportType);
        boolean isPartial = REPORT_TYPE_PARTIAL.equals(reportType);

        if (isMonthly) {
            applyMonthlyFilter();
        } else if (isPartial) {
            applyPartialFilter();
        } else {
            projectActivities.setAll(allProjectActivities);
        }
    }

    private void applyMonthlyFilter() {
        String selectedMonth = monthComboBox.getValue();
        boolean isMonthSelected = selectedMonth != null;

        if (!isMonthSelected) {
            projectActivities.setAll(allProjectActivities);
        } else {
            int monthNumber = MONTHS.indexOf(selectedMonth) + 1;
            int currentYear = LocalDate.now().getYear();
            YearMonth yearMonth = YearMonth.of(currentYear, monthNumber);
            LocalDate firstDayOfMonth = yearMonth.atDay(1);
            LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

            List<Activity> dateFiltered = new ArrayList<>();
            for (Activity activity : allProjectActivities) {
                boolean isStartBeforeMonthEnd = activity.getStartDate() == null
                        || !activity.getStartDate().isAfter(lastDayOfMonth);
                boolean isEndAfterMonthStart = activity.getEndDate() == null
                        || !activity.getEndDate().isBefore(firstDayOfMonth);
                boolean isInRange = isStartBeforeMonthEnd && isEndAfterMonthStart;
                if (isInRange) {
                    dateFiltered.add(activity);
                }
            }

            List<Activity> notYetUsed = filterOutUsedActivities(dateFiltered);
            projectActivities.setAll(notYetUsed);
        }
    }

    private List<Activity> filterOutUsedActivities(List<Activity> candidates) {
        List<Activity> result = new ArrayList<>(candidates);
        boolean hasIntern = currentIntern != null;
        if (hasIntern) {
            try {
                ReportActivityDAO reportActivityDAO = new ReportActivityDAO();
                List<Integer> usedIds = reportActivityDAO.findActivityIdsInMonthlyReportsByIntern(currentIntern.getId());
                List<Activity> unused = new ArrayList<>();
                for (Activity activity : candidates) {
                    boolean isAlreadyUsed = usedIds.contains(activity.getIdActivity());
                    if (!isAlreadyUsed) {
                        unused.add(activity);
                    }
                }
                result = unused;
            } catch (ValidationException | ServiceException persistenceException) {
                LOGGER.log(Level.WARNING, "No se pudieron filtrar actividades ya usadas en reportes mensuales: {0}",
                        persistenceException.getMessage());
            }
        }
        return result;
    }

    private void applyPartialFilter() {
        if (currentIntern == null) {
            projectActivities.setAll(allProjectActivities);
        } else {
            try {
                ReportActivityDAO reportActivityDAO = new ReportActivityDAO();
                List<Integer> usedActivityIds =
                        reportActivityDAO.findActivityIdsInMonthlyReportsByIntern(currentIntern.getId());

                boolean hasMonthlyReports = !usedActivityIds.isEmpty();
                if (!hasMonthlyReports) {
                    projectActivities.clear();
                } else {
                    List<Activity> filteredActivities = new ArrayList<>();
                    for (Activity activity : allProjectActivities) {
                        boolean isUsedInMonthly = usedActivityIds.contains(activity.getIdActivity());
                        if (isUsedInMonthly) {
                            filteredActivities.add(activity);
                        }
                    }
                    projectActivities.setAll(filteredActivities);
                }
            } catch (ValidationException | ServiceException persistenceException) {
                LOGGER.log(Level.WARNING,
                        "No se pudieron cargar las actividades de reportes mensuales: {0}",
                        persistenceException.getMessage());
                projectActivities.setAll(allProjectActivities);
            }
        }
    }

    private void populateReadOnlyInfo() {
        String internNameText = "Practicante: " + buildFullName(currentIntern);
        String projectNameText = "Proyecto: " + currentProject.getName();
        String approvedHoursText = "Horas validadas: " + approvedHours;
        internNameLabel.setText(internNameText);
        projectLabel.setText(projectNameText);
        approvedHoursLabel.setText(approvedHoursText);
    }

    private Optional<ReportActivity> showActivityProgressDialog(Activity activity, String reportType) {
        Optional<ReportActivity> dialogResult;
        if (REPORT_TYPE_MONTHLY.equals(reportType)) {
            dialogResult = showMonthlyActivityDialog(activity);
        } else if (REPORT_TYPE_PARTIAL.equals(reportType)) {
            dialogResult = showPartialActivityDialog(activity);
        } else {
            dialogResult = showFinalActivityDialog(activity);
        }
        return dialogResult;
    }

    private Optional<ReportActivity> showMonthlyActivityDialog(Activity activity) {
        Dialog<ButtonType> dialog = createActivityDialog(activity.getName());
        ButtonType confirmType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        GridPane grid = buildDialogGrid();
        RestrictedTextField periodField = new RestrictedTextField();
        periodField.setPromptText("Ej: Semana 1-3");
        applyTextFieldRestriction(periodField, 50);
        RestrictedTextArea observationsField = new RestrictedTextArea();
        observationsField.setPromptText("Observaciones de la actividad");
        observationsField.setPrefRowCount(3);
        applyTextAreaRestriction(observationsField, 150);

        grid.add(new Label("Período de ejecución:"), 0, 0);
        grid.add(periodField, 1, 0);
        grid.add(new Label("Observaciones:"), 0, 1);
        grid.add(observationsField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> dialogResult = dialog.showAndWait();
        boolean isConfirmed = dialogResult.isPresent() && dialogResult.get() == confirmType;

        Optional<ReportActivity> result = Optional.empty();
        if (isConfirmed) {
            result = Optional.of(
                    buildMonthlyActivity(activity,
                            periodField.getText().trim(),
                            observationsField.getText().trim()));
        }
        return result;
    }

    private ReportActivity buildMonthlyActivity(Activity activity, String periodo, String observaciones) {
        ReportActivity reportActivity = createReportActivity(activity);
        reportActivity.setPeriod(periodo);
        reportActivity.setObservation(observaciones);
        return reportActivity;
    }

    private Optional<ReportActivity> showPartialActivityDialog(Activity activity) {
        Dialog<ButtonType> dialog = createActivityDialog(activity.getName());
        ButtonType confirmType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        String fechaInicioText = "—";
        if (activity.getStartDate() != null) {
            fechaInicioText = activity.getStartDate().toString();
        }
        String fechaFinText = "—";
        if (activity.getEndDate() != null) {
            fechaFinText = activity.getEndDate().toString();
        }

        GridPane grid = buildDialogGrid();
        RestrictedTextField realStartWeekField = new RestrictedTextField("1");
        setTypeAndLength(realStartWeekField, "Number");
        RestrictedTextField realEndWeekField = new RestrictedTextField("8");
        setTypeAndLength(realEndWeekField, "Number");
        RestrictedTextArea observationsField = new RestrictedTextArea();
        observationsField.setPromptText("Observaciones");
        observationsField.setPrefRowCount(2);
        applyTextAreaRestriction(observationsField, 150);

        grid.add(new Label("Fecha inicio planificada:"), 0, 0);
        grid.add(new Label(fechaInicioText), 1, 0);
        grid.add(new Label("Fecha fin planificada:"), 0, 1);
        grid.add(new Label(fechaFinText), 1, 1);
        grid.add(new Label("Real desde semana:"), 0, 2);
        grid.add(realStartWeekField, 1, 2);
        grid.add(new Label("Real hasta semana:"), 0, 3);
        grid.add(realEndWeekField, 1, 3);
        grid.add(new Label("Observaciones:"), 0, 4);
        grid.add(observationsField, 1, 4);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> dialogResult = dialog.showAndWait();
        boolean isConfirmed = dialogResult.isPresent() && dialogResult.get() == confirmType;

        Optional<ReportActivity> reportActivity = Optional.empty();
        if (isConfirmed) {
            String realWeeks = realStartWeekField.getText().trim()
                    + ":" + realEndWeekField.getText().trim();
            reportActivity = Optional.of(buildPartialActivity(activity, realWeeks, observationsField.getText().trim()));
        }
        return reportActivity;
    }

    private ReportActivity buildPartialActivity(Activity activity, String realWeeks,
                                                 String observaciones) {
        ReportActivity reportActivity = createReportActivity(activity);
        reportActivity.setWeeklyPlan("1:8");
        reportActivity.setRealWeeks(realWeeks);
        reportActivity.setObservation(observaciones);
        return reportActivity;
    }

    private Optional<ReportActivity> showFinalActivityDialog(Activity activity) {
        Dialog<ButtonType> dialog = createActivityDialog(activity.getName());
        ButtonType confirmType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        GridPane grid = buildDialogGrid();
        RestrictedTextField advancePercentField = new RestrictedTextField("0");
        advancePercentField.setPromptText("Porcentaje 0-100");
        setTypeAndLength(advancePercentField, "Number");
        RestrictedTextArea observationsField = new RestrictedTextArea();
        observationsField.setPromptText("Observaciones");
        observationsField.setPrefRowCount(3);
        applyTextAreaRestriction(observationsField, 150);

        grid.add(new Label("% de avance:"), 0, 0);
        grid.add(advancePercentField, 1, 0);
        grid.add(new Label("Observaciones:"), 0, 1);
        grid.add(observationsField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> dialogResult = dialog.showAndWait();
        boolean isConfirmed = dialogResult.isPresent() && dialogResult.get() == confirmType;

        Optional<ReportActivity> reportActivity = Optional.empty();
        if (isConfirmed) {
            int advance = parseIntSafe(advancePercentField.getText().trim());
            reportActivity = Optional.of (buildFinalActivity(activity, advance, observationsField.getText().trim()));
        }
        return reportActivity;
    }

    private ReportActivity buildFinalActivity(Activity activity, int advance, String observaciones) {
        ReportActivity reportActivity = createReportActivity(activity);
        reportActivity.setAdvancePercentage(advance);
        reportActivity.setObservation(observaciones);
        return reportActivity;
    }

    private Dialog<ButtonType> createActivityDialog(String activityName) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Progreso de actividad");
        dialog.setHeaderText("Actividad: " + activityName);
        return dialog;
    }

    private ReportActivity createReportActivity(Activity activity) {
        ReportActivity reportActivity = new ReportActivity();
        reportActivity.setIdActivity(activity.getIdActivity());
        reportActivity.setActivityName(activity.getName());
        return reportActivity;
    }

    private GridPane buildDialogGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }

    private Optional<ReportDeliverable> showDeliverableDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Agregar entregable");
        dialog.setHeaderText("Nuevo entregable del reporte final");

        ButtonType confirmType = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        GridPane grid = buildDialogGrid();

        RestrictedTextField resultadoField = new RestrictedTextField();
        resultadoField.setPromptText("Resultado entregable");
        applyTextFieldRestriction(resultadoField, 100);
        RestrictedTextArea descripcionField = new RestrictedTextArea();
        descripcionField.setPromptText("Descripción");
        descripcionField.setPrefRowCount(2);
        applyTextAreaRestriction(descripcionField, 150);
        RestrictedTextField advancePercentField = new RestrictedTextField("0");
        advancePercentField.setPromptText("Porcentaje 0-100");
        setTypeAndLength(advancePercentField, "Number");
        RestrictedTextArea observationsField = new RestrictedTextArea();
        observationsField.setPromptText("Observaciones");
        observationsField.setPrefRowCount(2);
        applyTextAreaRestriction(observationsField, 150);

        grid.add(new Label("Resultado entregable:"), 0, 0);
        grid.add(resultadoField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descripcionField, 1, 1);
        grid.add(new Label("% de avance:"), 0, 2);
        grid.add(advancePercentField, 1, 2);
        grid.add(new Label("Observaciones:"), 0, 3);
        grid.add(observationsField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> dialogResult = dialog.showAndWait();
        boolean isConfirmed = dialogResult.isPresent() && dialogResult.get() == confirmType;
        boolean isResultadoNotBlank = !resultadoField.getText().isBlank();

        Optional<ReportDeliverable> result = Optional.empty();
        if (isConfirmed && isResultadoNotBlank) {
            ReportDeliverable deliverable = new ReportDeliverable();
            deliverable.setResultado(resultadoField.getText().trim());
            deliverable.setDescripcion(descripcionField.getText().trim());
            deliverable.setAdvancePercentage(parseIntSafe(advancePercentField.getText().trim()));
            deliverable.setObservaciones(observationsField.getText().trim());
            result = Optional.of(deliverable);
        }
        return result;
    }

    private boolean validateBusinessRules(String reportType) throws ServiceException {
        int internId = currentIntern.getId();
        int projectId = currentProject.getIdProject();
        ReportDAO reportDAO = new ReportDAO();
        boolean isValid = true;

        if (REPORT_TYPE_MONTHLY.equals(reportType)) {
            isValid = validateMonthlyRules(internId, reportDAO);
        } else if (REPORT_TYPE_PARTIAL.equals(reportType)) {
            isValid = validatePartialRules(internId, projectId, reportDAO);
        } else if (REPORT_TYPE_FINAL.equals(reportType)) {
            isValid = validateFinalRules(internId, projectId, reportDAO);
        }

        return isValid;
    }

    private boolean validateMonthlyRules(int internId, ReportDAO reportDAO) throws ServiceException {
        boolean isValid = true;
        if (monthComboBox.getValue() == null) {
            showAlert("Mes requerido", "Seleccione el mes para el reporte mensual.",
                    Alert.AlertType.WARNING);
            isValid = false;
        } else {
            int year = LocalDate.now().getYear();
            boolean exists = reportDAO.existsMonthlyByInternAndPeriod(
                    internId, monthComboBox.getValue(), year);
            if (exists) {
                showAlert("Reporte duplicado", "Ya existe un reporte mensual para "
                        + monthComboBox.getValue() + " " + year + ".",
                        Alert.AlertType.WARNING);
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean validatePartialRules(int internId, int projectId, ReportDAO reportDAO) throws ServiceException {
        boolean isValid = true;
        if (approvedHours < PARTIAL_MIN_HOURS) {
            showAlert("Horas insuficientes", "Necesita al menos " + PARTIAL_MIN_HOURS
                    + " horas validadas. Actuales: " + approvedHours + ".",
                    Alert.AlertType.WARNING);
            isValid = false;
        } else if (reportDAO.existsPartialByInternAndProject(internId, projectId)) {
            showAlert("Reporte duplicado", "Ya existe un reporte parcial para este proyecto.",
                    Alert.AlertType.WARNING);
            isValid = false;
        }
        return isValid;
    }

    private boolean validateFinalRules(int internId, int projectId, ReportDAO reportDAO) throws ServiceException {
        boolean isValid = true;
        if (approvedHours < FINAL_MIN_HOURS) {
            showAlert("Horas insuficientes", "Necesita al menos " + FINAL_MIN_HOURS
                    + " horas validadas. Actuales: " + approvedHours + ".",
                    Alert.AlertType.WARNING);
            isValid = false;
        } else if (reportDAO.existsFinalByInternAndProject(internId, projectId)) {
            showAlert("Reporte duplicado",
                    "Ya existe un reporte final para este proyecto.",
                    Alert.AlertType.WARNING);
            isValid = false;
        }
        return isValid;
    }

    private void processGeneration(String reportType) {
        try {
            if (REPORT_TYPE_MONTHLY.equals(reportType)) {
                generateMonthlyProcess();
            } else {
                generatePartialFinalProcess(reportType);
            }
        } catch (ValidationException validationException) {
            showAlert("Error de validación", validationException.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al guardar el reporte: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudo guardar el reporte. Intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error al generar PDF: {0}", ioException.getMessage());
            showAlert("Error al generar PDF",
                    "El reporte se registró pero no se pudo crear el archivo PDF.",
                    Alert.AlertType.WARNING);
        }
    }

    private void generateMonthlyProcess() throws ValidationException, ServiceException, IOException {
        if (isMonthlyInputValid()) {
            executeMonthlyGeneration();
        }
    }

    private boolean isMonthlyInputValid() {
        boolean isValid = true;
        if (monthComboBox.getValue() == null) {
            showAlert("Mes requerido",
                    "Seleccione el mes del reporte.", Alert.AlertType.WARNING);
            isValid = false;
        } else if (reportNumberTextField.getText().isBlank()) {
            showAlert("Número de informe requerido",
                    "Ingrese el número de informe.", Alert.AlertType.WARNING);
            isValid = false;
        } else if (reportedHoursTextField.getText().isBlank()) {
            showAlert("Horas requeridas",
                    "Ingrese las horas reportadas en el mes.", Alert.AlertType.WARNING);
            isValid = false;
        }
        return isValid;
    }

    private void executeMonthlyGeneration() throws ValidationException, ServiceException, IOException {
        int year = LocalDate.now().getYear();
        String month = monthComboBox.getValue();
        int reportedHoursCount = Integer.parseInt(reportedHoursTextField.getText());
        int reportNumber = Integer.parseInt(reportNumberTextField.getText());
        String period = buildAcademicPeriod(year, month);
        int totalHours = approvedHours + reportedHoursCount;

        MonthlyReport report = new MonthlyReport();
        report.setIdIntern(currentIntern.getId());
        report.setIdProject(currentProject.getIdProject());
        report.setIdProfessor(currentProject.getIdProfessor());
        report.setReportType(REPORT_TYPE_MONTHLY);
        report.setPeriod(period);
        report.setStatus(STATUS_PENDING);
        report.setMonth(month);
        report.setYear(year);
        report.setMonthlyHours(reportedHoursCount);
        report.setReportedHours(reportedHoursCount);
        report.setReportNumber(reportNumber);
        report.setDocumentPath("");
        report.setSumissionDate(new Date());

        MonthlyReportDAO monthlyReportDAO = new MonthlyReportDAO();
        int rowsAffected = monthlyReportDAO.save(report);

        if (rowsAffected > 0) {
            persistReportActivities(report.getIdReport());

            ReportGenerationContext generationContext = buildCurrentContext(totalHours);
            ReportContent reportContent = new ReportContent(Collections.unmodifiableList(reportActivities), null);

            String internalPath = MonthlyReportGenerator.generate(report, generationContext, reportContent);

            monthlyReportDAO.updateDocumentPath(report.getIdReport(), internalPath);

            AuditLog.record("generó el reporte " + report.getReportType() + " del practicante " + report.getIdIntern());
            showAlert("Reporte generado", "Reporte mensual generado correctamente.",
                    Alert.AlertType.INFORMATION);
            clearForm();
        } else {
            showAlert("Error", "No se pudo guardar el reporte.", Alert.AlertType.ERROR);
        }
    }

    private void generatePartialFinalProcess(String reportType)
            throws ValidationException, ServiceException, IOException {
        if (isPartialFinalInputValid(reportType)) {
            executePartialFinalGeneration(reportType);
        }
    }

    private boolean isPartialFinalInputValid(String reportType) {
        boolean isReportNumberBlank = reportNumberTextField.getText().isBlank();
        boolean isMethodologyBlank = methodologyTextArea.getText().isBlank();
        boolean isFinal = REPORT_TYPE_FINAL.equals(reportType);
        boolean isObservationsBlank = isFinal && observationsTextArea.getText().isBlank();
        boolean isValid = true;

        boolean hasMissingBasicFields = isReportNumberBlank || isMethodologyBlank;
        if (hasMissingBasicFields) {
            showAlert("Campos requeridos",
                    "Complete el número de informe y la metodología.",
                    Alert.AlertType.WARNING);
            isValid = false;
        } else if (isObservationsBlank) {
            showAlert("Campos requeridos",
                    "Las observaciones son obligatorias para el reporte final.",
                    Alert.AlertType.WARNING);
            isValid = false;
        }

        return isValid;
    }

    private void executePartialFinalGeneration(String reportType)
            throws ValidationException, ServiceException, IOException {
        String period = buildAcademicPeriod(LocalDate.now().getYear(),
                MONTHS.get(LocalDate.now().getMonthValue() - 1));

        PartialAndFinalReport report = buildPartialFinalReport(reportType, period);

        PartialAndFinalReportDAO partialAndFinalReportDAO = new PartialAndFinalReportDAO();
        int rowsAffected = partialAndFinalReportDAO.save(report);

        if (rowsAffected > 0) {
            persistReportActivities(report.getIdReport());
            savePartialFinalDocument(report, reportType, partialAndFinalReportDAO);
        } else {
            showAlert("Error", "No se pudo guardar el reporte.", Alert.AlertType.ERROR);
        }
    }

    private PartialAndFinalReport buildPartialFinalReport(String reportType, String period) {
        PartialAndFinalReport report = new PartialAndFinalReport();
        report.setIdIntern(currentIntern.getId());
        report.setIdProject(currentProject.getIdProject());
        report.setIdProfessor(currentProject.getIdProfessor());
        report.setReportType(reportType);
        report.setPeriod(period);
        report.setStatus(STATUS_PENDING);
        report.setReportedHours(approvedHours);
        report.setDocumentPath("");
        report.setSumissionDate(new Date());
        report.setReportNumber(Integer.parseInt(reportNumberTextField.getText()));
        report.setCoveredHours(approvedHours);
        report.setGeneralObjective(resolveProjectObjective());
        report.setMethodology(methodologyTextArea.getText().trim());

        String obtainedResults = "";
        if (resultsTextArea != null) {
            obtainedResults = resultsTextArea.getText().trim();
        }
        report.setObtainedResults(obtainedResults);

        String observations = "";
        if (observationsTextArea != null) {
            observations = observationsTextArea.getText().trim();
        }
        report.setObservations(observations);

        return report;
    }

    private String resolveProjectObjective() {
        String objective = "";
        if (currentProject.getObjetivo() != null) {
            objective = currentProject.getObjetivo();
        }
        return objective;
    }

    private void savePartialFinalDocument(PartialAndFinalReport report, String reportType, PartialAndFinalReportDAO partialAndFinalReportDAO) throws ValidationException, ServiceException, IOException {
        ReportGenerationContext generationContext = buildCurrentContext(approvedHours);
        String internalPath;

        if (REPORT_TYPE_PARTIAL.equals(reportType)) {
            ReportContent reportContent = new ReportContent(Collections.unmodifiableList(reportActivities), null);
            internalPath = PartialReportGenerator.generate(report, generationContext, reportContent);
        } else {
            persistReportDeliverables(report.getIdReport());
            ReportContent reportContent = new ReportContent(
                    Collections.unmodifiableList(reportActivities),
                    Collections.unmodifiableList(reportDeliverables));
            internalPath = FinalReportGenerator.generate(
                    report, generationContext, reportContent);
        }

        partialAndFinalReportDAO.updateDocumentPath(report.getIdReport(), internalPath);

        AuditLog.record("generó el reporte " + report.getReportType() + " del practicante " + report.getIdIntern());
        showAlert("Reporte generado",
                "Reporte " + reportType.toLowerCase() + " generado correctamente.",
                Alert.AlertType.INFORMATION);
        clearForm();
    }

    private ReportGenerationContext buildCurrentContext(int totalHours) {
        String organizationName = "";
        if (currentOrganization != null) {
            organizationName = currentOrganization.getName();
        }

        String technicianPosition = "";
        if (currentSupervisor != null) {
            technicianPosition = currentSupervisor.getPosition();
        }

        String projectNrc = "";
        if (currentProject.getNrc() != null) {
            projectNrc = currentProject.getNrc();
        }

        ReportGenerationContext generationContext = new ReportGenerationContextBuilder()
                .internFullName(buildFullName(currentIntern))
                .registrationNumber(currentIntern.getRegistrationNumber())
                .nrc(projectNrc)
                .organizationName(organizationName)
                .technicianName(buildFullName(currentSupervisor))
                .technicianPosition(technicianPosition)
                .professorName(buildFullName(currentProfessor))
                .projectName(currentProject.getName())
                .totalApprovedHours(totalHours)
                .ownerWindow(generateButton.getScene().getWindow())
                .build();
        return generationContext;
    }

    private void persistReportActivities(int idReport) {
        ReportActivityDAO reportActivityDAO = new ReportActivityDAO();
        for (ReportActivity reportActivity : reportActivities) {
            reportActivity.setIdReport(idReport);
            try {
                reportActivityDAO.save(reportActivity);
            } catch (ValidationException | ServiceException persistenceException) {
                LOGGER.log(Level.WARNING, "Error al guardar actividad del reporte: {0}",
                        persistenceException.getMessage());
            }
        }
    }

    private void persistReportDeliverables(int idReport) {
        ReportActivityDAO reportActivityDAO = new ReportActivityDAO();
        for (ReportDeliverable reportDeliverable : reportDeliverables) {
            reportDeliverable.setIdReport(idReport);
            try {
                reportActivityDAO.saveDeliverable(reportDeliverable);
            } catch (ValidationException | ServiceException persistenceException) {
                LOGGER.log(Level.WARNING, "Error al guardar entregable del reporte: {0}",
                        persistenceException.getMessage());
            }
        }
    }

    private String buildAcademicPeriod(int year, String month) {
        int monthNumber = MONTHS.indexOf(month) + 1;
        boolean isSpringMonth = monthNumber >= SPRING_FIRST_MONTH && monthNumber <= SPRING_LAST_MONTH;
        String period;
        if (isSpringMonth) {
            period = SPRING_PERIOD_LABEL + year;
        } else if (monthNumber == JANUARY_MONTH_NUMBER) {
            period = String.format(FALL_PERIOD_TEMPLATE, year - 1, year);
        } else {
            period = String.format(FALL_PERIOD_TEMPLATE, year, year + 1);
        }
        return period;
    }

    private String buildFullName(User user) {
        String fullName = "";
        if (user != null) {
            fullName = user.getFirstName() + " " + user.getLastName()
                    + " " + user.getSecondLastName();
        }
        return fullName;
    }

    private String buildFullName(TechnicalSupervisor supervisor) {
        String fullName = "";
        if (supervisor != null) {
            fullName = supervisor.getName() + " " + supervisor.getLastName()
                    + " " + supervisor.getSecondLastName();
        }
        return fullName;
    }

    private void disableGeneration() {
        if (generateButton != null) {
            generateButton.setDisable(true);
        }
        if (rootPane != null) {
            openWelcomePage(rootPane);
        }
    }

    private void clearForm() {
        reportTypeComboBox.getSelectionModel().clearSelection();
        monthComboBox.getSelectionModel().clearSelection();
        reportedHoursTextField.clear();
        reportNumberTextField.clear();
        if (methodologyTextArea != null) {
            methodologyTextArea.clear();
        }
        if (resultsTextArea != null) {
            resultsTextArea.clear();
        }
        if (observationsTextArea != null) {
            observationsTextArea.clear();
        }
        reportActivities.clear();
        reportDeliverables.clear();
    }

    private static int parseIntSafe(String text) {
        int parsedValue;
        try {
            parsedValue = Integer.parseInt(text);
        } catch (NumberFormatException numberFormatException) {
            parsedValue = 0;
        }
        return parsedValue;
    }
}
