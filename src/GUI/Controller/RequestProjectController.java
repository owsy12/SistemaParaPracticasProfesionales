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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static GUI.Utils.Alert.showAlert;

public class RequestProjectController {
    private static final  int SELECTION_LIMIT = 3;
    private static final int MINIMUM_NUMBER_OF_CREDITS_REQUIRED = 270;
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
    private TableColumn<Project,Boolean> seleccionColumn;
    @FXML
    private TableColumn<Project, String> organizationColumn;
    @FXML
    private TableColumn<Project, String> descriptionColumn;
    @FXML
    private TableView projectsTableView;
    Map<Project, Boolean> projectSelectionList = new HashMap<>();

    @FXML
    private void initialize(){
        verifyActiveInternProjectApplication();
    }

    @FXML
    public void projectSelectionListButton(ActionEvent actionEvent) {
        if (!projectSelectionList.isEmpty() && projectSelectionList.size() <= SELECTION_LIMIT){
            viewProjectSelections();
        }else {
            showAlert("Advertencia", "Verifique su seleccion , no puede seleccinar mas de 3",
            Alert.AlertType.WARNING);
        }
    }

    private void viewProjectSelections() {
        try {
            List<Project> projectList = new ArrayList<>();

            if (projectSelectionList == null){
                showAlert("Advertencia", "Seleccione un poryecto",
                        Alert.AlertType.INFORMATION);
            }else {

                for (Map.Entry<Project, Boolean> entry : projectSelectionList.entrySet()) {
                    Project project = getProject(entry);
                    projectList.add(project);
                }
                openViewProjectSelection(projectList);
            }

        } catch (IllegalStateException illegalStateException) {
            showAlert("Error", "No se a logrado encontrar un poryecto seleccionado",
                    Alert.AlertType.ERROR);
        }
    }

    private static Project getProject(Map.Entry<Project, Boolean> entry) {
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

    private void verifyActiveInternProjectApplication(){
        try {
            Intern intern;
            Assignment assignment;
            Application application;
            InternDAO internDAO = new InternDAO();
            AssignmentDAO assignmentDAO = new AssignmentDAO();
            ApplicationDAO applicationDAO = new ApplicationDAO();
            application = applicationDAO.findActiveApplicationByIntern(SessionManager.getInstance().getUsuario().getId());
            assignment = assignmentDAO.getActiveByIdIntern(SessionManager.getInstance().getUsuario().getId());
            intern = internDAO.findById(SessionManager.getInstance().getUsuario().getId());

            if ((intern.getCredits() > MINIMUM_NUMBER_OF_CREDITS_REQUIRED) && (assignment == null) && (application == null )){
                allowRequest();
            }else {
                showAlert("Advertecia", "No cumple con los requisito para poder crear una solicitud",
                        Alert.AlertType.WARNING);
                openWelcomePage();
            }

        } catch (ServiceException e) {
            showAlert("Error", "Servicio no disponible en este momento",
                    Alert.AlertType.ERROR);

        } catch (ValidationException e) {
            showAlert("Error", "No se logro encontrar su usuario",
                    Alert.AlertType.ERROR);
        }

    }

    private void allowRequest() throws ServiceException {
        ApplicationDAO applicationDAO = new ApplicationDAO();
        Application application = applicationDAO.findByIntern(SessionManager.getInstance().getUsuario().getId());

        if (application == null || !"Pendiente".equals(application.getStatus())) {
            configureTable();
        } else {

            showAlert("Advertencia", "Usted ya tiene una solicitud pendiente",
                    Alert.AlertType.WARNING
            );
            openWelcomePage();
        }

    }

    private void openWelcomePage(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/view/GUIWelcome.fml"));
            Parent vista = loader.load();
            anchorPane.getChildren().setAll(vista);
        } catch (IOException e) {
            showAlert("Error", "No se logro cargar",
                    Alert.AlertType.ERROR);
        }
    }

    private void openViewProjectSelection(List<Project> projects){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/view/GUIViewProjectSelection.fxml"));
            Parent vista = loader.load();
            ViewProjectSelectionController controller = loader.getController();
            controller.setProjectList(projects);
            anchorPane.getChildren().setAll(vista);

        } catch (IOException e) {
            showAlert("Error", "Error al cargar", Alert.AlertType.ERROR);
        }
    }

    private void configureTable(){
        loadProjects();
        addCheckBoxToTable();
        configureDataColumns();
    }

    private void configureDataColumns(){
        nameColumn.setCellValueFactory(cellData->
                new SimpleStringProperty(cellData.getValue().getName()));

        organizationColumn.setCellValueFactory(cellDAta ->
                new SimpleStringProperty(cellDAta.getValue().getOrganizationName()));

        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));

        placesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getMaximumPlaces())));

        startDateColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getStartDate()));

        endDateColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getEndDate()));



        startDateColumn.setCellFactory(column -> new TableCell<Project, LocalDate>() {
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
        });

        endDateColumn.setCellFactory(column -> new TableCell<Project, LocalDate>() {
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
        });
    }

    private void addCheckBoxToTable(){

        seleccionColumn.setCellValueFactory(cellData -> {
            Project project = cellData.getValue();

            Boolean isCkecked = projectSelectionList.getOrDefault(project, false);
            return new SimpleBooleanProperty(isCkecked);
        });

        seleccionColumn.setCellFactory(column -> new TableCell<Project, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    Project project = getTableRow().getItem();

                    if (checkBox.isSelected()) {
                        projectSelectionList.put(project, true);
                    } else {
                        projectSelectionList.remove(project);
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
                    checkBox.setSelected(projectSelectionList.getOrDefault(project, false));
                    setGraphic(checkBox);
                }
            }
        });
    }

    private void loadProjects(){
        try {
            List<Project> projectList;
            ProjectDAO projectDAO = new ProjectDAO();
            projectList = projectDAO.findAllAvailable();
            projectsTableView.getItems().setAll(projectList);
        }catch (ServiceException serviceException){
            showAlert("Error", "no se logro cargar projectos",
                    Alert.AlertType.ERROR);
        }
    }

}
