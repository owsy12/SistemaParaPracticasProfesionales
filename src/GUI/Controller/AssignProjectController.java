package GUI.Controller;

import Logic.DAO.ApplicationDAO;
import Logic.DAO.UserDAO;
import Logic.DTOs.Application;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static GUI.Utils.Alert.showAlert;

public class AssignProjectController {
    @FXML
    public TableView internsTableView;
    public AnchorPane anchorPane;
    @FXML
    private TableColumn<User, Void> actionColumn;
    @FXML
    private TableColumn<User, String> fullNameColumn;
    @FXML
    private TableColumn<User, String> matriculaColumn;
    @FXML
    private void initialize(){
        verifyActiveInternProjectApplications();
    }

    @FXML
    public void outk(ActionEvent actionEvent) {
    }

    private void verifyActiveInternProjectApplications(){
        try {
            List<Application> applicationList;
            ApplicationDAO applicationDAO = new ApplicationDAO();
            applicationList = applicationDAO.findByStatus("Pendiente");

            if (applicationList.isEmpty()){
                showAlert("Advertenica", "En este momento no exiten solicitudes",
                        Alert.AlertType.INFORMATION);
            }else {
                configureTable(applicationList);
            }

        } catch (ServiceException e) {
            showAlert("Error", "Servicio no disponible intente mas tarde",
                    Alert.AlertType.ERROR);
        } catch (ValidationException e) {
            showAlert("Error", "Error al verificar practicantes con solicitudes pendientes",
                    Alert.AlertType.ERROR);
        }
    }

    private void configureTable(List<Application> applicationList){
        loadInternsOnTable(applicationList);
        configureUserDataColumns();
        addAssignButtonToRow();
    }

    private void loadInternsOnTable(List<Application> applicationList){
        List<User> userList = new ArrayList<>();

        try {
            UserDAO userDAO = new UserDAO();

            for (Application application : applicationList){
                userList.add(userDAO.findById(application.getIdIntern()));
            }
            internsTableView.getItems().setAll(userList);

        } catch (ServiceException e) {
            showAlert("Error", "Servicio no disponible",
                    Alert.AlertType.ERROR);
        } catch (ValidationException e) {
            showAlert("error", "no se logro cargar ",
                    Alert.AlertType.ERROR);
        }
    }

    private void configureUserDataColumns(){
        fullNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFirstName() +
                                            cellData.getValue().getLastName() +
                                            cellData.getValue().getSecondLastName()));

        matriculaColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMatricula()));
    }

    private void addAssignButtonToRow() {
        actionColumn.setCellFactory(param -> new javafx.scene.control.TableCell<User, Void>() {
            private final Button button = new Button("Asignar");

            {
                button.setOnAction(actionEvent -> {
                    User user = getTableView().getItems().get(getIndex());
                    openInternProjectSelection(user);
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

    private void openInternProjectSelection(User user){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/view/GUIViewInternProjectSelection.fxml"));
            Parent vista = loader.load();
            ViewInterProjectSelection controller = loader.getController();
            controller.setUser(user);
            anchorPane.getChildren().setAll(vista);
        } catch (IOException e) {
            showAlert("Error", "Np se logro cargar la vista",
                    Alert.AlertType.ERROR);
        }
    }
}
