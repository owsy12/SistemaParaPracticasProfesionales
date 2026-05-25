package GUI.Controller;

import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

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

    private TechnicalSupervisor selectedTechnical;

    @FXML
    private void initialize() {
        configureListeners();
        loadTechnicalResponsibles();
    }

    @FXML
    public void cancelAction() {
        openWelcomePage(anchorPane);
    }

    @FXML
    public void deleteTechnicalResponsible(ActionEvent actionEvent) {
        boolean isSelectionMissing = selectedTechnical == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un técnico responsable de la tabla para eliminar.",
                    Alert.AlertType.WARNING);
            return;
        }

        String fullName = selectedTechnical.getName() + " " + selectedTechnical.getLastName();
        String confirmationMessage = "¿Desea eliminar al técnico responsable «"
                + fullName + "»? Esta acción es irreversible.";
        Optional<ButtonType> response = showAlertAndWait(
                "Confirmar eliminación", confirmationMessage, Alert.AlertType.CONFIRMATION);

        boolean isUserConfirmed = response.isPresent() && response.get() == ButtonType.OK;
        if (isUserConfirmed) {
            deleteTechnicalResponsibleProcess(selectedTechnical.getIdTechnicalSupervisor());
        }
    }

    private void configureListeners() {
        technicalResponsibleTableView.getSelectionModel().selectedItemProperty()
                .addListener(new TechnicalSelectionListener());
    }

    private final class TechnicalSelectionListener implements ChangeListener<TechnicalSupervisor> {
        @Override
        public void changed(ObservableValue<? extends TechnicalSupervisor> observable,
                            TechnicalSupervisor oldValue, TechnicalSupervisor newValue) {
            selectedTechnical = newValue;
        }
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
                technicalResponsibleTableView.getItems().setAll(technicalList);
            }

        } catch (ServiceException serviceException) {
            showAlert("Error de servicio",
                    "No se pudieron cargar los técnicos responsables.",
                    Alert.AlertType.ERROR);
        }
    }

    private void deleteTechnicalResponsibleProcess(int idTechnicalSupervisor) {
        try {
            TechnicalResponsibleDAO technicalResponsibleDAO = new TechnicalResponsibleDAO();
            technicalResponsibleDAO.deleteWithOrganizationValidation(idTechnicalSupervisor);
            selectedTechnical = null;
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
