package GUI.Controller;

import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.TechnicalResponsible;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
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
    private TableView<TechnicalResponsible> technicalResponsibleTableView;

    @FXML
    private TableColumn<TechnicalResponsible, String> nameColumn;

    @FXML
    private TableColumn<TechnicalResponsible, String> lastNameColumn;

    @FXML
    private TableColumn<TechnicalResponsible, String> secondLastNameColumn;

    @FXML
    private TableColumn<TechnicalResponsible, String> emailColumn;

    @FXML
    private TableColumn<TechnicalResponsible, String> positionColumn;

    @FXML
    private void initialize() {
        loadTechnicalResponsibles();
    }

    @FXML
    public void cancelAction() {
        openWelcomePage(anchorPane);
    }

    @FXML
    public void deleteTechnicalResponsible(ActionEvent actionEvent) {
        TechnicalResponsible selectedTechnical = technicalResponsibleTableView.getSelectionModel().getSelectedItem();
        boolean isSelectionMissing = selectedTechnical == null;
        if (isSelectionMissing) {
            showAlert("Sin selección",
                    "Seleccione un técnico responsable de la tabla para eliminar.",
                    Alert.AlertType.WARNING);
        } else {

            String fullName = selectedTechnical.getName() + " " + selectedTechnical.getLastName();
            String confirmationMessage = "¿Desea eliminar al técnico responsable «"
                    + fullName + "»? Esta acción es irreversible.";
            Optional<ButtonType> response = showAlertAndWait(
                    "Confirmar eliminación", confirmationMessage, Alert.AlertType.CONFIRMATION);

            boolean isUserConfirmed = response.isPresent() && response.get() == ButtonType.OK;
            if (isUserConfirmed) {
                deleteTechnicalResponsibleProcess(selectedTechnical.getIdTechnicalResponsible());
            }
        }
    }

    private void loadTechnicalResponsibles() {
        try {
            TechnicalResponsibleDAO technicalResponsibleDao = new TechnicalResponsibleDAO();
            List<TechnicalResponsible> technicalList = technicalResponsibleDao.findAll();

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

    private void deleteTechnicalResponsibleProcess(int idTechnicalResponsible) {
        try {
            TechnicalResponsibleDAO technicalResponsibleDao = new TechnicalResponsibleDAO();
            technicalResponsibleDao.deleteWithOrganizationValidation(idTechnicalResponsible);
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
