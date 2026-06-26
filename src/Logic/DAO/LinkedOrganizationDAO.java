package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.ILinkedOrganizationDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LinkedOrganizationDAO implements ILinkedOrganizationDAO {

    private static final Logger LOGGER = Logger.getLogger(LinkedOrganizationDAO.class.getName());

    private static final String STATUS_ACTIVE = "Activa";

    private static final String INSERT_LINKED_ORGANIZATION_SQL =
            "INSERT INTO organizacion_vinculada " +
                    "(nombre_organizacion, correo_organizacion, direccion, sector, estado) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_LINKED_ORGANIZATION_BY_ID_SQL =
            "SELECT id_organizacion, nombre_organizacion, correo_organizacion, " +
                    "direccion, sector, estado FROM organizacion_vinculada " +
                    "WHERE id_organizacion = ?";
    private static final String SELECT_ALL_LINKED_ORGANIZATIONS_SQL =
            "SELECT id_organizacion, nombre_organizacion, correo_organizacion, " +
                    "direccion, sector, estado FROM organizacion_vinculada";
    private static final String SELECT_ALL_ACTIVE_LINKED_ORGANIZATIONS_SQL =
            "SELECT id_organizacion, nombre_organizacion, correo_organizacion, " +
                    "direccion, sector, estado FROM organizacion_vinculada " +
                    "WHERE estado = 'Activa'";
    private static final String UPDATE_LINKED_ORGANIZATION_SQL =
            "UPDATE organizacion_vinculada " +
                    "SET nombre_organizacion = ?, correo_organizacion = ?, " +
                    "direccion = ?, sector = ?, estado = ? " +
                    "WHERE id_organizacion = ?";
    private static final String UPDATE_LINKED_ORGANIZATION_STATUS_SQL =
            "UPDATE organizacion_vinculada SET estado = 'Inactiva' WHERE id_organizacion = ?";
    private static final String COUNT_PROJECTS_BY_ORGANIZATION_SQL =
            "SELECT COUNT(*) FROM proyecto WHERE id_organizacion = ?";
    private static final String DELETE_RESPONSIBLE_TECHNICIANS_BY_ORGANIZATION_SQL =
            "DELETE FROM tecnico_responsable WHERE id_organizacion = ?";
    private static final String DELETE_LINKED_ORGANIZATION_SQL =
            "DELETE FROM organizacion_vinculada WHERE id_organizacion = ?";

    private static final String ERROR_ORGANIZATION_HAS_PROJECTS =
            "No se puede eliminar la organización porque tiene proyectos vinculados.";
    private static final String ERROR_DUPLICATE_ENTRY =
            "Ya existe un registro con esa clave en la base de datos.";
    private static final String VALIDATION_ID_ORGANIZATION =
            "El ID de la organización debe ser mayor a cero. ID recibido: ";

    @Override
    public boolean saveLinkedOrganization(LinkedOrganization linkedOrganization)
            throws ServiceException, ValidationException {
        boolean isSaved = false;
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(INSERT_LINKED_ORGANIZATION_SQL)) {
            preparedStatement.setString(1, linkedOrganization.getName());
            preparedStatement.setString(2, linkedOrganization.getEmail());
            preparedStatement.setString(3, linkedOrganization.getAddress());
            preparedStatement.setString(4, linkedOrganization.getSector());
            preparedStatement.setString(5, STATUS_ACTIVE);
            if (preparedStatement.executeUpdate() > 0) {
                isSaved = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving organization '{0}': {1}",
                    new Object[]{linkedOrganization.getName(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException("Error al guardar la organización vinculada.", sqlException);
        }
        return isSaved;
    }

    @Override
    public LinkedOrganization findById(int idOrganization)
            throws ServiceException, ValidationException {
        if (idOrganization <= 0) {
            throw new ValidationException(VALIDATION_ID_ORGANIZATION + idOrganization);
        }
        LinkedOrganization organizationResult = null;
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_LINKED_ORGANIZATION_BY_ID_SQL)) {
            preparedStatement.setInt(1, idOrganization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    organizationResult = mapLinkedOrganization(resultSet);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding organization with ID {0}: {1}",
                    new Object[]{idOrganization, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException("Error al buscar la organización vinculada por ID.",
                    sqlException);
        }
        return organizationResult;
    }

    @Override
    public List<LinkedOrganization> findAll() throws ServiceException {
        List<LinkedOrganization> organizationList = new ArrayList<>();
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ALL_LINKED_ORGANIZATIONS_SQL);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                organizationList.add(mapLinkedOrganization(rs));
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error retrieving all linked organizations: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar todas las organizaciones vinculadas.", sqlException);
        }
        return organizationList;
    }

    @Override
    public List<LinkedOrganization> findAllActive() throws ServiceException {
        List<LinkedOrganization> organizationList = new ArrayList<>();
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ALL_ACTIVE_LINKED_ORGANIZATIONS_SQL);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                organizationList.add(mapLinkedOrganization(rs));
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving active organizations: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar organizaciones vinculadas activas.", sqlException);
        }
        return organizationList;
    }

    @Override
    public boolean update(LinkedOrganization linkedOrganization)
            throws ServiceException, ValidationException {
        if (linkedOrganization.getIdLinkedOrganization() <= 0) {
            throw new ValidationException(
                    VALIDATION_ID_ORGANIZATION + linkedOrganization.getIdLinkedOrganization());
        }
        boolean isUpdated = false;
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UPDATE_LINKED_ORGANIZATION_SQL)) {
            preparedStatement.setString(1, linkedOrganization.getName());
            preparedStatement.setString(2, linkedOrganization.getEmail());
            preparedStatement.setString(3, linkedOrganization.getAddress());
            preparedStatement.setString(4, linkedOrganization.getSector());
            preparedStatement.setString(5, linkedOrganization.getStatus());
            preparedStatement.setInt(6, linkedOrganization.getIdLinkedOrganization());
            if (preparedStatement.executeUpdate() > 0) {
                isUpdated = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error updating organization with ID {0}: {1}",
                    new Object[]{linkedOrganization.getIdLinkedOrganization(),
                            sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException("Error al actualizar la organización vinculada.",
                    sqlException);
        }
        return isUpdated;
    }

    @Override
    public boolean deactivateLinkedOrganization(int idOrganization)
            throws ServiceException, ValidationException {
        if (idOrganization <= 0) {
            throw new ValidationException(VALIDATION_ID_ORGANIZATION + idOrganization);
        }
        boolean isDeactivated = false;
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UPDATE_LINKED_ORGANIZATION_STATUS_SQL)) {
            preparedStatement.setInt(1, idOrganization);
            if (preparedStatement.executeUpdate() > 0) {
                isDeactivated = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deactivating organization with ID {0}: {1}",
                    new Object[]{idOrganization, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException("Error al desactivar la organización vinculada.",
                    sqlException);
        }
        return isDeactivated;
    }

    @Override
    public boolean hasAssociatedProjects(int idOrganization)
            throws ServiceException, ValidationException {
        if (idOrganization <= 0) {
            throw new ValidationException(VALIDATION_ID_ORGANIZATION + idOrganization);
        }
        boolean hasProjects = false;
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(COUNT_PROJECTS_BY_ORGANIZATION_SQL)) {
            preparedStatement.setInt(1, idOrganization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    hasProjects = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error verifying projects for organization with ID {0}: {1}",
                    new Object[]{idOrganization, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al verificar proyectos asociados a la organización.", sqlException);
        }
        return hasProjects;
    }

    @Override
    public boolean deleteLinkedOrganization(int idOrganization)
            throws ServiceException, ValidationException {
        if (idOrganization <= 0) {
            throw new ValidationException(VALIDATION_ID_ORGANIZATION + idOrganization);
        }
        boolean isDeleted = false;
        if (hasAssociatedProjects(idOrganization)) {
            throw new ValidationException(ERROR_ORGANIZATION_HAS_PROJECTS + idOrganization);
        } else {
            try (Connection connection = DataBaseConnection.connectDatabase()) {
                connection.setAutoCommit(false);
                try (PreparedStatement deleteTechnicians = connection.prepareStatement(
                        DELETE_RESPONSIBLE_TECHNICIANS_BY_ORGANIZATION_SQL);
                     PreparedStatement deleteOrganization = connection.prepareStatement(
                             DELETE_LINKED_ORGANIZATION_SQL)) {
                    deleteTechnicians.setInt(1, idOrganization);
                    deleteTechnicians.executeUpdate();
                    deleteOrganization.setInt(1, idOrganization);
                    int rowsAffected = deleteOrganization.executeUpdate();
                    if (rowsAffected > 0) {
                        connection.commit();
                        isDeleted = true;
                    } else {
                        connection.rollback();
                    }
                } catch (SQLException sqlException) {
                    safeRollback(connection);
                    LOGGER.log(Level.SEVERE,
                            "Transaction error while deleting organization with ID {0}: {1}",
                            new Object[]{idOrganization, sqlException.getMessage()});
                    throw new ServiceException(
                            "Error al eliminar la organización vinculada.", sqlException);
                }
            } catch (SQLException sqlException) {
                LOGGER.log(Level.SEVERE,
                        "Connection error while deleting organization with ID {0}: {1}",
                        new Object[]{idOrganization, sqlException.getMessage()});
                throw new ServiceException(
                        "Error de conexión al eliminar la organización vinculada.", sqlException);
            }
        }
        return isDeleted;
    }

    private void safeRollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error executing rollback: {0}",
                    sqlException.getMessage());
        }
    }

    private LinkedOrganization mapLinkedOrganization(ResultSet resultSet) throws SQLException {
        LinkedOrganization organization = new LinkedOrganization(
                resultSet.getInt("id_organizacion"),
                resultSet.getString("nombre_organizacion"),
                resultSet.getString("sector"),
                resultSet.getString("direccion"),
                resultSet.getString("correo_organizacion")
        );
        organization.setStatus(resultSet.getString("estado"));
        return organization;
    }
}