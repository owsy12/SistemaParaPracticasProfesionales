package Logic.Exceptions;

import java.sql.SQLException;

public class DataAccessException extends Exception {
    public DataAccessException(String message, SQLException rollbackEx) {
        super(message);
    }
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
