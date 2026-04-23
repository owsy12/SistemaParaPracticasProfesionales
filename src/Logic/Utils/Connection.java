package Logic.Utils;

import DataAccess.DataBaseConnection;
import Logic.Exceptions.ServiceException;

import java.sql.SQLException;

public class Connection {
    public static java.sql.Connection connection;
    public static java.sql.Connection createdConnection () throws ServiceException {
        try {
            connection = DataBaseConnection.connectDatabase();
        }catch (SQLException sqlException) {
            throw new ServiceException("Error al conectar a la base de datos.", sqlException);
        }
        return connection;
    }
}
