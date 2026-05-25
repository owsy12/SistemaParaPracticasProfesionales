package GUI.Utils;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.UnaryOperator;
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
    private static final int PASSWORD_MIN_LENGTH = 8;



    public static void setTypeAndLength(TextField campo, String type) {
        UnaryOperator<TextFormatter.Change> filter =
                new UnaryOperator<TextFormatter.Change>() {
            @Override
            public TextFormatter.Change apply(TextFormatter.Change change) {
                String nuevoTexto = change.getControlNewText();
                boolean isValid = false;

                switch (type) {
                    case "ID":
                        boolean isIdMatch =
                                nuevoTexto.matches(ID_PATTERN.pattern());
                        boolean isIdLength =
                                nuevoTexto.length() <= 10;
                        if (isIdMatch && isIdLength) {
                            isValid = true;
                        }
                        break;
                    case "Name":
                        boolean isNameMatch =
                                nuevoTexto.matches(TEXT_PATTERN.pattern());
                        boolean isNameLength =
                                nuevoTexto.length() <= 30;
                        if (isNameMatch && isNameLength) {
                            isValid = true;
                        }
                        break;
                    case "Email":
                        boolean isEmailMatch =
                                nuevoTexto.matches(EMAIL_PATTERN.pattern());
                        boolean isEmailLength =
                                nuevoTexto.length() <= 50;
                        if (isEmailMatch && isEmailLength) {
                            isValid = true;
                        }
                        break;
                    case "Text":
                        boolean isTextMatch =
                                nuevoTexto.matches(TEXT_PATTERN.pattern());
                        boolean isTextLength =
                                nuevoTexto.length() <= 45;
                        if (isTextMatch && isTextLength) {
                            isValid = true;
                        }
                        break;
                    case "Number":
                        boolean isNumberMatch =
                                nuevoTexto.matches(NUMBER_PARTTERN.pattern());
                        boolean isNumberLength =
                                nuevoTexto.length() <= 8;
                        if (isNumberMatch && isNumberLength) {
                            isValid = true;
                        }
                        break;
                    case "Password":
                        boolean isPasswordMatch =
                                nuevoTexto.matches(PASSWORD_PATTERN.pattern());
                        boolean isPasswordLength =
                                nuevoTexto.length() <= 20;
                        if (isPasswordMatch && isPasswordLength) {
                            isValid = true;
                        }
                        break;
                    default:
                        isValid = false;
                }

                TextFormatter.Change result = change;
                if (!isValid) {
                    result = null;
                }
                return result;
            }
        };
        campo.setTextFormatter(new TextFormatter<>(filter));
    }

    public static boolean isValidEmail(String email) {
        boolean valid = email != null && VALID_EMAIL_PATTERN.matcher(email).matches();
        return valid;
    }

    public static boolean isPDF(File file) {
        boolean isPdf = false;
        try {
            String mimeType = Files.probeContentType(file.toPath());
            isPdf = mimeType != null && mimeType.equals("application/pdf");
        } catch (IOException ioException) {
            isPdf = false;
        }
        return isPdf;
    }

    public static String getPasswordValidationMessage(String password) {
        String message = null;
        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            message = "La contraseña debe tener al menos 8 caracteres.";
        } else if (!containsUppercase(password)) {
            message = "La contraseña debe contener al menos una letra mayúscula.";
        } else if (!containsLowercase(password)) {
            message = "La contraseña debe contener al menos una letra minúscula.";
        } else if (!containsDigit(password)) {
            message = "La contraseña debe contener al menos un número.";
        } else if (!containsSpecialChar(password)) {
            message = "La contraseña debe contener al menos un carácter especial.";
        }
        return message;
    }

    private static boolean containsUppercase(String password) {
        boolean found = false;
        for (int i = 0; i < password.length(); i++) {
            if (Character.isUpperCase(password.charAt(i))) {
                found = true;
            }
        }
        return found;
    }

    private static boolean containsLowercase(String password) {
        boolean found = false;
        for (int i = 0; i < password.length(); i++) {
            if (Character.isLowerCase(password.charAt(i))) {
                found = true;
            }
        }
        return found;
    }

    private static boolean containsDigit(String password) {
        boolean found = false;
        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.charAt(i))) {
                found = true;
            }
        }
        return found;
    }

    private static boolean containsSpecialChar(String password) {
        boolean found = false;
        for (int i = 0; i < password.length(); i++) {
            if (!Character.isLetterOrDigit(password.charAt(i))) {
                found = true;
            }
        }
        return found;
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
