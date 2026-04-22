package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.DatabaseException;
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
            throws DatabaseException, ValidationException {
        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(INSERT_LINKED_ORGANIZATION_SQL)) {

            ps.setString(1, linkedOrganization.getName());
            ps.setString(2, "");
            ps.setString(3, linkedOrganization.getAdress());
            ps.setString(4, linkedOrganization.getSector());
            ps.setString(5, "Activa");

            if (ps.executeUpdate() > 0) {
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
            throw new DatabaseException("Error al guardar la organización vinculada.", sqlException);
        }

        return isSaved;
    }

    @Override
    public LinkedOrganization findById(int idOrganizacion) throws DatabaseException, ValidationException {
        if (idOrganizacion <= 0) {
            throw new ValidationException(
                    "El ID de la organización debe ser mayor a cero. ID recibido: " + idOrganizacion);
        }
        LinkedOrganization organizationResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_LINKED_ORGANIZATION_BY_ID_SQL)) {

            ps.setInt(1, idOrganizacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    organizationResult = mapLinkedOrganization(rs);
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
            throw new DatabaseException("Error al buscar la organización vinculada por ID.", sqlException);
        }

        return organizationResult;
    }

    @Override
    public List<LinkedOrganization> findAll() throws DatabaseException {
        List<LinkedOrganization> organizationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL_LINKED_ORGANIZATIONS_SQL);
             ResultSet rs = ps.executeQuery()) {

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
            throw new DatabaseException("Error al recuperar todas las organizaciones vinculadas.", sqlException);
        }

        return organizationList;
    }

    @Override
    public List<LinkedOrganization> findAllActive() throws DatabaseException {
        List<LinkedOrganization> organizationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL_ACTIVE_LINKED_ORGANIZATIONS_SQL);
             ResultSet rs = ps.executeQuery()) {

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
            throw new DatabaseException("Error al recuperar organizaciones vinculadas activas.", sqlException);
        }

        return organizationList;
    }

    @Override
    public boolean update(LinkedOrganization linkedOrganization) throws DatabaseException, ValidationException {
        if (linkedOrganization.getIdLinkedOrganization() <= 0) {
            throw new ValidationException(
                    "El ID de la organización debe ser mayor a cero. ID recibido: "
                            + linkedOrganization.getIdLinkedOrganization());
        }
        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(UPDATE_LINKED_ORGANIZATION_SQL)) {

            ps.setString(1, linkedOrganization.getName());
            ps.setString(2, "");
            ps.setString(3, linkedOrganization.getAdress());
            ps.setString(4, linkedOrganization.getSector());
            ps.setString(5, "Activa");
            ps.setInt   (6, linkedOrganization.getIdLinkedOrganization());

            if (ps.executeUpdate() > 0) {
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
            throw new DatabaseException("Error al actualizar la organización vinculada.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean deactivateLinkedOrganization(int idOrganizacion)
            throws DatabaseException, ValidationException {
        if (idOrganizacion <= 0) {
            throw new ValidationException(
                    "El ID de la organización debe ser mayor a cero. ID recibido: " + idOrganizacion);
        }
        boolean isDeactivated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(UPDATE_LINKED_ORGANIZATION_STATUS_SQL)) {

            ps.setInt(1, idOrganizacion);

            if (ps.executeUpdate() > 0) {
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
            throw new DatabaseException("Error al desactivar la organización vinculada.", sqlException);
        }

        return isDeactivated;
    }

    private LinkedOrganization mapLinkedOrganization(ResultSet rs) throws SQLException {
        return new LinkedOrganization(
                rs.getInt   ("id_organizacion"),
                rs.getString("nombre_organizacion"),
                rs.getString("sector"),
                rs.getString("direccion"),
                ""
        );
    }
}
