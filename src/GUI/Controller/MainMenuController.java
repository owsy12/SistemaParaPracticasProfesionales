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
        loadView("/GUI/View/GUIWelcome.fxml");
    }

    public void loadMenuByRole() {
        menuVBox.getChildren().clear();

       for (String role : currentUser.getRoles()) {

            switch (role) {
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
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> loadView(fxmlPath));
        menuVBox.getChildren().add(button);
    }

    private void loadAdminustratorAcction(){
        addButton("Registrar coordiandor", "/GUI/view/GUIAddCoordinador.fxml");
        addButton("Registrar profesor","/GUI/view/GUIAddProfesor.fxml");
        addButton("Inactivar coordiandor","/GUI/view/GUIDeactivateCoordinator.fxml");
        addButton("Inactivar profesor","/GUI/view/GUIDeactivateProfessor.fxml");
    }

    private void loadProfesorAcctions(){
        addButton("Evaluar Reporte", "/GUI/view/GUIEvaluateReport.fxml");
    }

    private void loadCoordinadorAcctions(){
        addButton("Registrar Organizacion","/GUI/view/GUIAddLinkedOrganization.fxml");
        addButton("Registrar Practicante","/GUI/view/GUIAddIntern.fxml");
        addButton("Asignar Proyecto","/GUI/view/GUIAssignProject.fxml");
        addButton("Registrar Tecnico","/GUI/view/GUIAddTechnicalResponsible.fxml");
        addButton("Registrar Proyecto","/GUI/view/GUIAddProject.fxml");
        addButton("Actualizar Proyecto","/GUI/view/GUIManageProject.fxml");
        addButton("Inactivar Practicante", "/GUI/view/GUIDeactivateIntern.fxml");
        addButton("Consultar Organizaciones Vinculadas", "/GUI/view/GUIManageLinkedOrganization.fxml");
        addButton("Consualtar tecnicos responsables", "/GUI/view/GUIManageTechnicalResponsible.fxml");
    }

    private void loadInternActions(){
        addButton("Subir Documentos Iniciales", "/GUI/view/GUIUploadInitialDocuments.fxml");
        addButton("Solicitar Proyecto", "/GUI/view/GUIRequestProject.fxml");
        addButton("Generar Reporte", "/GUI/view/GUIGenerateReport.fxml");
        addButton("Añadir Reporte", "/GUI/view/GUIAddReport.fxml");
        addButton("Generar Autoevaluacin", "/GUI/view/GUIGenerateSelfEvaluation.fxml");
        addButton("Añadir Autoevañuacion", "/GUI/view/GUIAddSelfEvaluation.fxml");
        addButton("Añadir evaluacion OV", "/GUI/view/GUIAddOVEvaluation.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent vista = loader.load();
            contentPane.getChildren().setAll(vista);
        } catch (IOException e) {
            showAlert("Error", "Error al cargar", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void setCurrentUser(User user){
            this.currentUser = user;
            loadMenuByRole();
    }

}
