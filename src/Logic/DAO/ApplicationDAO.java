package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Application;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IApplicationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationDAO implements IApplicationDAO {

    private static final Logger LOGGER = Logger.getLogger(ApplicationDAO.class.getName());

    private static final String INSERT_SQL =
            "INSERT INTO solicitud (id_practicante, estado) VALUES (?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id_solicitud, id_practicante, estado, fecha_solicitud " +
                    "FROM solicitud WHERE id_solicitud = ?";

    private static final String SELECT_BY_INTERN_SQL =
            "SELECT id_solicitud, id_practicante, estado, fecha_solicitud " +
                    "FROM solicitud WHERE id_practicante = ? " +
                    "ORDER BY fecha_solicitud DESC LIMIT 1";

    private static final String SELECT_ALL_SQL =
            "SELECT id_solicitud, id_practicante, estado, fecha_solicitud FROM solicitud";

    private static final String SELECT_BY_STATUS_SQL =
            "SELECT id_solicitud, id_practicante, estado, fecha_solicitud " +
                    "FROM solicitud WHERE estado = ?";

    private static final String UPDATE_STATUS_SQL =
            "UPDATE solicitud SET estado = ? WHERE id_solicitud = ?";

    @Override
    public boolean create(Application application) throws ServiceException, ValidationException {
        if (application.getIdIntern() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + application.getIdIntern());
        }
        boolean isCreated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            preparedStatement.setInt(1, application.getIdIntern());
            preparedStatement.setString(2, application.getStatus());

            if (preparedStatement.executeUpdate() > 0) {
                isCreated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al registrar solicitud para practicante {0}: {1}",
                    new Object[]{ application.getIdIntern(), sqlException.getMessage() });
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al crear la solicitud.", sqlException);
        }

        return isCreated;
    }

    @Override
    public Application findById(int applicationId) throws ServiceException {
        Application applicationResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_ID_SQL)) {

            preparedStatement.setInt(1, applicationId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    applicationResult = mapApplication(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar solicitud con ID {0}: {1}",
                    new Object[]{ applicationId, sqlException.getMessage() });
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar la solicitud por ID.", sqlException);
        }

        return applicationResult;
    }

    @Override
    public Application findByIntern(int internId) throws ServiceException {
        Application applicationResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_INTERN_SQL)) {

            preparedStatement.setInt(1, internId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    applicationResult = mapApplication(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar solicitud para practicante {0}: {1}",
                    new Object[]{ internId, sqlException.getMessage() });
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar la solicitud del practicante.", sqlException);
        }

        return applicationResult;
    }

    @Override
    public List<Application> findAll() throws ServiceException {
        List<Application> applicationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                applicationList.add(mapApplication(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todas las solicitudes: {0}", sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar la lista de solicitudes.", sqlException);
        }

        return applicationList;
    }

    @Override
    public List<Application> findByStatus(String status) throws ServiceException, ValidationException {
        List<Application> applicationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_STATUS_SQL)) {

            preparedStatement.setString(1, status);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    applicationList.add(mapApplication(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar solicitudes con estado {0}: {1}",
                    new Object[]{ status, sqlException.getMessage() });
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar las solicitudes por estado.", sqlException);
        }

        return applicationList;
    }

    @Override
    public boolean updateStatus(int applicationId, String status) throws ServiceException {
        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_STATUS_SQL)) {

            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, applicationId);

            if (preparedStatement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar estado de solicitud {0}: {1}",
                    new Object[]{ applicationId, sqlException.getMessage() });
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al actualizar el estado de la solicitud.", sqlException);
        }

        return isUpdated;
    }

    private Application mapApplication(ResultSet resultSet) throws SQLException {
        return new Application(
                resultSet.getInt("id_solicitud"),
                resultSet.getInt("id_practicante"),
                resultSet.getString("estado"),
                resultSet.getDate("fecha_solicitud")
        );
    }
}