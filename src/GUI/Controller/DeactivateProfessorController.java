package GUI.Controller;

import Logic.DAO.ProfessorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Professor;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class DeactivateProfessorController {

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> secondLastNameColumn;

    @FXML
    private TableView<User> tableView;

    @FXML
    private TableColumn<Professor, String> academicDegreeColumn;

    @FXML
    private TableColumn<User, String> lastNameColumn;

    @FXML
    private TableColumn<User, String> tagColumn;

    @FXML
    private AnchorPane anchorPane;

    private User selectedUser;

    @FXML
    private void initialize() {
        configureDataColumns();
        configureListeners();
        loadProfessors();
    }

    @FXML
    public void inactivateProfessor(ActionEvent actionEvent) {
        boolean isSelectionMissing = selectedUser == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un profesor de la tabla para inactivar.",
                    Alert.AlertType.WARNING);
            return;
        }

        Optional<ButtonType> response = showAlertAndWait(
                "Desea desactivar",
                "¿Desea desactivar este profesor?",
                Alert.AlertType.CONFIRMATION);

        if (response.isPresent() && response.get() == ButtonType.OK) {
            selectedUser.setStatus("Inactivo");
            selectedUser.setRole("Profesor");
            deactivateProcess(selectedUser);
            loadProfessors();
        }
    }

    @FXML
    public void cancelOperation(ActionEvent actionEvent) {
        showAlert("Información", "Operación cancelada.", Alert.AlertType.INFORMATION);
        openWelcomePage(anchorPane);
    }

    private void configureDataColumns() {
        tagColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getMatricula());
            }
        });

        nameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getFirstName());
            }
        });

        lastNameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getLastName());
            }
        });

        secondLastNameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getSecondLastName());
            }
        });

        academicDegreeColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Professor, String>,
                        ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(
                    TableColumn.CellDataFeatures<Professor, String> cellData) {
                return new SimpleStringProperty(cellData.getValue().getAcademicArea());
            }
        });
    }

    private void configureListeners() {
        tableView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<User>() {
                    @Override
                    public void changed(ObservableValue<? extends User> observable,
                                        User oldValue, User newValue) {
                        selectedUser = newValue;
                    }
                });
    }

    private void deactivateProcess(User user) {
        try {
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            userRoleDAO.updateUserRolStatus(user);
            selectedUser = null;
            showAlert("Profesor desactivado",
                    "El profesor ha sido desactivado exitosamente.",
                    Alert.AlertType.INFORMATION);
        } catch (ValidationException validationException) {
            showAlert("Error", "Servicio no disponible.", Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logró desactivar.", Alert.AlertType.ERROR);
        }
    }

    private void loadProfessors() {
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            tableView.getItems().setAll(professorDAO.findActiveProfessors());
        } catch (ValidationException validationException) {
            showAlert("Error", "Servicio no disponible.", Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logró cargar los profesores.", Alert.AlertType.ERROR);
        }
    }

}
