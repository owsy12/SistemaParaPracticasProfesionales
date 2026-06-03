package Logic.Exceptions;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;



public class DuplicateEntryException extends ServiceException {

    private static final Logger LOGGER = Logger.getLogger(DuplicateEntryException.class.getName());
    public static final int MYSQL_DUPLICATE_ENTRY = 1062;
    public DuplicateEntryException(String message, SQLException cause) {
        super(message, cause);
        LOGGER.log(Level.WARNING, "Entrada duplicada detectada: {0}", message);
    }
    public static boolean isDuplicateEntry(SQLException sqlException) {
        return sqlException.getErrorCode() == MYSQL_DUPLICATE_ENTRY;
    }

    public boolean isEmailDuplicated() {
        boolean emailDuplicated = false;
        Throwable cause = getCause();
        if (cause != null && cause.getMessage() != null) {
            String causeMessage = cause.getMessage().toLowerCase();
            emailDuplicated = causeMessage.contains("correo");
        }
        return emailDuplicated;
    }
}
