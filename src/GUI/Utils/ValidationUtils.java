package GUI.Utils;

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

    private static final Pattern NUMBER_PATTERN =
            Pattern.compile("^[0-9]*$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^[a-zA-Z0-9@#$%^&*!?_\\-]*$");

    static final Pattern TEXTAREA_PATTERN =
            Pattern.compile("^[\\sa-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ.,;:!?()'\"\\-_%]*$");

    static final Pattern TEXTFIELD_PUNCT_PATTERN =
            Pattern.compile("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ .,;:!?()'\"\\-_%]*$");

    private static final int PASSWORD_MIN_LENGTH = 8;

    public static void setTypeAndLength(RestrictedTextField campo, String type) {
        int maxLength = resolveMaxLength(type);
        Pattern pattern = resolvePattern(type);
        campo.setRestriction(maxLength, pattern);
    }

    public static void setTypeAndLength(RestrictedPasswordField campo, String type) {
        int maxLength = resolveMaxLength(type);
        Pattern pattern = resolvePattern(type);
        campo.setRestriction(maxLength, pattern);
    }

    public static void limitTextField(RestrictedTextField textField, int maxLength) {
        textField.setRestriction(maxLength, null);
    }

    public static void limitTextArea(RestrictedTextArea textArea, int maxLength) {
        textArea.setRestriction(maxLength, null);
    }

    public static void applyTextAreaRestriction(RestrictedTextArea textArea, int maxLength) {
        textArea.setRestriction(maxLength, TEXTAREA_PATTERN);
    }

    public static void applyTextFieldRestriction(RestrictedTextField textField, int maxLength) {
        textField.setRestriction(maxLength, TEXTFIELD_PUNCT_PATTERN);
    }

    public static boolean isAcceptedInput(String text, int maxLength, Pattern pattern) {
        boolean withinLength = text.length() <= maxLength;
        boolean matchesPattern = pattern == null || pattern.matcher(text).matches();
        boolean accepted = withinLength && matchesPattern;
        return accepted;
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

    private static int resolveMaxLength(String type) {
        int length;
        switch (type) {
            case "ID":
                length = 10;
                break;
            case "Name":
                length = 30;
                break;
            case "Email":
                length = 50;
                break;
            case "Text":
                length = 45;
                break;
            case "Number":
                length = 8;
                break;
            case "Password":
                length = 20;
                break;
            default:
                length = 45;
        }
        return length;
    }

    private static Pattern resolvePattern(String type) {
        Pattern pattern;
        switch (type) {
            case "ID":
                pattern = ID_PATTERN;
                break;
            case "Email":
                pattern = EMAIL_PATTERN;
                break;
            case "Number":
                pattern = NUMBER_PATTERN;
                break;
            case "Password":
                pattern = PASSWORD_PATTERN;
                break;
            case "Name":
            case "Text":
            default:
                pattern = TEXT_PATTERN;
        }
        return pattern;
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
}
