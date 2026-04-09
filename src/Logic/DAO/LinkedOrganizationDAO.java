package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.LinkedOrganization;
import Logic.Interface.ILinkedOrganizationDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public boolean saveLinkedOrganization(LinkedOrganization linkedOrganization) {
        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_LINKED_ORGANIZATION_SQL)) {

            preparedStatement.setString(1, linkedOrganization.getName());
            preparedStatement.setString(2, "");  // Email not in DTO
            preparedStatement.setString(3, linkedOrganization.getAdress());
            preparedStatement.setString(4, linkedOrganization.getSector());
            preparedStatement.setString(5, "Activa");

            if (preparedStatement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving linked organization {0}: {1}",
                    new Object[]{linkedOrganization.getName(), sqlException.getMessage()});
        }

        return isSaved;
    }

    @Override
    public Optional<LinkedOrganization> findById(int idOrganizacion) {
        Optional<LinkedOrganization> organizationResult = Optional.empty();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LINKED_ORGANIZATION_BY_ID_SQL)) {

            preparedStatement.setInt(1, idOrganizacion);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    organizationResult = Optional.of(mapLinkedOrganization(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding linked organization with ID {0}: {1}",
                    new Object[]{idOrganizacion, sqlException.getMessage()});
        }

        return organizationResult;
    }

    @Override
    public List<LinkedOrganization> findAll() {
        List<LinkedOrganization> organizationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_LINKED_ORGANIZATIONS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                organizationList.add(mapLinkedOrganization(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all linked organizations: {0}",
                    sqlException.getMessage());
        }

        return organizationList;
    }

    @Override
    public List<LinkedOrganization> findAllActive() {
        List<LinkedOrganization> organizationList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_ACTIVE_LINKED_ORGANIZATIONS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                organizationList.add(mapLinkedOrganization(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving active linked organizations: {0}",
                    sqlException.getMessage());
        }

        return organizationList;
    }

    @Override
    public boolean update(LinkedOrganization linkedOrganization) {
        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_LINKED_ORGANIZATION_SQL)) {

            preparedStatement.setString(1, linkedOrganization.getName());
            preparedStatement.setString(2, "");  // Email not in DTO
            preparedStatement.setString(3, linkedOrganization.getAdress());
            preparedStatement.setString(4, linkedOrganization.getSector());
            preparedStatement.setString(5, "Activa");
            preparedStatement.setInt(6, linkedOrganization.getIdLinkedOrganization());

            if (preparedStatement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error updating linked organization with ID {0}: {1}",
                    new Object[]{linkedOrganization.getIdLinkedOrganization(), sqlException.getMessage()});
        }

        return isUpdated;
    }

    @Override
    public boolean deactivateLinkedOrganization(int idOrganizacion) {
        boolean isDeactivated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_LINKED_ORGANIZATION_STATUS_SQL)) {

            preparedStatement.setInt(1, idOrganizacion);

            if (preparedStatement.executeUpdate() > 0) {
                isDeactivated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deactivating linked organization with ID {0}: {1}",
                    new Object[]{idOrganizacion, sqlException.getMessage()});
        }

        return isDeactivated;
    }

    private LinkedOrganization mapLinkedOrganization(ResultSet resultSet) throws SQLException {
        return new LinkedOrganization(
                resultSet.getInt("id_organizacion"),
                resultSet.getString("nombre_organizacion"),
                resultSet.getString("sector"),
                resultSet.getString("direccion"),
                ""
        );
    }
}