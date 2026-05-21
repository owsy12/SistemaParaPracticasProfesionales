package DataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.cdimascio.dotenv.Dotenv;

public class DataBaseConnection {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String DB_URL = get("DB_URL");
    private static final String DB_USER = get("DB_USER");
    private static final String DB_PASSWORD = get("DB_PASSWORD");
    private static final Logger LOGGER = Logger.getLogger(DataBaseConnection.class.getName());

    private DataBaseConnection() {
    }

    public static String get(String key) {
        return dotenv.get(key);
    }

    public static Connection connectDatabase() throws SQLException {
        validateConnectionParameters();

        try {
            Class.forName(get("DB_DRIVER"));
        } catch (ClassNotFoundException classNotFoundException) {
            LOGGER.log(Level.SEVERE, "Driver MySQL no encontrado: {0}",
                    classNotFoundException.getMessage());
            throw new SQLException("Driver MySQL no encontrado.", classNotFoundException);
        }

        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void validateConnectionParameters() throws SQLException {
        if (DB_URL == null || DB_URL.isBlank()) {
            LOGGER.log(Level.SEVERE, "Variable de entorno DB_URL no configurada.");
            throw new SQLException("La variable de entorno DB_URL no está configurada.");
        }
        if (DB_USER == null || DB_USER.isBlank()) {
            LOGGER.log(Level.SEVERE, "Variable de entorno DB_USER no configurada.");
            throw new SQLException("La variable de entorno DB_USER no está configurada.");
        }
        if (DB_PASSWORD == null) {
            LOGGER.log(Level.SEVERE, "Variable de entorno DB_PASSWORD no configurada.");
            throw new SQLException("La variable de entorno DB_PASSWORD no está configurada.");
        }
    }
}
