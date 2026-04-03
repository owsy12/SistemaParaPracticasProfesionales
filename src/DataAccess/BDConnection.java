package DataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BDConnection {

    private static final String DB_URL      = "jdbc:mysql://localhost:3306/spp" +
            "?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER     = "app_user";
    private static final String DB_PASSWORD = "AppPass123!";
    private static final Logger LOGGER      = Logger.getLogger(BDConnection.class.getName());

    private BDConnection() {}

    public static Connection connectDatabase() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException classNotFoundException) {
            LOGGER.log(Level.SEVERE, "Driver MySQL no encontrado: {0}",
                    classNotFoundException.getMessage());
            throw new SQLException("Driver MySQL no encontrado", classNotFoundException);
        }

        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}