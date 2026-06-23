package DataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.github.cdimascio.dotenv.Dotenv;

public class DataBaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DataBaseConnection.class.getName());

    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private static final String DB_URL = get("DB_URL");
    private static final String DB_USER = get("DB_USER");
    private static final String DB_PASSWORD = get("DB_PASSWORD");
    private static final String DB_DRIVER = get("DB_DRIVER");

    private DataBaseConnection() {
    }

    public static String get(String key) {
        return dotenv.get(key);
    }

    public static Connection connectDatabase() throws SQLException {
        validateConnectionParameters();
        loadDriver();

        Connection connection;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "No se pudo conectar a la base de datos en {0}: {1}",
                    new Object[]{DB_URL, sqlException.getMessage()});
            throw new SQLException("No se pudo establecer la conexión con la base de datos. "
                    + "Verifique DB_URL y que el servidor MySQL esté disponible.", sqlException);
        }
        return connection;
    }

    private static void loadDriver() throws SQLException {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException classNotFoundException) {
            LOGGER.log(Level.SEVERE, "Driver MySQL no encontrado ({0}): {1}",
                    new Object[]{DB_DRIVER, classNotFoundException.getMessage()});
            throw new SQLException("Driver MySQL no encontrado. Verifique DB_DRIVER y que el "
                    + "conector de MySQL esté en el classpath.", classNotFoundException);
        }
    }

    private static void validateConnectionParameters() throws SQLException {
        if (DB_URL == null || DB_URL.isBlank()) {
            LOGGER.log(Level.SEVERE, "Variable de entorno DB_URL no configurada.");
            throw new SQLException("La variable de entorno DB_URL no está configurada. "
                    + "Verifique que el archivo .env exista en el directorio de trabajo.");
        }
        if (DB_USER == null || DB_USER.isBlank()) {
            LOGGER.log(Level.SEVERE, "Variable de entorno DB_USER no configurada.");
            throw new SQLException("La variable de entorno DB_USER no está configurada.");
        }
        if (DB_PASSWORD == null) {
            LOGGER.log(Level.SEVERE, "Variable de entorno DB_PASSWORD no configurada.");
            throw new SQLException("La variable de entorno DB_PASSWORD no está configurada.");
        }
        if (DB_DRIVER == null || DB_DRIVER.isBlank()) {
            LOGGER.log(Level.SEVERE, "Variable de entorno DB_DRIVER no configurada.");
            throw new SQLException("La variable de entorno DB_DRIVER no está configurada.");
        }
    }
}
