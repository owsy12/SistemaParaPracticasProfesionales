package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.ProjectApplication;
import Logic.Exceptions.DatabaseException;
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

    @Override
    public boolean create(ProjectApplication projectApplication)
            throws DatabaseException, ValidationException {
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
        if (projectApplication.getPreferenceOrder() <= 0) {
            throw new ValidationException(
                    "El orden de preferencia debe ser mayor a cero. Valor recibido: "
                            + projectApplication.getPreferenceOrder());
        }
        boolean isCreated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {

            ps.setInt(1, projectApplication.getIdApplication());
            ps.setInt(2, projectApplication.getIdProyect());
            ps.setInt(3, projectApplication.getPreferenceOrder());

            if (ps.executeUpdate() > 0) {
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
            throw new DatabaseException("Error al crear la opción de proyecto.", sqlException);
        }

        return isCreated;
    }

    @Override
    public ProjectApplication findById(int projectApplicationId)
            throws DatabaseException, ValidationException {
        if (projectApplicationId <= 0) {
            throw new ValidationException(
                    "El ID de la opción de proyecto debe ser mayor a cero. ID recibido: "
                            + projectApplicationId);
        }
        ProjectApplication projectApplicationResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setInt(1, projectApplicationId);

            try (ResultSet rs = ps.executeQuery()) {
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
            throw new DatabaseException("Error al buscar la opción de proyecto por ID.", sqlException);
        }

        return projectApplicationResult;
    }

    @Override
    public List<ProjectApplication> findByApplication(int applicationId)
            throws DatabaseException, ValidationException {
        if (applicationId <= 0) {
            throw new ValidationException(
                    "El ID de la solicitud debe ser mayor a cero. ID recibido: " + applicationId);
        }
        List<ProjectApplication> projectApplicationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_APPLICATION_SQL)) {

            ps.setInt(1, applicationId);

            try (ResultSet rs = ps.executeQuery()) {
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
            throw new DatabaseException(
                    "Error al buscar las opciones de proyecto para la solicitud.", sqlException);
        }

        return projectApplicationList;
    }

    @Override
    public List<ProjectApplication> findAll() throws DatabaseException {
        List<ProjectApplication> projectApplicationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                projectApplicationList.add(mapProjectApplication(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todas las opciones de proyecto: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException(
                    "Error al recuperar la lista de opciones de proyecto.", sqlException);
        }

        return projectApplicationList;
    }

    @Override
    public boolean delete(int projectApplicationId) throws DatabaseException, ValidationException {
        if (projectApplicationId <= 0) {
            throw new ValidationException(
                    "El ID de la opción de proyecto debe ser mayor a cero. ID recibido: "
                            + projectApplicationId);
        }
        boolean isDeleted = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(DELETE_SQL)) {

            ps.setInt(1, projectApplicationId);

            if (ps.executeUpdate() > 0) {
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
            throw new DatabaseException("Error al eliminar la opción de proyecto.", sqlException);
        }

        return isDeleted;
    }

    private ProjectApplication mapProjectApplication(ResultSet rs) throws SQLException {
        return new ProjectApplication(
                rs.getInt("id_solicitud_proyecto"),
                rs.getInt("id_solicitud"),
                rs.getInt("id_proyecto"),
                rs.getInt("orden_preferencia")
        );
    }
}
