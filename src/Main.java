import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            URL fxmlLocation = getClass().getResource("/GUI/View/GUILogin.fxml");
            
            if (fxmlLocation == null) {
                throw new RuntimeException("No se encontró el archivo FXML en: /GUI/View/GUILogin.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            AnchorPane root = loader.load();
            
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("Sistema para Prácticas Profesionales - Registrar Coordinador");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new RuntimeException("Error al cargar la aplicación", exception);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


