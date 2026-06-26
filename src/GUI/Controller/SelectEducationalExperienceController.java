package GUI.Controller;

import GUI.SessionManager.SessionManager;
import Logic.DAO.EducationalExperienceDAO;
import Logic.DTOs.EducationalExperience;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static GUI.Utils.Alert.showAlert;
import static GUI.Utils.ViewsUtils.findContentPane;
import static GUI.Utils.ViewsUtils.wrapInScrollableContent;

public class SelectEducationalExperienceController implements EventHandler<ActionEvent> {

    private static final Logger LOGGER =
            Logger.getLogger(SelectEducationalExperienceController.class.getName());

    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})");

    @FXML
    private FlowPane experienceCardsPane;

    @FXML
    private FlowPane historicalCardsPane;

    @FXML
    private VBox historicalSection;

    @FXML
    private Label emptyLabel;

    @FXML
    private void initialize() {
        loadEducationalExperiences();
    }

    @Override
    public void handle(ActionEvent event) {
        Button card = (Button) event.getSource();
        EducationalExperience experience = (EducationalExperience) card.getUserData();
        openInternView(experience);
    }

    private void loadEducationalExperiences() {
        try {
            int professorId = SessionManager.getInstance().getUser().getIdUser();
            EducationalExperienceDAO educationalExperienceDAO = new EducationalExperienceDAO();
            List<EducationalExperience> experiences =
                    educationalExperienceDAO.findByProfessor(professorId);

            experienceCardsPane.getChildren().clear();
            historicalCardsPane.getChildren().clear();
            int currentYear = LocalDate.now().getYear();
            boolean hasCurrent = false;
            boolean hasHistorical = false;

            for (EducationalExperience experience : experiences) {
                Button card = buildExperienceCard(experience);
                boolean isHistorical = isFromPreviousPeriod(experience, currentYear);
                if (isHistorical) {
                    historicalCardsPane.getChildren().add(card);
                    hasHistorical = true;
                } else {
                    experienceCardsPane.getChildren().add(card);
                    hasCurrent = true;
                }
            }

            if (hasCurrent) {
                emptyLabel.setText("");
            } else {
                emptyLabel.setText("No tienes experiencias educativas del período actual.");
            }

            historicalSection.setVisible(hasHistorical);
            historicalSection.setManaged(hasHistorical);
        } catch (ValidationException validationException) {
            showAlert("Error de validación",
                    validationException.getMessage(), Alert.AlertType.ERROR);
        } catch (ServiceException serviceException) {
            LOGGER.log(Level.SEVERE,
                    "Error loading educational experiences for professor: {0}",
                    serviceException.getMessage());
            showAlert("Servicio no disponible",
                    "No se pudieron cargar las experiencias educativas. Intente más tarde.",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean isFromPreviousPeriod(EducationalExperience experience, int currentYear) {
        int periodYear = 0;
        String period = experience.getPeriod();
        if (period != null) {
            Matcher matcher = YEAR_PATTERN.matcher(period);
            while (matcher.find()) {
                periodYear = Integer.parseInt(matcher.group(1));
            }
        }
        return periodYear > 0 && periodYear < currentYear;
    }

    private Button buildExperienceCard(EducationalExperience experience) {
        String cardText = "NRC " + experience.getNrc() + "\n"
                + experience.getName() + "\n"
                + experience.getPeriod();
        Button card = new Button(cardText);
        card.setUserData(experience);
        card.setOnAction(this);
        card.setWrapText(true);
        card.setPrefSize(240.0, 120.0);
        card.getStyleClass().add("primaryActionButton");
        return card;
    }

    private void openInternView(EducationalExperience experience) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/View/GUISelectionInternProject.fxml"));
            Parent view = loader.load();

            SelectionInternProjectController controller = loader.getController();
            controller.setReviewContext(experience);

            Pane contentPane = findContentPane(experienceCardsPane);
            if (contentPane == null) {
                showAlert("Error de navegación",
                        "No se pudo abrir la vista de practicantes.", Alert.AlertType.ERROR);
            } else {
                contentPane.getChildren().setAll(wrapInScrollableContent(view));
            }
        } catch (IOException ioException) {
            LOGGER.log(Level.SEVERE, "Error loading interns view: {0}",
                    ioException.getMessage());
            showAlert("Error de navegación",
                    "No se pudo abrir la vista de practicantes.", Alert.AlertType.ERROR);
        }
    }
}
