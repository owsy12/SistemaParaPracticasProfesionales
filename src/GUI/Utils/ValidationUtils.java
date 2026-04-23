package GUI.Utils;

import javafx.scene.control.TextField;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public static void  setTypeAndLenght(TextField campo, String regex, int limite) {
        campo.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String nuevoTexto = change.getControlNewText();

            if (nuevoTexto.matches(regex) && nuevoTexto.length() <= limite) {
                return change;
            }
            return null;
        }));
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }



}
