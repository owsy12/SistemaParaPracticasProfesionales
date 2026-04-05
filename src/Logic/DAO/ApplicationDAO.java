package Logic.DAO;

import DataAccess.BDConnection;
import Logic.DTOs.Application;
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
    public boolean create(Application application) {
        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            preparedStatement.setInt(1, application.getIdIntern());
            preparedStatement.setString(2, application.getStatus());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al registrar solicitud para practicante {0}: {1}",
                    new Object[]{ application.getIdIntern(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public Application findById(int applicationId) {
        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_ID_SQL)) {

            preparedStatement.setInt(1, applicationId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapApplication(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar solicitud con ID {0}: {1}",
                    new Object[]{ applicationId, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public Application findByIntern(int internId) {
        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_INTERN_SQL)) {

            preparedStatement.setInt(1, internId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapApplication(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar solicitud para practicante {0}: {1}",
                    new Object[]{ internId, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public List<Application> findAll() {
        List<Application> applicationList = new ArrayList<>();

        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                applicationList.add(mapApplication(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al recuperar todas las solicitudes: {0}",
                    sqlException.getMessage());
        }
        return applicationList;
    }

    @Override
    public List<Application> findByStatus(String status) {
        List<Application> applicationList = new ArrayList<>();

        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_STATUS_SQL)) {

            preparedStatement.setString(1, status);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    applicationList.add(mapApplication(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar solicitudes con estado {0}: {1}",
                    new Object[]{ status, sqlException.getMessage() });
        }
        return applicationList;
    }

    @Override
    public boolean updateStatus(int applicationId, String status) {
        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_STATUS_SQL)) {

            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, applicationId);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al actualizar estado de solicitud {0}: {1}",
                    new Object[]{ applicationId, sqlException.getMessage() });
            return false;
        }
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