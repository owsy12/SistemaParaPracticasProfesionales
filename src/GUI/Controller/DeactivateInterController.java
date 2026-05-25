package GUI.Controller;

import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import java.util.Optional;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;
import static GUI.Utils.Alert.showFormAlert;
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class DeactivateInterController {

    @FXML private TableView<User> internsTableView;
    @FXML private TableColumn<User, String> tagColumn;
    @FXML private TableColumn<User, String> interFullNameColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private AnchorPane anchorPane;
    @FXML private TextField searchTextField;
    @FXML private Label recordCountLabel;

    private final ObservableList<User> masterList = FXCollections.observableArrayList();
    private FilteredList<User> filteredList;
    private User selectedUser;

    @FXML
    private void initialize() {
        configureDataColumns();
        configureStatusColumn();
        configureSearch();
        loadActiveInterns();
        configureListeners();
    }

    @FXML
    public void inactivateIntern(ActionEvent actionEvent) {
        boolean isSelectionMissing = selectedUser == null;
        if (isSelectionMissing) {
            showFormAlert(AlertType.WARNING,
                    "Sin selecci\u00F3n",
                    "Ning\u00FAn practicante seleccionado",
                    "Seleccione un practicante de la tabla para inactivar.");
            return;
        }

        Optional<ButtonType> confirmationResponse = showAlertAndWait(
                "Confirmar inactivaci\u00F3n",
                "\u00BFEst\u00E1 seguro que desea inactivar este practicante?",
                javafx.scene.control.Alert.AlertType.CONFIRMATION);

        if (confirmationResponse.isPresent() && confirmationResponse.get() == ButtonType.OK) {
            inactiveProcess(selectedUser);
            loadActiveInterns();
        }
    }

    @FXML
    public void cancelOperation(ActionEvent actionEvent) {
        showFormAlert(AlertType.INFORMATION,
                "Proceso cancelado",
                "Inactivaci\u00F3n cancelada",
                "El proceso ha sido cancelado. Ser\u00E1s redirigido al inicio.");
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

    private void configureStatusColumn() {
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));
        statusColumn.setCellFactory(col -> buildStatusCell());
    }

    private TableCell<User, String> buildStatusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    setGraphic(buildStatusBadge(status));
                }
            }
        };
    }

    private Label buildStatusBadge(String status) {
        Label badge = new Label();
        boolean isActive = "Activo".equalsIgnoreCase(status);
        badge.setText(isActive ? "\u2714  ACTIVO" : "\u00D7  INACTIVO");
        badge.getStyleClass().add(isActive ? "statusActiveBadgeLabel" : "statusInactiveBadgeLabel");
        return badge;
    }

    private void configureSearch() {
        filteredList = new FilteredList<>(masterList, p -> true);
        SortedList<User> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(internsTableView.comparatorProperty());
        internsTableView.setItems(sortedList);
        searchTextField.textProperty().addListener((obs, oldVal, newVal) ->
                applySearchFilter(newVal));
    }

    private void applySearchFilter(String searchText) {
        filteredList.setPredicate(user -> matchesSearchCriteria(user, searchText));
        updateRecordCount();
    }

    private boolean matchesSearchCriteria(User user, String searchText) {
        boolean matchFound = true;
        if (searchText != null && !searchText.isEmpty()) {
            String lower = searchText.toLowerCase();
            boolean mMatricula = user.getMatricula().toLowerCase().contains(lower);
            boolean mFirstName = user.getFirstName().toLowerCase().contains(lower);
            boolean mLastName = user.getLastName().toLowerCase().contains(lower);
            boolean mSecondLastName = user.getSecondLastName() != null &&
                    user.getSecondLastName().toLowerCase().contains(lower);
            matchFound = mMatricula || mFirstName || mLastName || mSecondLastName;
        }
        return matchFound;
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
            masterList.setAll(internDAO.findAllActiveinterns());
            updateRecordCount();
        } catch (ServiceException serviceException) {
            showFormAlert(AlertType.ERROR,
                    "Error de servicio",
                    "Servicio no disponible",
                    "Error en el servicio, intente nuevamente m\u00E1s tarde.");
        } catch (ValidationException validationException) {
            showFormAlert(AlertType.ERROR,
                    "Error de validaci\u00F3n",
                    "No se pudieron cargar los practicantes",
                    "No se logr\u00F3 recuperar los practicantes.");
        }
    }

    private void inactiveProcess(User user) {
        try {
            InternDAO internDAO = new InternDAO();
            internDAO.deactivateIntern(user.getId());
            selectedUser = null;
        } catch (ServiceException serviceException) {
            showFormAlert(AlertType.ERROR,
                    "Error de servicio",
                    "Servicio no disponible",
                    "Servicio no disponible por el momento, intente m\u00E1s tarde.");
        } catch (ValidationException validationException) {
            showFormAlert(AlertType.ERROR,
                    "Error de validaci\u00F3n",
                    "No se pudo inactivar",
                    "No se logr\u00F3 comprobar al practicante.");
        }
    }

    private void updateRecordCount() {
        int total = masterList.size();
        int visible = filteredList != null ? filteredList.size() : 0;
        recordCountLabel.setText(visible + " de " + total + " registros");
    }
}