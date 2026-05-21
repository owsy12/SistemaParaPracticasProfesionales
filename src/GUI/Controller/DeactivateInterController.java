package GUI.Controller;

import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;

public class DeactivateInterController {
    @FXML
    private TableView<User> internsTabeView;
    @FXML
    private TableColumn<User, Void> actionColumn;
    @FXML
    private TableColumn<User, String> tagColumn;
    @FXML
    private TableColumn<User, String> interFullNameColumn;

    @FXML
    private void initialize(){
        configureTable();
    }

    private void configureTable(){
        loadActiveInterns();
        configureDataColumns();
        addButtonToRow();
    }

    private void loadActiveInterns(){
        try{

            List<Intern> internList;
            InternDAO internDAO = new InternDAO();
            internList = internDAO.findAllActiveinterns();
            internsTabeView.getItems().setAll(internList);
        } catch (ServiceException serviceException) {
            showAlert("Error", "Error enservicio intenten nuemaete mas tarde",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logro recuperar",
                    Alert.AlertType.ERROR);
        }
    }

    private void configureDataColumns(){
        tagColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMatricula()));

        interFullNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFirstName() +
                        cellData.getValue().getLastName() + cellData.getValue().getSecondLastName()));
    }

    private void addButtonToRow(){
        actionColumn.setCellFactory(column -> new javafx.scene.control.TableCell<User, Void>(){
            private final Button button = new Button("Inactivar");

            {
                button.setOnAction(event ->{
                    User user = getTableView().getItems().get(getIndex());
                    showAlertAndWait("Advertencia", "seurrp que desea inactivar este practicante",
                            Alert.AlertType.CONFIRMATION).ifPresent(eventConfirmation -> {

                                if (eventConfirmation == ButtonType.OK){
                                    inactiveProcess(user);
                                    internsTabeView.refresh();
                                }

                    });

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
        });
    }

    private void inactiveProcess(User user){
        try {
            InternDAO internDAO = new InternDAO();
            internDAO.deactivateIntern(user.getId());
        } catch (ServiceException serviceException) {
            showAlert("Error", "Servicio no disponible por el momento intenre mas tarde",
                    Alert.AlertType.ERROR);
        } catch (ValidationException validationException) {
            showAlert("Error", "No se logro comporbar al practivante",
                    Alert.AlertType.ERROR);
        }
    }

}
