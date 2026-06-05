package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DTOs.User;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ViewsUtils.wrapInScrollableContent;

public class MainMenuController implements EventHandler<ActionEvent> {

    private static final int SIDEBAR_EXPANDED_WIDTH = 280;
    private static final int SIDEBAR_COLLAPSED_WIDTH = 64;
    private static final String ARROW_EXPANDED = "‹";
    private static final String ARROW_COLLAPSED = "›";

    private static final String[] DAYS_ES = {
            "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
    private static final String[] MONTHS_ES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

    @FXML
    private VBox menuVBox;

    @FXML
    private StackPane contentPane;

    @FXML
    private VBox sidebarContainerVBox;

    @FXML
    private VBox sidebarLogoTextVBox;

    @FXML
    private VBox sidebarUserProfileVBox;

    @FXML
    private Label menuSectionTitleLabel;

    @FXML
    private Label userInitialsLabel;

    @FXML
    private Label userFullNameLabel;

    @FXML
    private Label userMatriculaLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Label collapseMenuTextLabel;

    @FXML
    private Label collapseArrowIconLabel;

    @FXML
    private Label headerCurrentDateLabel;

    @FXML
    private Label headerPeriodLabel;

    private User currentUser;
    private boolean sidebarCollapsed = false;

    @FXML
    private void initialize() {
        updateHeaderDate();
        loadView("/GUI/View/GUIWelcome.fxml");
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserProfile();
        loadMenuByRole();
    }

    private void updateHeaderDate() {
        LocalDate today = LocalDate.now();
        headerCurrentDateLabel.setText(formatDate(today));
        headerPeriodLabel.setText(buildPeriodLabel(today));
    }

    private String formatDate(LocalDate date) {
        String dayAbbreviation = DAYS_ES[date.getDayOfWeek().getValue() - 1];
        String monthAbbreviation = MONTHS_ES[date.getMonthValue() - 1];
        String formattedDate = dayAbbreviation + " · " + date.getDayOfMonth() + " " + monthAbbreviation + " " + date.getYear();
        return formattedDate;
    }

    private String buildPeriodLabel(LocalDate date) {
        int month = date.getMonthValue();
        int year = date.getYear();
        boolean isSpringPeriod = month >= 2 && month <= 7;
        String result;
        if (isSpringPeriod) {
            result = "Periodo Feb-Jul " + year;
        } else {
            boolean isLaterInYear = month >= 8;
            int startYear = isLaterInYear ? year : year - 1;
            result = "Periodo Ago-Ene " + startYear;
        }
        return result;
    }

    private void updateUserProfile() {
        String initials = extractInitials(currentUser.getFirstName(), currentUser.getLastName());
        String fullName = buildFullName(currentUser);
        String activeRole = resolveActiveRole(currentUser);

        userInitialsLabel.setText(initials.toUpperCase());
        userFullNameLabel.setText(fullName.toUpperCase());
        userMatriculaLabel.setText(currentUser.getMatricula());
        userRoleLabel.setText(activeRole);
    }

    private String extractInitials(String firstName, String lastName) {
        boolean isFirstNameEmpty = firstName == null || firstName.isEmpty();
        boolean isLastNameEmpty = lastName == null || lastName.isEmpty();
        String first = isFirstNameEmpty ? "" : String.valueOf(firstName.charAt(0));
        String last = isLastNameEmpty ? "" : String.valueOf(lastName.charAt(0));
        String initials = first + last;
        return initials;
    }

    private String buildFullName(User user) {
        String second = "";
        if (user.getSecondLastName() != null) {
            second = " " + user.getSecondLastName();
        }
        String fullName = user.getFirstName() + " " + user.getLastName() + second;
        return fullName;
    }

    private String resolveActiveRole(User user) {
        String result = "SIN ROL";
        boolean hasRoles = user.getRoles() != null && !user.getRoles().isEmpty();
        if (hasRoles) {
            result = user.getRoles().get(0).toUpperCase();
        }
        return result;
    }

    public void loadMenuByRole() {
        menuVBox.getChildren().clear();

        for (String role : currentUser.getRoles()) {
            switch (role) {
                case "Administrador":
                    loadAdministratorActions();
                    break;
                case "Profesor":
                    loadProfesorActions();
                    break;
                case "Coordinador":
                    loadCoordinadorActions();
                    break;
                case "Practicante":
                    loadInternActions();
                    break;
                default:
                    break;
            }
        }
    }

    private void addButton(String text, String fxmlPath) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setUserData(fxmlPath);
        button.setOnAction(this);
        menuVBox.getChildren().add(button);
    }

    @Override
    public void handle(ActionEvent event) {
        Button source = (Button) event.getSource();
        String fxmlPath = (String) source.getUserData();
        loadView(fxmlPath);
    }

    private void loadAdministratorActions() {
        addButton("Registrar coordinador", "/GUI/View/GUIAddCoordinador.fxml");
        addButton("Registrar profesor", "/GUI/View/GUIAddProfesor.fxml");
        addButton("Inactivar coordinador", "/GUI/View/GUIDeactivateCoordinator.fxml");

    }

    private void loadProfesorActions() {
        addButton("Evaluar Reporte", "/GUI/View/GUIEvaluateReport.fxml");
        addButton("Manejar Actividades", "/GUI/view/GUISelectProjectForActivity.fxml");
    }

    private void loadCoordinadorActions() {
        addButton("Registrar Experencia Educativa", "/GUI/View/GUIAddEducationalExperience.fxml");
        addButton("Registrar Organización", "/GUI/View/GUIAddLinkedOrganization.fxml");
        addButton("Registrar Practicante", "/GUI/View/GUIAddIntern.fxml");
        addButton("Asignar Proyecto", "/GUI/View/GUIAssignProject.fxml");
        addButton("Registrar Técnico", "/GUI/View/GUIAddTechnicalResponsible.fxml");
        addButton("Registrar Proyecto", "/GUI/View/GUIAddProject.fxml");
        addButton("Actualizar Proyecto", "/GUI/View/GUIManageProject.fxml");
        addButton("Inactivar Practicante", "/GUI/View/GUIDeactivateIntern.fxml");
        addButton("Consultar Organizaciones Vinculadas", "/GUI/view/GUIManageLinkedOrganization.fxml");
        addButton("Consultar técnicos responsables", "/GUI/view/GUIManageTechnicalResponsible.fxml");
        addButton("Registrar profesor", "/GUI/View/GUIAddProfesor.fxml");
        addButton("Inactivar profesor", "/GUI/View/GUIDeactivateProfessor.fxml");
    }

    private void loadInternActions() {
        addButton("Subir Documentos Iniciales", "/GUI/View/GUIUploadInitialDocuments.fxml");
        addButton("Solicitar Proyecto", "/GUI/View/GUIRequestProject.fxml");
        addButton("Generar Reporte", "/GUI/View/GUIGenerateReport.fxml");
        addButton("Añadir Reporte", "/GUI/View/GUIAddRerport.fxml");
        addButton("Generar Autoevaluación", "/GUI/View/GUIGenerateSelfEvaluation.fxml");
        addButton("Añadir Autoevaluación", "/GUI/View/GUIAddSerlEvaluation.fxml");
        addButton("Añadir Evaluación OV", "/GUI/View/GUIAddOVEvaluation.fxml");
        addButton("Retroalimentación", "/GUI/View/GUIInternFeedback.fxml");
    }

    @FXML
    private void handleCollapseMenu() {
        sidebarCollapsed = !sidebarCollapsed;
        applySidebarState();
    }

    private void applySidebarState() {
        int targetWidth = sidebarCollapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_EXPANDED_WIDTH;
        String arrowText = sidebarCollapsed ? ARROW_COLLAPSED : ARROW_EXPANDED;

        sidebarContainerVBox.setMinWidth(targetWidth);
        sidebarContainerVBox.setPrefWidth(targetWidth);
        sidebarContainerVBox.setMaxWidth(targetWidth);
        sidebarLogoTextVBox.setVisible(!sidebarCollapsed);
        sidebarLogoTextVBox.setManaged(!sidebarCollapsed);
        sidebarUserProfileVBox.setVisible(!sidebarCollapsed);
        sidebarUserProfileVBox.setManaged(!sidebarCollapsed);
        menuSectionTitleLabel.setVisible(!sidebarCollapsed);
        menuSectionTitleLabel.setManaged(!sidebarCollapsed);
        collapseMenuTextLabel.setVisible(!sidebarCollapsed);
        collapseMenuTextLabel.setManaged(!sidebarCollapsed);
        collapseArrowIconLabel.setText(arrowText);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        openLoginView();
    }

    private void openLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/View/GUILogin.fxml"));
            Parent loginRoot = loader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.setTitle("Inicio de Sesión — SGPP");
            loginStage.show();
            Stage currentStage = (Stage) contentPane.getScene().getWindow();
            currentStage.close();
        } catch (IOException ioException) {
            showAlert("Error", "No se pudo cerrar sesión correctamente.", Alert.AlertType.ERROR);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent vista = loader.load();
            contentPane.getChildren().setAll(wrapInScrollableContent(vista));
        } catch (IOException ioException) {
            showAlert("Error", "Error al cargar la vista.", Alert.AlertType.ERROR);
        }
    }

}