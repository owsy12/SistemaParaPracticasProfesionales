package GUI.Utils;

import javafx.scene.control.TextField;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern VALID_EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern ID_PATTERN =
            Pattern.compile("^[a-zA-Z0-9]*$");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9@._%+\\-]*$");

    private static final Pattern TEXT_PATTERN =
            Pattern.compile("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]*$");

    private static final Pattern NUMBER_PARTTERN =
            Pattern.compile("^[0-9]*$");



    public static void setTypeAndLength(TextField campo, String type) {
        campo.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String nuevoTexto = change.getControlNewText();
            boolean isValid = false;

            switch (type) {
                case "ID":
                    if (nuevoTexto.matches(ID_PATTERN.pattern()) && nuevoTexto.length() <= 10) {
                        isValid = true;
                    }
                    break;
                case "Name":
                    if (nuevoTexto.matches(TEXT_PATTERN.pattern()) && nuevoTexto.length() <= 30) {
                        isValid = true;
                    }
                    break;
                case "Email":
                    if (nuevoTexto.matches(EMAIL_PATTERN.pattern()) && nuevoTexto.length() <= 50) {
                        isValid = true;
                    }
                    break;
                case "Text":
                    if (nuevoTexto.matches(TEXT_PATTERN.pattern()) && nuevoTexto.length() <= 45) {
                        isValid = true;
                    }
                    break;
                case "Number":
                    if (nuevoTexto.matches(NUMBER_PARTTERN.pattern()) && nuevoTexto.length() <= 8){
                        isValid = true;
                    }
                    break;
                default:
                    isValid = false;
            }
            if (!isValid) {
              change = null;
            }
            return change;
        }));
    }

    public static boolean isValidEmail(String email) {
        return email != null && VALID_EMAIL_PATTERN.matcher(email).matches();
    }



}
