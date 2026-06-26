package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.InitialFormat;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IInitialFormatDAO;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InitialFormatDAO implements IInitialFormatDAO {
    private static final String STATUS_SUBMITTED = "Entregado";


    private static final Logger LOGGER = Logger.getLogger(InitialFormatDAO.class.getName());
    private static final String SQL_INSERT =
            "INSERT INTO formato_inicial " +
                    "(id_practicante, id_proyecto, tipo_formato, ruta_archivo, estado, fecha_entrega) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
            "SELECT id_formato, id_practicante, id_proyecto, tipo_formato, " +
                    "       ruta_archivo, estado, fecha_entrega " +
                    "FROM formato_inicial WHERE id_formato = ?";
    private static final String SQL_SELECT_ALL =
            "SELECT id_formato, id_practicante, id_proyecto, tipo_formato, " +
                    "       ruta_archivo, estado, fecha_entrega " +
                    "FROM formato_inicial";
    private static final String SQL_SELECT_BY_INTERN =
            "SELECT id_formato, id_practicante, id_proyecto, tipo_formato, " +
                    "       ruta_archivo, estado, fecha_entrega " +
                    "FROM formato_inicial WHERE id_practicante = ?";

    private static final String SQL_FIND_PENDING_BY_INTERN =
            "SELECT id_formato, id_practicante, id_proyecto, tipo_formato, " +
                    "ruta_archivo, estado, fecha_entrega " +
                    "FROM formato_inicial " +
                    "WHERE id_practicante = ? AND estado = 'Pendiente'";

    private static final String SQL_UPLOAD_UPDATE_STATUS =
            "UPDATE formato_inicial " +
                    "SET estado = ?, ruta_archivo = ?, tipo_formato = ? " +
                    "WHERE id_formato = ?";
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

            statement.setInt (1, initialFormat.getIdIntern());
            statement.setInt (2, initialFormat.getIdProject());
            statement.setString(3, initialFormat.getFormatType());
            statement.setString(4, initialFormat.getFilePath());
            statement.setString(5, initialFormat.getStatus());

            if (initialFormat.getSubmissionDate() != null) {
                statement.setDate(6, Date.valueOf(initialFormat.getSubmissionDate()));
            } else {
                statement.setDate(6, Date.valueOf(LocalDateTime.now().toLocalDate()));
            }

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    initialFormat.setIdInitialFormat(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving initial format for intern {0}: {1}",
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

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    initialFormat = mapResultSet(resultSet);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving initial format with ID {0}: {1}",
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
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                initialFormats.add(mapResultSet(resultSet));
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all initial formats: {0}",
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

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    initialFormats.add(mapResultSet(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving initial formats for intern {0}: {1}",
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
    @Override
    public List<InitialFormat> findPendingByIntern(int idIntern) throws ServiceException, ValidationException {

        if (idIntern <= 0) {

            throw new ValidationException("El ID del practicante debe ser mayor a cero. ID recibido: "
                            + idIntern);
        }

        List<InitialFormat> initialFormats = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_PENDING_BY_INTERN)) {
            statement.setInt(1, idIntern);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    initialFormats.add(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {

            LOGGER.log(Level.SEVERE, "Error retrieving pending formats for intern {0}: {1}",
                    new Object[]{idIntern, sqlException.getMessage()});

            throw new ServiceException("Error al recuperar los formatos pendientes del practicante.",
                    sqlException);
        }

        return initialFormats;
    }

    @Override
    public int updateStatus(InitialFormat initialFormat) throws ServiceException, ValidationException {

        int rowsAffected;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement =
                     connection.prepareStatement(SQL_UPLOAD_UPDATE_STATUS)) {

            statement.setString(1, STATUS_SUBMITTED);
            statement.setString(2, initialFormat.getFilePath());
            statement.setString(3, initialFormat.getFormatType());
            statement.setInt(4, initialFormat.getIdInitialFormat());

            rowsAffected = statement.executeUpdate();

        } catch (SQLException sqlException) {

            LOGGER.log(Level.SEVERE,
                    "Error updating initial format status with ID {0}: {1}",
                    new Object[]{initialFormat.getIdInitialFormat(), sqlException.getMessage()});

            throw new ServiceException(
                    "Error al actualizar el estado del formato inicial.",
                    sqlException);

        }

        return rowsAffected;
    }

    private InitialFormat mapResultSet(ResultSet resultSet) throws SQLException {
        InitialFormat initialFormat = new InitialFormat();
        initialFormat.setIdInitialFormat(resultSet.getInt ("id_formato"));
        initialFormat.setIdIntern (resultSet.getInt ("id_practicante"));
        initialFormat.setIdProject (resultSet.getInt ("id_proyecto"));
        initialFormat.setFormatType (resultSet.getString("tipo_formato"));
        initialFormat.setFilePath (resultSet.getString("ruta_archivo"));
        initialFormat.setStatus (resultSet.getString("estado"));
        Date submissionDate = resultSet.getDate("fecha_entrega");

        if (submissionDate != null) {
            initialFormat.setSubmissionDate(submissionDate.toLocalDate());
        }

        return initialFormat;
    }

    public boolean deleteByInternAndProject(int internId, int projectId)
            throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }
        if (projectId <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + projectId);
        }

        String sql = "DELETE FROM formato_inicial WHERE id_practicante = ? AND id_proyecto = ?";
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, internId);
            statement.setInt(2, projectId);
            rowsAffected = statement.executeUpdate();

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error deleting formats for intern {0} in project {1}: {2}",
                    new Object[]{internId, projectId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al eliminar formatos del practicante.", sqlException);
        }

        return rowsAffected >= 0;
    }
}
