package Logic.Exceptions;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReferentialIntegrityException extends ServiceException {

    private static final Logger LOGGER =
            Logger.getLogger(ReferentialIntegrityException.class.getName());
    public static final int MYSQL_FOREIGN_KEY_VIOLATION = 1451;

    public ReferentialIntegrityException(String message, SQLException cause) {
        super(message, cause);
        LOGGER.log(Level.WARNING, "Violación de integridad referencial detectada: {0}", message);
    }

    public static boolean isForeignKeyViolation(SQLException sqlException) {
        return sqlException.getErrorCode() == MYSQL_FOREIGN_KEY_VIOLATION;
    }
}
