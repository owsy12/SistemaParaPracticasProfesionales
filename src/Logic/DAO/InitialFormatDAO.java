package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.InitialFormat;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IInitialFormatDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InitialFormatDAO implements IInitialFormatDAO {

    private static final Logger LOGGER = Logger.getLogger(InitialFormatDAO.class.getName());
    private static final String SQL_INSERT =
            "INSERT INTO formato_inicial " +
                    "(id_practicante, tipo_formato, ruta_archivo, estado, fecha_entrega) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
            "SELECT id_formato, id_practicante, tipo_formato, " +
                    "       ruta_archivo, estado, fecha_entrega " +
                    "FROM formato_inicial WHERE id_formato = ?";
    private static final String SQL_SELECT_ALL =
            "SELECT id_formato, id_practicante, tipo_formato, " +
                    "       ruta_archivo, estado, fecha_entrega " +
                    "FROM formato_inicial";
    private static final String SQL_SELECT_BY_INTERN =
            "SELECT id_formato, id_practicante, tipo_formato, " +
                    "       ruta_archivo, estado, fecha_entrega " +
                    "FROM formato_inicial WHERE id_practicante = ?";

    @Override
    public int save(InitialFormat initialFormat) throws ServiceException, ValidationException {
        if (initialFormat.getIdIntern() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: "
                            + initialFormat.getIdIntern());
        }
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt   (1, initialFormat.getIdIntern());
            statement.setString(2, initialFormat.getFormatType());
            statement.setString(3, initialFormat.getFilePath());
            statement.setString(4, initialFormat.getStatus());

            if (initialFormat.getSubmissionDate() != null) {
                statement.setDate(5, new java.sql.Date(initialFormat.getSubmissionDate().getTime()));
            } else {
                statement.setNull(5, Types.DATE);
            }

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    initialFormat.setIdInitialFormat(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar formato inicial para practicante {0}: {1}",
                    new Object[]{initialFormat.getIdIntern(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar el formato inicial.", sqlException);
        }

        return rowsAffected;
    }

    @Override
    public InitialFormat getById(int idInitialFormat) throws ServiceException, ValidationException {
        if (idInitialFormat <= 0) {
            throw new ValidationException(
                    "El ID del formato inicial debe ser mayor a cero. ID recibido: "
                            + idInitialFormat);
        }
        InitialFormat initialFormat = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idInitialFormat);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    initialFormat = mapResultSet(rs);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar formato inicial con ID {0}: {1}",
                    new Object[]{idInitialFormat, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar el formato inicial con ID " + idInitialFormat, sqlException);
        }

        return initialFormat;
    }

    @Override
    public List<InitialFormat> getAll() throws ServiceException {
        List<InitialFormat> initialFormats = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                initialFormats.add(mapResultSet(rs));
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todos los formatos iniciales: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar todos los formatos iniciales.", sqlException);
        }

        return initialFormats;
    }

    @Override
    public List<InitialFormat> getByIdIntern(int idIntern) throws ServiceException, ValidationException {
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }
        List<InitialFormat> initialFormats = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_INTERN)) {

            statement.setInt(1, idIntern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    initialFormats.add(mapResultSet(rs));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar formatos iniciales del practicante {0}: {1}",
                    new Object[]{idIntern, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar los formatos iniciales del practicante.", sqlException);
        }

        return initialFormats;
    }

    private InitialFormat mapResultSet(ResultSet rs) throws SQLException {
        InitialFormat initialFormat = new InitialFormat();
        initialFormat.setIdInitialFormat(rs.getInt   ("id_formato"));
        initialFormat.setIdIntern       (rs.getInt   ("id_practicante"));
        initialFormat.setFormatType     (rs.getString("tipo_formato"));
        initialFormat.setFilePath       (rs.getString("ruta_archivo"));
        initialFormat.setStatus         (rs.getString("estado"));
        initialFormat.setSubmissionDate (rs.getDate  ("fecha_entrega"));
        return initialFormat;
    }
}
