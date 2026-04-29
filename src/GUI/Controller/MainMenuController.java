package GUI.Controller;

import GUI.Utils.ViewsUtils.*;
import Logic.DTOs.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class MainMenuController {
    @FXML
    private VBox menuVBox;
    @FXML
    private StackPane contentPane;

    private User currentUser;
    @FXML
    private void initialize() {
        loadView("/GUI/View/GUIWelcome.fxml");
    }

    public void loadMenuByRole() {
        menuVBox.getChildren().clear();

        if (currentUser.getRoles().size() > 1){
            loadAdminustratorAcction();
            loadProfesorAcctions();
        }else {
            switch (currentUser.getRoles().getFirst()) {
                case "Administrador":
                    loadAdminustratorAcction();
                    break;

                case "Profesor":
                    loadProfesorAcctions();
                    break;

                case "Coordinador":
                    loadCoordinadorAcctions();
                    break;

                case "Practicante":
                    loadInternActions();
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

    private void loadAdminustratorAcction(){
        addButton("Registrar coordiandor","/GUI/view/GUIAddCoordinador.fxml");
        addButton("Registrar profesor","/GUI/view/GUIAddProfesor.fxml");
        addButton("Inactivar coordiandor","");
        addButton("Inactivar profesor","");
    }

    private void loadProfesorAcctions(){
    }

    private void loadCoordinadorAcctions(){
        addButton("Registrar Organizacion","/GUI/view/GUIAddLinkedOrganization");

    }

    private void loadInternActions(){

    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent vista = loader.load();

            contentPane.getChildren().setAll(vista);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentUser(User user){
            this.currentUser = user;
            loadMenuByRole();
    }

}
