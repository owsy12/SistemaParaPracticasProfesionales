package Logic.Exceptions;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseException extends Exception {

    private static final Logger LOGGER = Logger.getLogger(DatabaseException.class.getName());

    public DatabaseException(String message, SQLException cause) {
        super(message, cause);
        LOGGER.log(Level.SEVERE, "Error en base de datos: {0} | Causa: {1}",
                new Object[]{message, cause.getMessage()});
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        LOGGER.log(Level.SEVERE, "Error en base de datos: {0} | Causa: {1}",
                new Object[]{message, cause.getMessage()});
    }
}
