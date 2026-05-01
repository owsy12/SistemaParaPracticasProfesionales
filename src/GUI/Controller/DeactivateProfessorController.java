package GUI.Controller;

import Logic.DAO.ProfessorDAO;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.Professor;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.Alert.showAlertAndWait;

public class DeactivateProfessorController {
    @FXML
    private TableColumn<User,String> nameColumn;
    @FXML
    private TableColumn<User,Void> actionsColumn;
    @FXML
    private TableColumn<User,String> secondLastNameColumn;
    @FXML
    private TableView tableView;
    @FXML
    private TableColumn<Professor,String>academicDegreeColumn;
    @FXML
    private TableColumn<User,String> lastNameColumn;
    @FXML
    private TableColumn<User,String> matriculaColumn;

    @FXML
    private void initialize() {
        matriculaColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMatricula()));

        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastName()));

        lastNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastName()));

        secondLastNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSecondLastName()));
        academicDegreeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAcademicArea()));
        loadProfessors();
        addButtonToTable();
    }

    private void addButtonToTable() {
        actionsColumn.setCellFactory(param -> new javafx.scene.control.TableCell<User, Void>() {

            private final javafx.scene.control.Button btn = new javafx.scene.control.Button("Inactivar");

            {
                btn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    showAlertAndWait("Desea desactivar ","desae", javafx.scene.control.Alert.AlertType.CONFIRMATION).ifPresent(response -> {

                        if (response == javafx.scene.control.ButtonType.OK) {
                            user.setStatus("Inactivo");
                            user.setRole("Profesor");
                            deactivateProcess(user);
                            loadProfessors();
                        }

                    });;

                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void deactivateProcess(User user){
        try{
            UserRoleDAO userRoleDAO = new UserRoleDAO();
            userRoleDAO.updateUserRolStatus(user);
            showAlert("Profesor desactivado", "El profesor ha sido desactivado exitosamente.",
                    javafx.scene.control.Alert.AlertType.INFORMATION);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadProfessors(){
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            tableView.getItems().setAll(professorDAO.findActiveProfessors());
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
