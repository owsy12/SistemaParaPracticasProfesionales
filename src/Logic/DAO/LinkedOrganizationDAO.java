package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.ILinkedOrganizationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LinkedOrganizationDAO implements ILinkedOrganizationDAO {

    private static final Logger LOGGER = Logger.getLogger(LinkedOrganizationDAO.class.getName());
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

    @Override
    public boolean saveLinkedOrganization(LinkedOrganization linkedOrganization)
            throws ServiceException, ValidationException {
        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_LINKED_ORGANIZATION_SQL)) {

            preparedStatement.setString(1, linkedOrganization.getName());
            preparedStatement.setString(2, linkedOrganization.getEmail());
            preparedStatement.setString(3, linkedOrganization.getAdress());
            preparedStatement.setString(4, linkedOrganization.getSector());
            preparedStatement.setString(5, "Activa");

            if (preparedStatement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar organización '{0}': {1}",
                    new Object[]{linkedOrganization.getName(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar la organización vinculada.", sqlException);
        }

        return isSaved;
    }

    @Override
    public LinkedOrganization findById(int idOrganizacion) throws ServiceException, ValidationException {
        if (idOrganizacion <= 0) {
            throw new ValidationException(
                    "El ID de la organización debe ser mayor a cero. ID recibido: " + idOrganizacion);
        }
        LinkedOrganization organizationResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LINKED_ORGANIZATION_BY_ID_SQL)) {

            preparedStatement.setInt(1, idOrganizacion);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    organizationResult = mapLinkedOrganization(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar organización con ID {0}: {1}",
                    new Object[]{idOrganizacion, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar la organización vinculada por ID.", sqlException);
        }

        return organizationResult;
    }

    @Override
    public List<LinkedOrganization> findAll() throws ServiceException {
        List<LinkedOrganization> organizationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_LINKED_ORGANIZATIONS_SQL);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                organizationList.add(mapLinkedOrganization(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todas las organizaciones vinculadas: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar todas las organizaciones vinculadas.", sqlException);
        }

        return organizationList;
    }

    @Override
    public List<LinkedOrganization> findAllActive() throws ServiceException {
        List<LinkedOrganization> organizationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_ACTIVE_LINKED_ORGANIZATIONS_SQL);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                organizationList.add(mapLinkedOrganization(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar organizaciones activas: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar organizaciones vinculadas activas.", sqlException);
        }

        return organizationList;
    }

    @Override
    public boolean update(LinkedOrganization linkedOrganization) throws ServiceException, ValidationException {
        if (linkedOrganization.getIdLinkedOrganization() <= 0) {
            throw new ValidationException(
                    "El ID de la organización debe ser mayor a cero. ID recibido: "
                            + linkedOrganization.getIdLinkedOrganization());
        }
        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_LINKED_ORGANIZATION_SQL)) {

            preparedStatement.setString(1, linkedOrganization.getName());
            preparedStatement.setString(2, "");
            preparedStatement.setString(3, linkedOrganization.getAdress());
            preparedStatement.setString(4, linkedOrganization.getSector());
            preparedStatement.setString(5, "Activa");
            preparedStatement.setInt   (6, linkedOrganization.getIdLinkedOrganization());

            if (preparedStatement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar organización con ID {0}: {1}",
                    new Object[]{linkedOrganization.getIdLinkedOrganization(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al actualizar la organización vinculada.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean deactivateLinkedOrganization(int idOrganizacion)
            throws ServiceException, ValidationException {
        if (idOrganizacion <= 0) {
            throw new ValidationException(
                    "El ID de la organización debe ser mayor a cero. ID recibido: " + idOrganizacion);
        }
        boolean isDeactivated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_LINKED_ORGANIZATION_STATUS_SQL)) {

            preparedStatement.setInt(1, idOrganizacion);

            if (preparedStatement.executeUpdate() > 0) {
                isDeactivated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al desactivar organización con ID {0}: {1}",
                    new Object[]{idOrganizacion, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al desactivar la organización vinculada.", sqlException);
        }

        return isDeactivated;
    }

    private LinkedOrganization mapLinkedOrganization(ResultSet resultSet) throws SQLException {
        return new LinkedOrganization(
                resultSet.getInt   ("id_organizacion"),
                resultSet.getString("nombre_organizacion"),
                resultSet.getString("sector"),
                resultSet.getString("direccion"),
                ""
        );
    }
}
