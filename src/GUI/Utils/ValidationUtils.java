package GUI.Utils;

import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^[a-zA-Z0-9@#$%^&*!?_\\-]*$");



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
                case "Password":
                    if (nuevoTexto.matches(PASSWORD_PATTERN.pattern()) && nuevoTexto.length() <= 20) {
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

    public static boolean isPDF(File file) {

        try {

            String mimeType =
                    Files.probeContentType(
                            file.toPath()
                    );

            return mimeType != null &&
                    mimeType.equals(
                            "application/pdf"
                    );

        } catch (IOException e) {

            return false;
        }
    }

    public static boolean isValidPassword(String password) {

        boolean isValid = true;
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasNumber = false;
        boolean hasSpecialCharacter = false;
        final int MIN_LENGTH = 8;

        if (password == null || password.length() < MIN_LENGTH) {

            isValid = false;

        } else {

            for (int i = 0; i < password.length(); i++) {

                char character = password.charAt(i);

                if (Character.isUpperCase(character)) {
                    hasUppercase = true;
                }

                if (Character.isLowerCase(character)) {
                    hasLowercase = true;
                }

                if (Character.isDigit(character)) {
                    hasNumber = true;
                }

                if (!Character.isLetterOrDigit(character)) {
                    hasSpecialCharacter = true;
                }

                if (i < password.length() - 2) {

                    char current = password.charAt(i);
                    char next = password.charAt(i + 1);
                    char nextNext = password.charAt(i + 2);

                    if (Character.isDigit(current) &&
                            Character.isDigit(next) &&
                            Character.isDigit(nextNext)) {

                        if ((next == current + 1) &&
                                (nextNext == next + 1)) {

                            isValid = false;

                        }

                    }

                }

            }

            if (!hasUppercase ||
                    !hasLowercase ||
                    !hasNumber ||
                    !hasSpecialCharacter) {

                isValid = false;

            }

        }

        return isValid;
    }

}
