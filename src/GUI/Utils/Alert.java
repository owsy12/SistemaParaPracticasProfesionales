package GUI.Utils;

import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Alert {
    public static void showAlert(String title, String message, javafx.scene.control.Alert.AlertType type) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

    }

    public static Optional<ButtonType> showAlertAndWait(String title, String message, javafx.scene.control.Alert.AlertType type) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        return alert.showAndWait();
    }
}
