import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Clase principal de la aplicación JavaFX
 * Carga la interfaz gráfica desde el archivo FXML
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Cargar el archivo FXML
            URL fxmlLocation = getClass().getResource("/GUI/View/FXMLExample.fxml");
            
            if (fxmlLocation == null) {
                throw new RuntimeException("No se encontró el archivo FXML en: /GUI/View/FXMLExample.fxml");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            AnchorPane root = loader.load();
            
            // Crear la escena
            Scene scene = new Scene(root);
            
            // Configurar el escenario
            primaryStage.setTitle("Sistema para Prácticas Profesionales");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error al cargar la interfaz: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al cargar la aplicación", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
