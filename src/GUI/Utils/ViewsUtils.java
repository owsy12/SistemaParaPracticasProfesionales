package GUI.Utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.io.IOException;

import static GUI.Utils.Alert.showAlert;

public class ViewsUtils {

    public static final String CONTENT_PANE_ID = "contentPane";

    public static void openWelcomePage(AnchorPane anchorPane) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewsUtils.class.getResource("/GUI/View/GUIWelcome.fxml"));
            Parent welcomeView = loader.load();
            Pane contentPane = findContentPane(anchorPane);
            if (contentPane == null) {
                anchorPane.getChildren().setAll(welcomeView);
            } else {
                contentPane.getChildren().setAll(welcomeView);
            }
        } catch (IOException ioException) {
            showAlert("Error", "No se logro cargar",
                    javafx.scene.control.Alert.AlertType.ERROR);
        }
    }

    public static Pane findContentPane(Node startNode) {
        Pane contentPane = null;
        Node currentNode = startNode;
        while (currentNode != null && contentPane == null) {
            boolean isContentPane = currentNode instanceof StackPane
                    && CONTENT_PANE_ID.equals(currentNode.getId());
            if (isContentPane) {
                contentPane = (Pane) currentNode;
            }
            currentNode = currentNode.getParent();
        }
        return contentPane;
    }

    public static Node wrapInScrollableContent(Parent view) {
        Node wrappedView = view;
        boolean alreadyScrollable = view instanceof ScrollPane;
        if (!alreadyScrollable) {
            ScrollPane scrollPane = new ScrollPane(view);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(false);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            wrappedView = scrollPane;
        } else {
            ScrollPane scrollPane = (ScrollPane) view;
            scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
        if (wrappedView instanceof Region) {
            Region region = (Region) wrappedView;
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
        return wrappedView;
    }

}
