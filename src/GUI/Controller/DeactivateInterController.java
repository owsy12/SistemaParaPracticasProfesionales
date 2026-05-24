package GUI.Controller;

import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
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

import java.util.List;
import java.util.Optional;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class DeactivateInterController {

    @FXML
    private TableView<User> internsTableView;

    @FXML
    private TableColumn<User, String> tagColumn;
    @FXML
    private TableColumn<User, String> interFullNameColumn;

    @FXML
    private AnchorPane anchorPane;

    private User selectedUser;

    @FXML
    private void initialize() {
        configureDataColumns();
        configureListeners();
        loadActiveInterns();
    }

    @FXML
    public void inactivateIntern(ActionEvent actionEvent) {
        boolean isSelectionMissing = selectedUser == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un practicante de la tabla para inactivar.",
                    Alert.AlertType.WARNING);
            return;
        }

        Optional<ButtonType> confirmationResponse = showAlertAndWait(
                "Advertencia",
                "¿Está seguro que desea inactivar este practicante?",
                Alert.AlertType.CONFIRMATION);

        if (confirmationResponse.isPresent() && confirmationResponse.get() == ButtonType.OK) {
            inactiveProcess(selectedUser);
            internsTableView.refresh();
        }
    }

    @FXML
    public void cancelOperation(ActionEvent actionEvent) {
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

        interFullNameColumn.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<User, String> cellData) {
                String firstName = cellData.getValue().getFirstName();
                String lastName = cellData.getValue().getLastName();
                String secondLastName = cellData.getValue().getSecondLastName();
                String fullName = firstName + " " + lastName + " " + secondLastName;
                return new SimpleStringProperty(fullName);
            }
        });
    }

    private void configureListeners() {
        internsTableView.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<User>() {
                    @Override
                    public void changed(ObservableValue<? extends User> observable,
                                        User oldValue, User newValue) {
                        selectedUser = newValue;
                    }
                });
    }

    private void loadActiveInterns() {
        try {
            InternDAO internDAO = new InternDAO();
            List<Intern> internList = internDAO.findAllActiveinterns();
            internsTableView.getItems().setAll(internList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Error en el servicio, intente nuevamente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró recuperar los practicantes.",
                    Alert.AlertType.ERROR);
        }
    }

    private void inactiveProcess(User user) {
        try {
            InternDAO internDAO = new InternDAO();
            internDAO.deactivateIntern(user.getId());
            selectedUser = null;
        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible por el momento, intente más tarde.",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logró comprobar al practicante.",
                    Alert.AlertType.ERROR);
        }
    }

}
