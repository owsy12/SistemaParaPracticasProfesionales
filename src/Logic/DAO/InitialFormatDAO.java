package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.InitialFormat;
import Logic.Exceptions.DataAccessException;
import Logic.Interface.IInitialFormatDAO;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;

public class InitialFormatDAO implements IInitialFormatDAO {

    private static final String SQL_INSERT =
            "INSERT INTO formato_inicial " +
                    "(id_practicante, tipo_formato, ruta_archivo, estado, fecha_entrega) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_formato, id_practicante, tipo_formato, " +
                    "       ruta_archivo, estado, fecha_entrega " +
                    "FROM formato_inicial " +
                    "WHERE id_formato = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT id_formato, id_practicante, tipo_formato, " +
                    "       ruta_archivo, estado, fecha_entrega " +
                    "FROM formato_inicial";

    private static final String SQL_SELECT_BY_INTERN =
            "SELECT id_formato, id_practicante, tipo_formato, " +
                    "       ruta_archivo, estado, fecha_entrega " +
                    "FROM formato_inicial " +
                    "WHERE id_practicante = ?";


    @Override
    public int save(InitialFormat initialFormat) throws DataAccessException {
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt   (1, initialFormat.getIdIntern());
            statement.setString(2, initialFormat.getFormatType());
            statement.setString(3, initialFormat.getFilePath());
            statement.setString(4, initialFormat.getStatus());

            if (initialFormat.getSubmissionDate() != null) {
                statement.setDate(5, new java.sql.Date(
                        initialFormat.getSubmissionDate().getTime()));
            } else {
                statement.setNull(5, Types.DATE);
            }

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    initialFormat.setIdInitialFormat(generatedKeys.getInt(1));
                }
            }
        }catch (SQLException sqlException){
            throw new DataAccessException("Error saving initial format for intern", sqlException);
        }

        return rowsAffected;
    }

    @Override
    public InitialFormat getById(int idInitialFormat) throws DataAccessException {
        InitialFormat initialFormat = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idInitialFormat);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    initialFormat = mapResultSet(resultSet);
                }
            }
        }catch (SQLException sqlException){
            throw new DataAccessException("Error retrieving initial format with ID " + idInitialFormat, sqlException);
        }

        return initialFormat;
    }

    @Override
    public List<InitialFormat> getAll() throws DataAccessException {
        List<InitialFormat> initialFormats = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                initialFormats.add(mapResultSet(resultSet));
            }
        }catch (SQLException sqlException){
            throw new DataAccessException("Error retrieving all initial formats", sqlException);
        }

        return initialFormats;
    }

    @Override
    public List<InitialFormat> getByIdIntern(int idIntern) throws DataAccessException {
        List<InitialFormat> initialFormats = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_INTERN)) {

            statement.setInt(1, idIntern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    initialFormats.add(mapResultSet(resultSet));
                }
            }
        }catch (SQLException sqlException){
            throw new DataAccessException("Error retrieving initial formats for intern with ID " + idIntern, sqlException);
        }

        return initialFormats;
    }


    private InitialFormat mapResultSet(ResultSet resultSet) throws SQLException {
        InitialFormat initialFormat = new InitialFormat();
        initialFormat.setIdInitialFormat(resultSet.getInt   ("id_formato"));
        initialFormat.setIdIntern       (resultSet.getInt   ("id_practicante"));
        initialFormat.setFormatType     (resultSet.getString("tipo_formato"));
        initialFormat.setFilePath       (resultSet.getString("ruta_archivo"));
        initialFormat.setStatus         (resultSet.getString("estado"));
        initialFormat.setSubmissionDate (resultSet.getDate  ("fecha_entrega"));
        return initialFormat;
    }
}
