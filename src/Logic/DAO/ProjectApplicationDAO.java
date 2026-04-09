package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.ProjectApplication;
import Logic.Exceptions.DataAccessException;
import Logic.Interface.IProjectApplicationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectApplicationDAO implements IProjectApplicationDAO {

    private static final Logger LOGGER = Logger.getLogger(ProjectApplicationDAO.class.getName());

    private static final String INSERT_SQL =
            "INSERT INTO solicitud_proyecto (id_solicitud, id_proyecto, orden_preferencia) " +
                    "VALUES (?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id_solicitud_proyecto, id_solicitud, id_proyecto, orden_preferencia " +
                    "FROM solicitud_proyecto WHERE id_solicitud_proyecto = ?";

    private static final String SELECT_BY_APPLICATION_SQL =
            "SELECT id_solicitud_proyecto, id_solicitud, id_proyecto, orden_preferencia " +
                    "FROM solicitud_proyecto WHERE id_solicitud = ? " +
                    "ORDER BY orden_preferencia ASC";

    private static final String SELECT_ALL_SQL =
            "SELECT id_solicitud_proyecto, id_solicitud, id_proyecto, orden_preferencia " +
                    "FROM solicitud_proyecto";

    private static final String DELETE_SQL =
            "DELETE FROM solicitud_proyecto WHERE id_solicitud_proyecto = ?";

    @Override
    public boolean create(ProjectApplication projectApplication) throws DataAccessException {
        boolean isCreated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {

            preparedStatement.setInt(1, projectApplication.getIdApplication());
            preparedStatement.setInt(2, projectApplication.getIdProyect());
            preparedStatement.setInt(3, projectApplication.getPreferenceOrder());

            if (preparedStatement.executeUpdate() > 0) {
                isCreated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al registrar opción de proyecto para solicitud {0}: {1}",
                    new Object[]{ projectApplication.getIdApplication(), sqlException.getMessage() });
            throw new DataAccessException("Error al crear la opción de proyecto.", sqlException);
        }

        return isCreated;
    }

    @Override
    public ProjectApplication findById(int projectApplicationId) throws DataAccessException {
        ProjectApplication projectApplicationResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_ID_SQL)) {

            preparedStatement.setInt(1, projectApplicationId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    projectApplicationResult = mapProjectApplication(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar opción de proyecto con ID {0}: {1}",
                    new Object[]{ projectApplicationId, sqlException.getMessage() });
            throw new DataAccessException("Error al buscar la opción de proyecto por ID.", sqlException);
        }

        return projectApplicationResult;
    }

    @Override
    public List<ProjectApplication> findByApplication(int applicationId) throws DataAccessException {
        List<ProjectApplication> projectApplicationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_APPLICATION_SQL)) {

            preparedStatement.setInt(1, applicationId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    projectApplicationList.add(mapProjectApplication(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar opciones de proyecto para solicitud {0}: {1}",
                    new Object[]{ applicationId, sqlException.getMessage() });
            throw new DataAccessException("Error al buscar las opciones de proyecto para la solicitud.", sqlException);
        }

        return projectApplicationList;
    }

    @Override
    public List<ProjectApplication> findAll() throws DataAccessException {
        List<ProjectApplication> projectApplicationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                projectApplicationList.add(mapProjectApplication(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todas las opciones de proyecto: {0}", sqlException.getMessage());
            throw new DataAccessException("Error al recuperar la lista de opciones de proyecto.", sqlException);
        }

        return projectApplicationList;
    }

    @Override
    public boolean delete(int projectApplicationId) throws DataAccessException {
        boolean isDeleted = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {

            preparedStatement.setInt(1, projectApplicationId);

            if (preparedStatement.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar opción de proyecto con ID {0}: {1}",
                    new Object[]{ projectApplicationId, sqlException.getMessage() });
            throw new DataAccessException("Error al eliminar la opción de proyecto.", sqlException);
        }

        return isDeleted;
    }

    private ProjectApplication mapProjectApplication(ResultSet resultSet) throws SQLException {
        return new ProjectApplication(
                resultSet.getInt("id_solicitud_proyecto"),
                resultSet.getInt("id_solicitud"),
                resultSet.getInt("id_proyecto"),
                resultSet.getInt("orden_preferencia")
        );
    }
}