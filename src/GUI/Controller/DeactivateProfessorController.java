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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
import static GUI.Utils.ViewsUtils.openWelcomePage;

public class DeactivateProfessorController {

    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> secondLastNameColumn;
    @FXML private TableView<User> tableView;
    @FXML private TableColumn<Professor, String> academicDegreeColumn;
    @FXML private TableColumn<User, String> lastNameColumn;
    @FXML private TableColumn<User, String> tagColumn;
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
        loadProfessors();
        configureListeners();
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
                "\u00BFDesea desactivar este profesor?",
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
        buildCancelAlert().showAndWait();
        openWelcomePage(anchorPane);
    }

    private Alert buildCancelAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Proceso cancelado");
        alert.setHeaderText("Inactivaci\u00F3n cancelada");
        alert.setContentText("La inactivaci\u00F3n de profesor ha sido cancelada. Ser\u00E1s redirigido al inicio.");
        return alert;
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
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedList);
        searchTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            applySearchFilter(newVal);
        });
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
            boolean mAcademicArea = false;
            if (user instanceof Professor) {
                String area = ((Professor) user).getAcademicArea();
                mAcademicArea = area != null && area.toLowerCase().contains(lower);
            }
            matchFound = mMatricula || mFirstName || mLastName || mSecondLastName || mAcademicArea;
        }
        return matchFound;
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
            showAlert("Error", "No se logr\u00F3 desactivar.", Alert.AlertType.ERROR);
        }
    }

    private void loadProfessors() {
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            masterList.setAll(professorDAO.findActiveProfessors());
            updateRecordCount();
        } catch (ValidationException validationException) {
            showAlert("Error", "Servicio no disponible.", Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            showAlert("Error", "No se logr\u00F3 cargar los profesores.", Alert.AlertType.ERROR);
        }
    }

    private void updateRecordCount() {
        int total = masterList.size();
        int visible = filteredList != null ? filteredList.size() : 0;
        recordCountLabel.setText(visible + " de " + total + " registros");
    }
}