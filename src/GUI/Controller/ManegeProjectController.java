package GUI.Controller;

import Logic.DAO.ProjectDAO;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class ManegeProjectController {

    @FXML
    private TableColumn<Project, String> capacityColumn;

    @FXML
    private TableColumn<Project, String> endDateColumn;

    @FXML
    private TableColumn<Project, Void> updateColumn;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TableColumn<Project, String> nameColumn;

    @FXML
    private TableColumn<Project, String> statusColmun;

    @FXML
    private TableView tableView;

    @FXML
    private TableColumn<Project, String> organizationColumn;

    @FXML
    private TableColumn deleteColumn;

    @FXML
    private TableColumn<Project, String> starDateColumn;

    @FXML
    private TableColumn<Project, String> nrcColumn;

    @FXML
    private void initialize() {
        loadProjectsOnTableView();
    }

    @FXML
    public void cancelAction(ActionEvent actionEvent) {
        openWelcomePage(anchorPane);
    }

    private void loadProjectsOnTableView() {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            List<Project> projectList = projectDAO.findAll();

            if (projectList != null) {
                configureDataColumnsTable();
                tableView.getItems().setAll(projectList);
            } else {
                showAlert("Advertencia", "No se encontraron proyectos.", Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);
            }

        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible, intente más tarde.", Alert.AlertType.ERROR);
        }
    }

    private void configureDataColumnsTable() {
        capacityColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> project) {
                return new SimpleStringProperty(
                        String.valueOf(project.getValue().getMaximumPlaces()));
            }
        });

        endDateColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> project) {
                return new SimpleStringProperty(project.getValue().getEndDate().toString());
            }
        });

        starDateColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> project) {
                return new SimpleStringProperty(project.getValue().getStartDate().toString());
            }
        });

        nameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> project) {
                return new SimpleStringProperty(project.getValue().getName());
            }
        });

        statusColmun.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> project) {
                return new SimpleStringProperty(project.getValue().getStatus());
            }
        });

        organizationColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> project) {
                return new SimpleStringProperty(project.getValue().getOrganizationName());
            }
        });

        nrcColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Project, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Project, String> project) {
                return new SimpleStringProperty(String.valueOf(project.getValue().getIdProyect()));
            }
        });

        addActionButtons();
    }

    private void addActionButtons() {
        deleteColumn.setCellFactory(new Callback<TableColumn<Project, Void>, TableCell<Project, Void>>() {
            @Override
            public TableCell<Project, Void> call(TableColumn<Project, Void> column) {
                return new TableCell<Project, Void>() {
                    private final Button button = new Button("Eliminar");

                    {
                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                int idProject = getTableView().getItems().get(getIndex()).getIdProyect();
                                Optional<ButtonType> response = showAlertAndWait(
                                        "Desea Eliminar",
                                        "¿Desea eliminar este proyecto? Esta acción es irreversible.",
                                        Alert.AlertType.CONFIRMATION);

                                if (response.isPresent() && response.get() == ButtonType.OK) {
                                    deleteProcess(idProject);
                                }
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(button);
                        }
                    }
                };
            }
        });

        updateColumn.setCellFactory(new Callback<TableColumn<Project, Void>, TableCell<Project, Void>>() {
            @Override
            public TableCell<Project, Void> call(TableColumn<Project, Void> column) {
                return new TableCell<Project, Void>() {
                    private final Button button = new Button("Actualizar");

                    {
                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Project project = getTableView().getItems().get(getIndex());
                                openModifyProjectView(project);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(button);
                        }
                    }
                };
            }
        });
    }

    private void openModifyProjectView(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/view/GUIUpdateProject.fxml"));
            Parent vista = loader.load();
            UpdateProjectController controller = loader.getController();
            controller.setProject(project);
            anchorPane.getChildren().setAll(vista);
        } catch (IOException ioException) {
            showAlert("Error", "No se logró cargar la vista.",
                    Alert.AlertType.ERROR);
        }
    }

    private void deleteProcess(int idProject) {
        try {
            ProjectDAO projectDAO = new ProjectDAO();
            projectDAO.deleteProject(idProject);
            loadProjectsOnTableView();
            showAlert("Éxito", "Proyecto eliminado exitosamente.",
                    Alert.AlertType.INFORMATION);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible.", Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró validar el proyecto.", Alert.AlertType.ERROR);
        }
    }

}
