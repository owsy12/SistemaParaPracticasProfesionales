package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.ProjectApplication;
import Logic.Interface.IProjectApplicationDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public boolean create(ProjectApplication projectApplication) {
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
            LOGGER.log(Level.SEVERE, "Error creating project application choice {0}: {1}",
                    new Object[]{ projectApplication.getIdApplication(), sqlException.getMessage() });
        }

        return isCreated;
    }

    @Override
    public Optional<ProjectApplication> findById(int projectApplicationId) {
        Optional<ProjectApplication> projectResult = Optional.empty();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_ID_SQL)) {

            preparedStatement.setInt(1, projectApplicationId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    projectResult = Optional.of(mapProjectApplication(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding project application choice ID {0}: {1}",
                    new Object[]{ projectApplicationId, sqlException.getMessage() });
        }

        return projectResult;
    }

    @Override
    public List<ProjectApplication> findByApplication(int applicationId) {
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
            LOGGER.log(Level.SEVERE, "Error finding project choices for application {0}: {1}",
                    new Object[]{ applicationId, sqlException.getMessage() });
        }

        return projectApplicationList;
    }

    @Override
    public List<ProjectApplication> findAll() {
        List<ProjectApplication> projectApplicationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                projectApplicationList.add(mapProjectApplication(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all project applications: {0}",
                    sqlException.getMessage());
        }

        return projectApplicationList;
    }

    @Override
    public boolean delete(int projectApplicationId) {
        boolean isDeleted = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {

            preparedStatement.setInt(1, projectApplicationId);
            if (preparedStatement.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deleting project application ID {0}: {1}",
                    new Object[]{ projectApplicationId, sqlException.getMessage() });
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