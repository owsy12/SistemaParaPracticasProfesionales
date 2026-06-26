package Logic.Exceptions;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceException extends Exception {

    private static final Logger LOGGER = Logger.getLogger(ServiceException.class.getName());

    public ServiceException(String message, SQLException cause) {
        super(message, cause);
        LOGGER.log(Level.SEVERE, "Database error: {0} | Cause: {1}",
                new Object[]{message, cause.getMessage()});
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        LOGGER.log(Level.SEVERE, "Database error: {0} | Cause: {1}",
                new Object[]{message, cause.getMessage()});
    }
}
