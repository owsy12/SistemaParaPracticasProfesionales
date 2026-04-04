package Logic.DAO;

import Logic.DTOs.Admin;
import Logic.Interface.IAdminDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdministratorDAO implements IAdminDAO {

    private static final Logger LOGGER = Logger.getLogger(AdministratorDAO.class.getName());

    private final Connection connection;

    public AdministratorDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveAdmin(Admin admin) {
        String sql = "INSERT INTO administrador (id_usuario) VALUES (?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, admin.getId());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar administrador con id {0}: {1}",
                    new Object[]{ admin.getId(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public Admin findById(int id) {
        String sql = "SELECT u.* FROM usuario u " +
                "JOIN administrador a ON u.id_usuario = a.id_usuario " +
                "WHERE u.id_usuario = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAdmin(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar administrador con id {0}: {1}",
                    new Object[]{ id, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public List<Admin> findAll() {
        List<Admin> adminList = new ArrayList<>();
        String sql = "SELECT u.* FROM usuario u " +
                "JOIN administrador a ON u.id_usuario = a.id_usuario";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                adminList.add(mapAdmin(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los administradores: {0}",
                    sqlException.getMessage());
        }
        return adminList;
    }

    private Admin mapAdmin(ResultSet resultSet) throws SQLException {
        return new Admin(
                resultSet.getInt("id_usuario"),
                resultSet.getString("matricula"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido_paterno"),
                resultSet.getString("apellido_materno"),
                resultSet.getString("contrasenia"),
                resultSet.getString("estado")
        );
    }
}