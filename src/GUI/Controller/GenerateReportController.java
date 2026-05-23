package GUI.Controller;

import GUI.SessionManager.SessionManager;
import GUI.Utils.ReportContent;
import GUI.Utils.ReportDocxGenerator;
import GUI.Utils.ReportGenerationContext;
import Logic.DAO.*;
import Logic.DTOs.*;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ValidationUtils.setTypeAndLength;

public class GenerateReportController {

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

    private static final List<String> MONTHS = Arrays.asList(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre");

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
    private TextField reportedHoursTextField;

    @FXML
    private TextField reportNumberTextField;

    @FXML
    private TextArea methodologyTextArea;

    @FXML
    private TextArea resultsTextArea;

    @FXML
    private TextArea observationsTextArea;

    @FXML
    private Button generateButton;

    @FXML
    private TableView<Activity> projectActivitiesTable;

    @FXML
    private TableColumn<Activity, String> colActName;

    @FXML
    private TableColumn<Activity, String> colActDesc;

    @FXML
    private TableColumn<Activity, Integer> colActPlanStart;

    @FXML
    private TableColumn<Activity, Integer> colActPlanEnd;

    @FXML
    private TableView<ReportActivity> reportActivitiesTable;

    @FXML
    private TableColumn<ReportActivity, String> colRaName;

    @FXML
    private TableColumn<ReportActivity, String> colRaPeriod;

    @FXML
    private TableColumn<ReportActivity, String> colRaDetail;

    @FXML
    private Label deliverablesLabel;

    @FXML
    private Button addDeliverableButton;

    @FXML
    private Button removeDeliverableButton;

    @FXML
    private TableView<ReportDeliverable> reportDeliverablesTable;

    @FXML
    private TableColumn<ReportDeliverable, String> colRdResult;

    @FXML
    private TableColumn<ReportDeliverable, String> colRdDesc;

    @FXML
    private TableColumn<ReportDeliverable, String> colRdAdvance;

    @FXML
    private TableColumn<ReportDeliverable, String> colRdObs;

    private Intern currentIntern;
    private Project currentProject;
    private LinkedOrganization currentOrganization;
    private TechnicalSupervisor currentSupervisor;
    private Professor currentProfessor;
    private int approvedHours;

    private final ObservableList<Activity> projectActivities = FXCollections.observableArrayList();
    private final ObservableList<ReportActivity> reportActivities = FXCollections.observableArrayList();
    private final ObservableList<ReportDeliverable> reportDeliverables = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setTypeAndLength(reportedHoursTextField, "Number");
        setTypeAndLength(reportNumberTextField, "Number");

        reportTypeComboBox.getItems().setAll(REPORT_TYPE_MONTHLY, REPORT_TYPE_PARTIAL, REPORT_TYPE_FINAL);
        monthComboBox.getItems().setAll(MONTHS);

        configureProjectActivitiesTable();
        configureReportActivitiesTable();
        configureDeliverablesTable();
        configureListeners();

        loadInternContext();
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
        Optional<ReportDeliverable> result = showNewDeliverableDialog();
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
            showAlert("Actividades requeridas",
                    "Agregue al menos una actividad al reporte.", Alert.AlertType.WARNING);
        } else {
            tryProcessGeneration(reportType);
        }
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        clearForm();
    }

    private void configureListeners() {
        reportTypeComboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                applyReportTypeVisibility();
            }
        });
    }

    private void tryAddActivity(Activity selected) {
        String reportType = reportTypeComboBox.getValue();
        boolean isReportTypeMissing = reportType == null;
        boolean isAlreadyAdded = isActivityAlreadyAdded(selected);

        if (isReportTypeMissing) {
            showAlert("Tipo de reporte", "Seleccione primero el tipo de reporte.",
                    Alert.AlertType.WARNING);
        } else if (isAlreadyAdded) {
            showAlert("Actividad duplicada", "Esta actividad ya fue agregada al reporte.",
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
            if (reportActivity.getIdActividad() == activity.getIdActivity()) {
                alreadyAdded = true;
            }
        }
        return alreadyAdded;
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

    private void configureProjectActivitiesTable() {
        colActName.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Activity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Activity, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getName());
            }
        });

        colActDesc.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Activity, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Activity, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getDescription());
            }
        });

        colActPlanStart.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Activity, Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(
                    TableColumn.CellDataFeatures<Activity, Integer> cellData) {
                return new SimpleIntegerProperty(
                        cellData.getValue().getSemanaInicioPlan()).asObject();
            }
        });

        colActPlanEnd.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Activity, Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(
                    TableColumn.CellDataFeatures<Activity, Integer> cellData) {
                return new SimpleIntegerProperty(
                        cellData.getValue().getSemanaFinPlan()).asObject();
            }
        });

        projectActivitiesTable.setItems(projectActivities);
    }

    private void configureReportActivitiesTable() {
        colRaName.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ReportActivity, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<ReportActivity, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getActivityName());
            }
        });

        colRaPeriod.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ReportActivity, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<ReportActivity, String> cellData) {
                return new SimpleStringProperty(resolvePeriodDisplay(cellData.getValue()));
            }
        });

        colRaDetail.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ReportActivity, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<ReportActivity, String> cellData) {
                return new SimpleStringProperty(resolveDetailDisplay(cellData.getValue()));
            }
        });

        reportActivitiesTable.setItems(reportActivities);
    }

    private String resolvePeriodDisplay(ReportActivity reportActivity) {
        String periodDisplay;
        if (reportActivity.getPeriodo() != null) {
            periodDisplay = reportActivity.getPeriodo();
        } else if (reportActivity.getPlanSemanas() != null) {
            periodDisplay = "Plan:" + reportActivity.getPlanSemanas();
        } else {
            periodDisplay = "";
        }
        return periodDisplay;
    }

    private String resolveDetailDisplay(ReportActivity reportActivity) {
        String detailDisplay;
        if (reportActivity.getPorcentajeAvance() > 0) {
            detailDisplay = reportActivity.getPorcentajeAvance() + "%";
        } else if (reportActivity.getObservaciones() != null) {
            detailDisplay = reportActivity.getObservaciones();
        } else {
            detailDisplay = "";
        }
        return detailDisplay;
    }

    private void configureDeliverablesTable() {
        colRdResult.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ReportDeliverable, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<ReportDeliverable, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getResultado());
            }
        });

        colRdDesc.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ReportDeliverable, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<ReportDeliverable, String> cellData) {
                String descripcion = "";
                if (cellData.getValue().getDescripcion() != null) {
                    descripcion = cellData.getValue().getDescripcion();
                }
                return new SimpleStringProperty(descripcion);
            }
        });

        colRdAdvance.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ReportDeliverable, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<ReportDeliverable, String> cellData) {
                return new SimpleStringProperty(
                        cellData.getValue().getPorcentajeAvance() + "%");
            }
        });

        colRdObs.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<ReportDeliverable, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<ReportDeliverable, String> cellData) {
                String observaciones = "";
                if (cellData.getValue().getObservaciones() != null) {
                    observaciones = cellData.getValue().getObservaciones();
                }
                return new SimpleStringProperty(observaciones);
            }
        });

        reportDeliverablesTable.setItems(reportDeliverables);
    }

    private void applyReportTypeVisibility() {
        String type = reportTypeComboBox.getValue();
        if (type != null) {
            boolean isMonthly = REPORT_TYPE_MONTHLY.equals(type);
            boolean isFinal = REPORT_TYPE_FINAL.equals(type);
            boolean isPartialOrFinal = !isMonthly;

            monthComboBox.setVisible(isMonthly);
            reportedHoursTextField.setVisible(isMonthly);
            reportNumberTextField.setVisible(isPartialOrFinal);
            methodologyTextArea.setVisible(isPartialOrFinal);
            resultsTextArea.setVisible(isPartialOrFinal);

            deliverablesLabel.setVisible(isFinal);
            reportDeliverablesTable.setVisible(isFinal);
            addDeliverableButton.setVisible(isFinal);
            removeDeliverableButton.setVisible(isFinal);
        }
    }

    private void loadInternContext() {
        if (SessionManager.getInstance().getUsuario() == null) {
            disableGeneration();
        } else {
            tryLoadInternData();
        }
    }

    private void tryLoadInternData() {
        int internId = SessionManager.getInstance().getUsuario().getId();
        try {
            fetchInternData(internId);
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    "Los datos del practicante no son válidos.", Alert.AlertType.ERROR);
            disableGeneration();
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE, "Error al cargar contexto del practicante {0}: {1}",
                    new Object[]{internId, serviceException.getMessage()});
            showAlert("Servicio no disponible",
                    "No se pudo cargar su información. Intente más tarde.",
                    Alert.AlertType.ERROR);
            disableGeneration();
        }
    }

    private void fetchInternData(int internId) throws ValidationException, ServiceException {
        InternDAO internDAO = new InternDAO();
        currentIntern = internDAO.findById(internId);

        AssignmentDAO assignmentDAO = new AssignmentDAO();
        Assignment assignment = assignmentDAO.getActiveByIdIntern(internId);

        if (assignment == null) {
            showAlert("Sin proyecto asignado",
                    "No tiene un proyecto activo. No puede generar reportes.",
                    Alert.AlertType.WARNING);
            disableGeneration();
        } else {
            fetchProjectData(assignment, internId);
        }
    }

    private void fetchProjectData(Assignment assignment, int internId)
            throws ValidationException, ServiceException {
        ProjectDAO projectDAO = new ProjectDAO();
        currentProject = projectDAO.findById(assignment.getIdProyect());

        LinkedOrganizationDAO organizationDAO = new LinkedOrganizationDAO();
        currentOrganization = organizationDAO.findById(currentProject.getIdOrganization());

        TechnicalResponsibleDAO techDAO = new TechnicalResponsibleDAO();
        currentSupervisor = techDAO.findById(currentProject.getIdTechnicalSupervisor());

        ProfessorDAO professorDAO = new ProfessorDAO();
        currentProfessor = professorDAO.findById(currentProject.getIdProfessor());

        ReportDAO reportDAO = new ReportDAO();
        approvedHours = reportDAO.getTotalApprovedHoursByIntern(internId);

        populateReadOnlyInfo();
        loadProjectActivities();
    }

    private void loadProjectActivities() {
        try {
            ActivityDAO activityDAO = new ActivityDAO();
            List<Activity> activities = activityDAO.findByProject(currentProject.getIdProyect());
            projectActivities.setAll(activities);
        } catch (ValidationException | ServiceException persistenceException) {
            LOGGER.log(Level.WARNING, "No se pudieron cargar las actividades del proyecto: {0}",
                    persistenceException.getMessage());
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

    private Optional<ReportActivity> showActivityProgressDialog(Activity activity,
                                                                 String reportType) {
        Dialog<ReportActivity> dialog;
        if (REPORT_TYPE_MONTHLY.equals(reportType)) {
            dialog = buildMonthlyActivityDialog(activity);
        } else if (REPORT_TYPE_PARTIAL.equals(reportType)) {
            dialog = buildPartialActivityDialog(activity);
        } else {
            dialog = buildFinalActivityDialog(activity);
        }
        return dialog.showAndWait();
    }

    private Dialog<ReportActivity> buildMonthlyActivityDialog(Activity activity) {
        Dialog<ReportActivity> dialog = createActivityDialog(activity.getName());
        ButtonType confirmType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        GridPane grid = buildDialogGrid();
        TextField periodField = new TextField();
        periodField.setPromptText("Ej: Semana 1-3");
        TextArea observationsField = new TextArea();
        observationsField.setPromptText("Observaciones de la actividad");
        observationsField.setPrefRowCount(3);

        grid.add(new Label("Período de ejecución:"), 0, 0);
        grid.add(periodField, 1, 0);
        grid.add(new Label("Observaciones:"), 0, 1);
        grid.add(observationsField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        final ReportActivity reportActivity = createReportActivity(activity);

        dialog.setResultConverter(new Callback<ButtonType, ReportActivity>() {
            @Override
            public ReportActivity call(ButtonType buttonType) {
                ReportActivity result = null;
                if (buttonType == confirmType) {
                    reportActivity.setPeriodo(periodField.getText().trim());
                    reportActivity.setObservaciones(observationsField.getText().trim());
                    result = reportActivity;
                }
                return result;
            }
        });

        return dialog;
    }

    private Dialog<ReportActivity> buildPartialActivityDialog(Activity activity) {
        Dialog<ReportActivity> dialog = createActivityDialog(activity.getName());
        ButtonType confirmType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        int planStart = activity.getSemanaInicioPlan() > 0 ? activity.getSemanaInicioPlan() : 1;
        int planEnd = activity.getSemanaFinPlan() > 0 ? activity.getSemanaFinPlan() : 8;

        GridPane grid = buildDialogGrid();
        TextField realFromField = new TextField("1");
        TextField realToField = new TextField(String.valueOf(planEnd));
        TextArea observationsField = new TextArea();
        observationsField.setPromptText("Observaciones");
        observationsField.setPrefRowCount(2);

        grid.add(new Label("Plan (semanas del proyecto):"), 0, 0);
        grid.add(new Label("Plan: S" + planStart + " - S" + planEnd), 1, 0);
        grid.add(new Label("Real desde semana:"), 0, 1);
        grid.add(realFromField, 1, 1);
        grid.add(new Label("Real hasta semana:"), 0, 2);
        grid.add(realToField, 1, 2);
        grid.add(new Label("Observaciones:"), 0, 3);
        grid.add(observationsField, 1, 3);
        dialog.getDialogPane().setContent(grid);

        final ReportActivity reportActivity = createReportActivity(activity);
        final int finalPlanStart = planStart;
        final int finalPlanEnd = planEnd;

        dialog.setResultConverter(new Callback<ButtonType, ReportActivity>() {
            @Override
            public ReportActivity call(ButtonType buttonType) {
                ReportActivity result = null;
                if (buttonType == confirmType) {
                    reportActivity.setPlanSemanas(finalPlanStart + ":" + finalPlanEnd);
                    reportActivity.setRealSemanas(
                            realFromField.getText().trim() + ":" + realToField.getText().trim());
                    reportActivity.setObservaciones(observationsField.getText().trim());
                    result = reportActivity;
                }
                return result;
            }
        });

        return dialog;
    }

    private Dialog<ReportActivity> buildFinalActivityDialog(Activity activity) {
        Dialog<ReportActivity> dialog = createActivityDialog(activity.getName());
        ButtonType confirmType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        GridPane grid = buildDialogGrid();
        TextField advanceField = new TextField("0");
        advanceField.setPromptText("Porcentaje 0-100");
        TextArea observationsField = new TextArea();
        observationsField.setPromptText("Observaciones");
        observationsField.setPrefRowCount(3);

        grid.add(new Label("% de avance:"), 0, 0);
        grid.add(advanceField, 1, 0);
        grid.add(new Label("Observaciones:"), 0, 1);
        grid.add(observationsField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        final ReportActivity reportActivity = createReportActivity(activity);

        dialog.setResultConverter(new Callback<ButtonType, ReportActivity>() {
            @Override
            public ReportActivity call(ButtonType buttonType) {
                ReportActivity result = null;
                if (buttonType == confirmType) {
                    reportActivity.setPorcentajeAvance(parseIntSafe(advanceField.getText().trim()));
                    reportActivity.setObservaciones(observationsField.getText().trim());
                    result = reportActivity;
                }
                return result;
            }
        });

        return dialog;
    }

    private Dialog<ReportActivity> createActivityDialog(String activityName) {
        Dialog<ReportActivity> dialog = new Dialog<>();
        dialog.setTitle("Progreso de actividad");
        dialog.setHeaderText("Actividad: " + activityName);
        return dialog;
    }

    private ReportActivity createReportActivity(Activity activity) {
        ReportActivity reportActivity = new ReportActivity();
        reportActivity.setIdActividad(activity.getIdActivity());
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

    private int parseIntSafe(String text) {
        int parsedValue = 0;
        try {
            parsedValue = Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            parsedValue = 0;
        }
        return parsedValue;
    }

    private Optional<ReportDeliverable> showNewDeliverableDialog() {
        Dialog<ReportDeliverable> dialog = new Dialog<>();
        dialog.setTitle("Agregar entregable");
        dialog.setHeaderText("Nuevo entregable del reporte final");

        ButtonType confirmType = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, ButtonType.CANCEL);

        GridPane grid = buildDialogGrid();

        TextField resultTextField = new TextField();
        resultTextField.setPromptText("Resultado entregable");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Descripción");
        descriptionField.setPrefRowCount(2);
        TextField advanceField = new TextField("0");
        advanceField.setPromptText("Porcentaje 0-100");
        TextArea observationsField = new TextArea();
        observationsField.setPromptText("Observaciones");
        observationsField.setPrefRowCount(2);

        grid.add(new Label("Resultado entregable:"), 0, 0);
        grid.add(resultTextField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("% de avance:"), 0, 2);
        grid.add(advanceField, 1, 2);
        grid.add(new Label("Observaciones:"), 0, 3);
        grid.add(observationsField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(new Callback<ButtonType, ReportDeliverable>() {
            @Override
            public ReportDeliverable call(ButtonType buttonType) {
                ReportDeliverable deliverable = null;
                boolean isConfirm = buttonType == confirmType;
                boolean isResultNotBlank = !resultTextField.getText().isBlank();

                if (isConfirm && isResultNotBlank) {
                    deliverable = new ReportDeliverable();
                    deliverable.setResultado(resultTextField.getText().trim());
                    deliverable.setDescripcion(descriptionField.getText().trim());
                    deliverable.setPorcentajeAvance(parseIntSafe(advanceField.getText().trim()));
                    deliverable.setObservaciones(observationsField.getText().trim());
                }
                return deliverable;
            }
        });

        return dialog.showAndWait();
    }

    private boolean validateBusinessRules(String reportType) throws ServiceException {
        int internId = currentIntern.getId();
        int projectId = currentProject.getIdProyect();
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

    private boolean validateMonthlyRules(int internId, ReportDAO reportDAO)
            throws ServiceException {
        boolean isValid = true;
        if (monthComboBox.getValue() == null) {
            showAlert("Mes requerido",
                    "Seleccione el mes para el reporte mensual.",
                    Alert.AlertType.WARNING);
            isValid = false;
        } else {
            int year = LocalDate.now().getYear();
            boolean exists = reportDAO.existsMonthlyByInternAndPeriod(
                    internId, monthComboBox.getValue(), year);
            if (exists) {
                showAlert("Reporte duplicado",
                        "Ya existe un reporte mensual para "
                        + monthComboBox.getValue() + " " + year + ".",
                        Alert.AlertType.WARNING);
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean validatePartialRules(int internId, int projectId, ReportDAO reportDAO)
            throws ServiceException {
        boolean isValid = true;
        if (approvedHours < PARTIAL_MIN_HOURS) {
            showAlert("Horas insuficientes",
                    "Necesita al menos " + PARTIAL_MIN_HOURS
                    + " horas validadas. Actuales: " + approvedHours + ".",
                    Alert.AlertType.WARNING);
            isValid = false;
        } else if (reportDAO.existsPartialByInternAndProject(internId, projectId)) {
            showAlert("Reporte duplicado",
                    "Ya existe un reporte parcial para este proyecto.",
                    Alert.AlertType.WARNING);
            isValid = false;
        }
        return isValid;
    }

    private boolean validateFinalRules(int internId, int projectId, ReportDAO reportDAO)
            throws ServiceException {
        boolean isValid = true;
        if (approvedHours < FINAL_MIN_HOURS) {
            showAlert("Horas insuficientes",
                    "Necesita al menos " + FINAL_MIN_HOURS
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

    private void generateMonthlyProcess()
            throws ValidationException, ServiceException, IOException {
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
        } else if (reportedHoursTextField.getText().isBlank()) {
            showAlert("Horas requeridas",
                    "Ingrese las horas reportadas en el mes.", Alert.AlertType.WARNING);
            isValid = false;
        }
        return isValid;
    }

    private void executeMonthlyGeneration()
            throws ValidationException, ServiceException, IOException {
        int year = LocalDate.now().getYear();
        String month = monthComboBox.getValue();
        int reportedHoursCount = Integer.parseInt(reportedHoursTextField.getText());
        String period = buildAcademicPeriod(year, month);

        MonthlyReport report = new MonthlyReport();
        report.setIdIntern(currentIntern.getId());
        report.setIdProyect(currentProject.getIdProyect());
        report.setIdProfessor(currentProject.getIdProfessor());
        report.setReportType(REPORT_TYPE_MONTHLY);
        report.setPeriod(period);
        report.setStatus("Pendiente");
        report.setMonth(month);
        report.setYear(year);
        report.setMonthlyHours(reportedHoursCount);
        report.setReportedHours(reportedHoursCount);
        report.setDocumentPath("");
        report.setSumissionDate(new Date());

        MonthlyReportDAO monthlyReportDAO = new MonthlyReportDAO();
        int rowsAffected = monthlyReportDAO.save(report);

        if (rowsAffected > 0) {
            persistReportActivities(report.getIdReport());

            ReportGenerationContext generationContext = buildCurrentContext();
            ReportContent reportContent = new ReportContent(
                    Collections.unmodifiableList(reportActivities), null);

            String internalPath = ReportDocxGenerator.generateMonthlyReport(
                    report, generationContext, reportContent);

            monthlyReportDAO.updateDocumentPath(report.getIdReport(), internalPath);

            showAlert("Reporte generado", "Reporte mensual generado correctamente.",
                    Alert.AlertType.INFORMATION);
            clearForm();
        } else {
            showAlert("Error", "No se pudo guardar el reporte.", Alert.AlertType.ERROR);
        }
    }

    private void generatePartialFinalProcess(String reportType)
            throws ValidationException, ServiceException, IOException {
        if (isPartialFinalInputValid()) {
            executePartialFinalGeneration(reportType);
        }
    }

    private boolean isPartialFinalInputValid() {
        boolean isReportNumberBlank = reportNumberTextField.getText().isBlank();
        boolean isMethodologyBlank = methodologyTextArea.getText().isBlank();
        boolean isValid = true;

        if (isReportNumberBlank || isMethodologyBlank) {
            showAlert("Campos requeridos",
                    "Complete el número de informe y la metodología.",
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
        report.setIdProyect(currentProject.getIdProyect());
        report.setIdProfessor(currentProject.getIdProfessor());
        report.setReportType(reportType);
        report.setPeriod(period);
        report.setStatus("Pendiente");
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

    private void savePartialFinalDocument(PartialAndFinalReport report,
                                          String reportType,
                                          PartialAndFinalReportDAO partialAndFinalReportDAO)
            throws ValidationException, ServiceException, IOException {
        ReportGenerationContext generationContext = buildCurrentContext();
        String internalPath;

        if (REPORT_TYPE_PARTIAL.equals(reportType)) {
            ReportContent reportContent = new ReportContent(
                    Collections.unmodifiableList(reportActivities), null);
            internalPath = ReportDocxGenerator.generatePartialReport(
                    report, generationContext, reportContent);
        } else {
            persistReportDeliverables(report.getIdReport());
            ReportContent reportContent = new ReportContent(
                    Collections.unmodifiableList(reportActivities),
                    Collections.unmodifiableList(reportDeliverables));
            internalPath = ReportDocxGenerator.generateFinalReport(
                    report, generationContext, reportContent);
        }

        partialAndFinalReportDAO.updateDocumentPath(report.getIdReport(), internalPath);

        showAlert("Reporte generado",
                "Reporte " + reportType.toLowerCase() + " generado correctamente.",
                Alert.AlertType.INFORMATION);
        clearForm();
    }

    private ReportGenerationContext buildCurrentContext() {
        String organizationName = "";
        if (currentOrganization != null) {
            organizationName = currentOrganization.getName();
        }

        String technicianPosition = "";
        if (currentSupervisor != null) {
            technicianPosition = currentSupervisor.getPosition();
        }

        return new ReportGenerationContext.Builder()
                .internFullName(buildFullName(currentIntern))
                .matricula(currentIntern.getMatricula())
                .organizationName(organizationName)
                .technicianName(buildFullName(currentSupervisor))
                .technicianPosition(technicianPosition)
                .professorName(buildFullName(currentProfessor))
                .projectName(currentProject.getName())
                .totalApprovedHours(approvedHours)
                .ownerWindow(generateButton.getScene().getWindow())
                .build();
    }

    private void persistReportActivities(int idReport) {
        ReportActivityDAO reportActivityDAO = new ReportActivityDAO();
        for (ReportActivity reportActivity : reportActivities) {
            reportActivity.setIdReporte(idReport);
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
            reportDeliverable.setIdReporte(idReport);
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
        String period;
        if (monthNumber >= SPRING_FIRST_MONTH && monthNumber <= SPRING_LAST_MONTH) {
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

}
