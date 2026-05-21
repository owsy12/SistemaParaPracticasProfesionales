package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.ProjectApplication;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
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

    private static final String SELECT_PROJECT_IDS_BY_INTERN_SQL =
            "SELECT sp.id_proyecto FROM solicitud_proyecto sp " +
                    "JOIN solicitud s ON s.id_solicitud = sp.id_solicitud " +
                    "WHERE s.id_practicante = ? " +
                    "ORDER BY sp.orden_preferencia ASC";

    @Override
    public boolean create(ProjectApplication projectApplication)
            throws ServiceException, ValidationException {
        if (projectApplication.getIdApplication() <= 0) {
            throw new ValidationException(
                    "El ID de la solicitud debe ser mayor a cero. ID recibido: "
                            + projectApplication.getIdApplication());
        }
        if (projectApplication.getIdProyect() <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: "
                            + projectApplication.getIdProyect());
        }

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
                    new Object[]{projectApplication.getIdApplication(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al crear la opción de proyecto.", sqlException);
        }

        return isCreated;
    }

    @Override
    public ProjectApplication findById(int projectApplicationId)
            throws ServiceException, ValidationException {
        if (projectApplicationId <= 0) {
            throw new ValidationException(
                    "El ID de la opción de proyecto debe ser mayor a cero. ID recibido: "
                            + projectApplicationId);
        }
        ProjectApplication projectApplicationResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_ID_SQL)) {

            preparedStatement.setInt(1, projectApplicationId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    projectApplicationResult = mapProjectApplication(rs);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar opción de proyecto con ID {0}: {1}",
                    new Object[]{projectApplicationId, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar la opción de proyecto por ID.", sqlException);
        }

        return projectApplicationResult;
    }

    @Override
    public List<ProjectApplication> findByApplication(int applicationId)
            throws ServiceException, ValidationException {
        if (applicationId <= 0) {
            throw new ValidationException(
                    "El ID de la solicitud debe ser mayor a cero. ID recibido: " + applicationId);
        }
        List<ProjectApplication> projectApplicationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BY_APPLICATION_SQL)) {

            preparedStatement.setInt(1, applicationId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    projectApplicationList.add(mapProjectApplication(rs));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar opciones para solicitud {0}: {1}",
                    new Object[]{applicationId, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException(
                    "Error al buscar las opciones de proyecto para la solicitud.", sqlException);
        }

        return projectApplicationList;
    }

    @Override
    public List<ProjectApplication> findAll() throws ServiceException {
        List<ProjectApplication> projectApplicationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                projectApplicationList.add(mapProjectApplication(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todas las opciones de proyecto: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar la lista de opciones de proyecto.", sqlException);
        }

        return projectApplicationList;
    }

    @Override
    public boolean delete(int projectApplicationId) throws ServiceException, ValidationException {
        if (projectApplicationId <= 0) {
            throw new ValidationException(
                    "El ID de la opción de proyecto debe ser mayor a cero. ID recibido: "
                            + projectApplicationId);
        }
        boolean isDeleted = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {

            preparedStatement.setInt(1, projectApplicationId);

            if (preparedStatement.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar opción de proyecto con ID {0}: {1}",
                    new Object[]{projectApplicationId, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al eliminar la opción de proyecto.", sqlException);
        }

        return isDeleted;
    }

    @Override
    public List<Integer> findProjectIdsByIntern(int idIntern) throws ServiceException, ValidationException {
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }
        List<Integer> projectIds = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PROJECT_IDS_BY_INTERN_SQL)) {

            preparedStatement.setInt(1, idIntern);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    projectIds.add(rs.getInt("id_proyecto"));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar proyectos seleccionados por practicante {0}: {1}",
                    new Object[]{idIntern, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al recuperar los proyectos seleccionados por el practicante.", sqlException);
        }

        return projectIds;
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
