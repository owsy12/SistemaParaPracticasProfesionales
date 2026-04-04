package Logic.DAO;

import Logic.DTOs.LinkedOrganization;
import Logic.Interface.ILinkedOrganizationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LinkedOrganizationDAO implements ILinkedOrganizationDAO {

    private static final Logger LOGGER = Logger.getLogger(LinkedOrganizationDAO.class.getName());

    private final Connection connection;

    public LinkedOrganizationDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveLinkedOrganization(LinkedOrganization linkedOrganization) {
        String sql = "INSERT INTO organizacion_vinculada " +
                "(nombre_organizacion, correo_organizacion, direccion, sector, estado) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, linkedOrganization.getNombreOrganizacion());
            preparedStatement.setString(2, linkedOrganization.getCorreoOrganizacion());
            preparedStatement.setString(3, linkedOrganization.getDireccion());
            preparedStatement.setString(4, linkedOrganization.getSector());
            preparedStatement.setString(5, linkedOrganization.getEstado());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar organización {0}: {1}",
                    new Object[]{ linkedOrganization.getNombreOrganizacion(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public LinkedOrganization findById(int idOrganizacion) {
        String sql = "SELECT * FROM organizacion_vinculada WHERE id_organizacion = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idOrganizacion);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapLinkedOrganization(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar organización con id {0}: {1}",
                    new Object[]{ idOrganizacion, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public List<LinkedOrganization> findAll() {
        List<LinkedOrganization> organizationList = new ArrayList<>();
        String sql = "SELECT * FROM organizacion_vinculada";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                organizationList.add(mapLinkedOrganization(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al obtener todas las organizaciones: {0}",
                    sqlException.getMessage());
        }
        return organizationList;
    }

    @Override
    public List<LinkedOrganization> findAllActive() {
        List<LinkedOrganization> organizationList = new ArrayList<>();
        String sql = "SELECT * FROM organizacion_vinculada WHERE estado = 'Activa'";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                organizationList.add(mapLinkedOrganization(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al obtener organizaciones activas: {0}",
                    sqlException.getMessage());
        }
        return organizationList;
    }

    @Override
    public boolean update(LinkedOrganization linkedOrganization) {
        String sql = "UPDATE organizacion_vinculada " +
                "SET nombre_organizacion = ?, correo_organizacion = ?, " +
                "direccion = ?, sector = ?, estado = ? " +
                "WHERE id_organizacion = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, linkedOrganization.getNombreOrganizacion());
            preparedStatement.setString(2, linkedOrganization.getCorreoOrganizacion());
            preparedStatement.setString(3, linkedOrganization.getDireccion());
            preparedStatement.setString(4, linkedOrganization.getSector());
            preparedStatement.setString(5, linkedOrganization.getEstado());
            preparedStatement.setInt(6, linkedOrganization.getIdOrganizacion());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar organización con id {0}: {1}",
                    new Object[]{ linkedOrganization.getIdOrganizacion(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public boolean deactivateLinkedOrganization(int idOrganizacion) {
        String sql = "UPDATE organizacion_vinculada SET estado = 'Inactiva' WHERE id_organizacion = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idOrganizacion);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al desactivar organización con id {0}: {1}",
                    new Object[]{ idOrganizacion, sqlException.getMessage() });
            return false;
        }
    }

    private LinkedOrganization mapLinkedOrganization(ResultSet resultSet) throws SQLException {
        return new LinkedOrganization(
                resultSet.getInt("id_organizacion"),
                resultSet.getString("nombre_organizacion"),
                resultSet.getString("correo_organizacion"),
                resultSet.getString("direccion"),
                resultSet.getString("sector"),
                resultSet.getString("estado")
        );
    }
}