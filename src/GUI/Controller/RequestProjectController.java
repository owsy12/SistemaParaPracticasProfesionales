package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.ApplicationDAO;
import Logic.DAO.AssignmentDAO;
import Logic.DAO.InternDAO;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Application;
import Logic.DTOs.Assignment;
import Logic.DTOs.Intern;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class RequestProjectController {

    private static final int SELECTION_LIMIT = 3;
    private static final int MINIMUM_NUMBER_OF_CREDITS_REQUIRED = 270;

    @FXML
    public AnchorPane anchorPane;

    @FXML
    private TableColumn<Project, LocalDate> endDateColumn;

    @FXML
    private TableColumn<Project, String> nameColumn;

    @FXML
    private TableColumn<Project, LocalDate> startDateColumn;

    @FXML
    private TableColumn<Project, String> placesColumn;

    @FXML
    private TableColumn<Project, Boolean> seleccionColumn;

    @FXML
    private TableColumn<Project, String> organizationColumn;

    @FXML
    private TableColumn<Project, String> descriptionColumn;

    @FXML
    private TableView projectsTableView;

    @FXML
    private TableColumn<Project, String> nrcColumn;

    Map<Project, Boolean> projectSelectionList = new HashMap<>();

    @FXML
    private void initialize() {
        verifyActiveInternProjectApplication();
    }

    @FXML
    public void projectSelectionListButton(ActionEvent actionEvent) {
        boolean hasRequiredSelections = projectSelectionList.size() == SELECTION_LIMIT;
        if (hasRequiredSelections) {
            viewProjectSelections();
        } else {
            showAlert("Advertencia", "Verifique su selección, debe seleccionar 3 proyectos.",
                    Alert.AlertType.WARNING);
        }
    }

    private void viewProjectSelections() {
        try {
            List<Project> projectList = new ArrayList<>();

            for (Map.Entry<Project, Boolean> entry : projectSelectionList.entrySet()) {
                Project project = buildProjectFromEntry(entry);
                projectList.add(project);
            }

            openViewProjectSelection(projectList);

        } catch (IllegalStateException illegalStateException) {
            showAlert("Error", "No se logró encontrar un proyecto seleccionado.",
                    Alert.AlertType.ERROR);
        }
    }

    private Project buildProjectFromEntry(Map.Entry<Project, Boolean> entry) {
        Project project = new Project();
        project.setIdProyect(entry.getKey().getIdProyect());
        project.setName(entry.getKey().getName());
        project.setOrganizationName(entry.getKey().getOrganizationName());
        project.setEndDate(entry.getKey().getEndDate());
        project.setStartDate(entry.getKey().getStartDate());
        project.setDescription(entry.getKey().getDescription());
        project.setDescription(String.valueOf(entry.getKey().getAvaliablePlaces()));
        return project;
    }

    private void verifyActiveInternProjectApplication() {
        try {
            InternDAO internDAO = new InternDAO();
            AssignmentDAO assignmentDAO = new AssignmentDAO();
            ApplicationDAO applicationDAO = new ApplicationDAO();
            int currentUserId = SessionManager.getInstance().getUsuario().getId();

            Application application = applicationDAO.findActiveApplicationByIntern(currentUserId);
            Assignment assignment = assignmentDAO.getActiveByIdIntern(currentUserId);
            Intern intern = internDAO.findById(currentUserId);

            boolean hasEnoughCredits = intern.getCredits() > MINIMUM_NUMBER_OF_CREDITS_REQUIRED;
            boolean hasNoAssignment = assignment == null;
            boolean hasNoApplication = application == null;
            boolean canRequest = hasEnoughCredits && hasNoAssignment && hasNoApplication;

            if (canRequest) {
                allowRequest();
            } else {
                showAlert("Advertencia",
                        "No cumple con los requisitos para poder crear una solicitud.",
                        Alert.AlertType.WARNING);
                openWelcomePage(anchorPane);
            }

        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible en este momento.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró encontrar su usuario.",
                    Alert.AlertType.ERROR);
        }
    }

    private void allowRequest() throws ServiceException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        int currentUserId = SessionManager.getInstance().getUsuario().getId();
        Application application = applicationDAO.findByIntern(currentUserId);

        boolean hasPendingApplication = application != null && "Pendiente".equals(application.getStatus());

        if (!hasPendingApplication) {
            configureTable();
        } else {
            showAlert("Advertencia", "Usted ya tiene una solicitud pendiente.",
                    Alert.AlertType.WARNING);
            openWelcomePage(anchorPane);
        }
    }

    private void openViewProjectSelection(List<Project> projects) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/view/GUIViewProjectSelection.fxml"));
            Parent vista = loader.load();
            ViewProjectSelectionController controller = loader.getController();
            controller.setProjectList(projects);
            anchorPane.getChildren().setAll(vista);

        } catch (IOException ioException) {
            showAlert("Error", "Error al cargar.", Alert.AlertType.ERROR);
        }
    }

    private void configureTable() {
        loadProjects();
        addCheckBoxToTable();
        configureDataColumns();
    }

    private void configureDataColumns() {
        nameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getName());
            }
        });

        organizationColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getOrganizationName());
            }
        });

        descriptionColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getDescription());
            }
        });

        placesColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> cellData) {
                return new SimpleStringProperty(
                        String.valueOf(cellData.getValue().getMaximumPlaces()));
            }
        });

        startDateColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, LocalDate>,
                        ObservableValue<LocalDate>>() {
            @Override
            public ObservableValue<LocalDate> call(
                    TableColumn.CellDataFeatures<Project, LocalDate> data) {
                return new SimpleObjectProperty<>(data.getValue().getStartDate());
            }
        });

        endDateColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, LocalDate>,
                        ObservableValue<LocalDate>>() {
            @Override
            public ObservableValue<LocalDate> call(
                    TableColumn.CellDataFeatures<Project, LocalDate> data) {
                return new SimpleObjectProperty<>(data.getValue().getEndDate());
            }
        });

        nrcColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> cellData) {
                return new SimpleStringProperty(
                        String.valueOf(cellData.getValue().getIdProyect()));
            }
        });

        startDateColumn.setCellFactory(
                new Callback<TableColumn<Project, LocalDate>, TableCell<Project, LocalDate>>() {
            @Override
            public TableCell<Project, LocalDate> call(TableColumn<Project, LocalDate> column) {
                return new TableCell<Project, LocalDate>() {
                    @Override
                    protected void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            setText(item.format(formatter));
                        }
                    }
                };
            }
        });

        endDateColumn.setCellFactory(
                new Callback<TableColumn<Project, LocalDate>, TableCell<Project, LocalDate>>() {
            @Override
            public TableCell<Project, LocalDate> call(TableColumn<Project, LocalDate> column) {
                return new TableCell<Project, LocalDate>() {
                    @Override
                    protected void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            setText(item.format(formatter));
                        }
                    }
                };
            }
        });
    }

    private void addCheckBoxToTable() {
        seleccionColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, Boolean>,
                        ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(
                    TableColumn.CellDataFeatures<Project, Boolean> cellData) {
                Project project = cellData.getValue();
                Boolean isChecked = projectSelectionList.getOrDefault(project, false);
                return new SimpleBooleanProperty(isChecked);
            }
        });

        seleccionColumn.setCellFactory(
                new Callback<TableColumn<Project, Boolean>, TableCell<Project, Boolean>>() {
            @Override
            public TableCell<Project, Boolean> call(TableColumn<Project, Boolean> column) {
                return new TableCell<Project, Boolean>() {
                    private final CheckBox checkBox = new CheckBox();

                    {
                        checkBox.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Project project = getTableRow().getItem();
                                if (checkBox.isSelected()) {
                                    projectSelectionList.put(project, true);
                                } else {
                                    projectSelectionList.remove(project);
                                }
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Project project = getTableView().getItems().get(getIndex());
                            checkBox.setSelected(
                                    projectSelectionList.getOrDefault(project, false));
                            setGraphic(checkBox);
                        }
                    }
                };
            }
        });
    }

    private void loadProjects() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projectList = projectDAO.findAllAvailable();
            projectsTableView.getItems().setAll(projectList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logró cargar los proyectos.",
                    Alert.AlertType.ERROR);
        }
    }

}
