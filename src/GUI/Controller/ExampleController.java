package GUI.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

/**
 * Controlador para la vista de ejemplo
 * Maneja los eventos y la lógica de la interfaz gráfica
 */
public class ExampleController {

    @FXML
    private AnchorPane rootPane;

    /**
     * Inicializa el controlador después de que el FXML ha sido cargado
     */
    @FXML
    public void initialize() {
        System.out.println("ExampleController inicializado correctamente");
        // Aquí puedes agregar código de inicialización
    }
}
