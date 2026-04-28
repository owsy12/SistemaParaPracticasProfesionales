package GUI.Controller;

import GUI.Utils.ViewsUtils.*;
import Logic.DTOs.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

import static GUI.Utils.Alert.showAlert;

public class MainMenuController {
    @FXML
    private VBox menuVBox;
    @FXML
    private StackPane contentPane;

    private User currentUser;
    @FXML
    private void initialize() {

    }

    public void loadMenuByRole() {
        menuVBox.getChildren().clear();

        if (currentUser.getRoles().size() > 1){

        }else {
            switch (currentUser.getRoles().get(0)) {
                case "Administrador":
                    addButton("Usuarios", "/views/GUIusers.fxml");
                    break;

                case "Profesor":
                    addButton("Validar horas", "/views/validation.fxml");
                    break;

                case "ESTUDIANTE":
                    addButton("Mis actividades", "/views/activities.fxml");
                    break;
            }
        }

    }

    private void addButton(String text, String fxmlPath) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);

        btn.setOnAction(e -> loadView(fxmlPath));

        menuVBox.getChildren().add(btn);
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentUser(User user){
            this.currentUser = user;
            loadMenuByRole();
    }
}
