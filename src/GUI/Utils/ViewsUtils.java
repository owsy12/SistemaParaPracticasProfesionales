package GUI.Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

import static GUI.Utils.Alert.showAlert;

public class ViewsUtils {
    public static void openWelcomePage(AnchorPane anchorPane){
        try {
            FXMLLoader loader = new FXMLLoader(ViewsUtils.class.getResource("/GUI/view/GUIWelcome.fxml"));
            Parent vista = loader.load();
            anchorPane.getChildren().setAll(vista);
        } catch (IOException e) {
            showAlert("Error", "No se logro cargar",
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }


}
