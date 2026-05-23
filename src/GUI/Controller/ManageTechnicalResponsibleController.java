package GUI.Controller;

import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class ManageTechnicalResponsibleController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TableView<TechnicalSupervisor> technicalResponsibleTableView;

    @FXML
    private TableColumn<TechnicalSupervisor, String> nameColumn;

    @FXML
    private TableColumn<TechnicalSupervisor, String> lastNameColumn;

    @FXML
    private TableColumn<TechnicalSupervisor, String> secondLastNameColumn;

    @FXML
    private TableColumn<TechnicalSupervisor, String> emailColumn;

    @FXML
    private TableColumn<TechnicalSupervisor, String> positionColumn;

    @FXML
    private TableColumn<TechnicalSupervisor, Void> actionColumn;

    @FXML
    private void initialize() {
        loadTechnicalResponsibles();
    }

    @FXML
    public void cancelAction() {
        openWelcomePage(anchorPane);
    }

    private void loadTechnicalResponsibles() {
        try {
            TechnicalResponsibleDAO technicalResponsibleDAO = new TechnicalResponsibleDAO();
            List<TechnicalSupervisor> technicalList = technicalResponsibleDAO.findAll();

            if (technicalList.isEmpty()) {
                showAlert("Sin registros",
                        "No hay técnicos responsables registrados.",
                        Alert.AlertType.INFORMATION);
                openWelcomePage(anchorPane);
            } else {
                configureDataColumn();
                technicalResponsibleTableView.getItems().setAll(technicalList);
            }
        } catch (ServiceException serviceException) {
            showAlert("Error de servicio",
                    "No se pudieron cargar los técnicos responsables.",
                    Alert.AlertType.ERROR);
        }
    }

    private void configureDataColumn() {
        nameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<TechnicalSupervisor, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<TechnicalSupervisor, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getName());
            }
        });

        lastNameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<TechnicalSupervisor, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<TechnicalSupervisor, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getLastName());
            }
        });

        secondLastNameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<TechnicalSupervisor, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<TechnicalSupervisor, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getSecondLastName());
            }
        });

        emailColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<TechnicalSupervisor, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<TechnicalSupervisor, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().geteMail());
            }
        });

        positionColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<TechnicalSupervisor, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<TechnicalSupervisor, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getPosition());
            }
        });

        actionColumn.setCellFactory(
                new Callback<TableColumn<TechnicalSupervisor, Void>,
                        TableCell<TechnicalSupervisor, Void>>() {
            @Override
            public TableCell<TechnicalSupervisor, Void> call(
                    TableColumn<TechnicalSupervisor, Void> column) {
                return new TableCell<TechnicalSupervisor, Void>() {
                    private final Button deleteButton = new Button("Eliminar");

                    {
                        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                TechnicalSupervisor technicalSupervisor =
                                        getTableView().getItems().get(getIndex());

                                Optional<ButtonType> response = showAlertAndWait(
                                        "Confirmar eliminación",
                                        "¿Desea eliminar al técnico responsable «"
                                                + technicalSupervisor.getName() + " "
                                                + technicalSupervisor.getLastName()
                                                + "»? Esta acción es irreversible.",
                                        Alert.AlertType.CONFIRMATION);

                                if (response.isPresent() && response.get() == ButtonType.OK) {
                                    deleteTechnicalResponsibleProcess(
                                            technicalSupervisor.getIdTechnicalSupervisor());
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
                            setGraphic(deleteButton);
                        }
                    }
                };
            }
        });
    }

    private void deleteTechnicalResponsibleProcess(int idTechnicalSupervisor) {
        try {
            TechnicalResponsibleDAO technicalResponsibleDAO = new TechnicalResponsibleDAO();
            technicalResponsibleDAO.deleteWithOrganizationValidation(idTechnicalSupervisor);
            showAlert("Eliminación exitosa",
                    "El técnico responsable fue eliminado exitosamente.",
                    Alert.AlertType.INFORMATION);
            loadTechnicalResponsibles();
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(),
                    Alert.AlertType.WARNING);
        } catch (ServiceException serviceException) {
            showAlert("No se pudo eliminar",
                    serviceException.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

}
