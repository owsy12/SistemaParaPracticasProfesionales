package GUI.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainMenuController {
    public void openRegisterOrganization(ActionEvent actionEvent) {
        openWindow("GUIAddLinkedOrganization.fxml", "Registrar Organización Vinculada");
    }

    public void openRegisterTechnician(ActionEvent actionEvent) {
        openWindow("GUIAddTechnicalResponsible.fxml", "Registrar Responsable Técnico");
    }

    public void viewOrganizations(ActionEvent actionEvent) {
        openWindow("GUIAddCoordinador.fxml","Regsitrar coordinaodr");
    }

    public void exit(ActionEvent actionEvent) {
        openWindow("GUIAddProfesor.fxml","Registrar profesor");
    }

    private void openWindow(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/View/" + fxml));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
