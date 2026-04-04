package Logic.DAO;

import Logic.DTOs.UserRole;
import Logic.Interface.IUserRoleDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRoleDAO implements IUserRoleDAO {

    private static final Logger LOGGER = Logger.getLogger(UserRoleDAO.class.getName());

    private final Connection connection;

    public UserRoleDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveUserRole(UserRole userRole) {
        String sql = "INSERT INTO usuario_rol (id_usuario, rol) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userRole.getIdUsuario());
            preparedStatement.setString(2, userRole.getRole());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar rol {0} para usuario {1}: {2}",
                    new Object[]{ userRole.getRole(), userRole.getIdUsuario(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public List<UserRole> findRolesByUserId(int idUsuario) {
        List<UserRole> roleList = new ArrayList<>();
        String sql = "SELECT * FROM usuario_rol WHERE id_usuario = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idUsuario);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    roleList.add(mapUserRole(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar roles del usuario {0}: {1}",
                    new Object[]{ idUsuario, sqlException.getMessage() });
        }
        return roleList;
    }

    @Override
    public List<UserRole> findUsersByRole(String role) {
        List<UserRole> userList = new ArrayList<>();
        String sql = "SELECT * FROM usuario_rol WHERE rol = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, role);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userList.add(mapUserRole(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuarios con rol {0}: {1}",
                    new Object[]{ role, sqlException.getMessage() });
        }
        return userList;
    }

    @Override
    public boolean deleteUserRole(int idUsuario, String role) {
        String sql = "DELETE FROM usuario_rol WHERE id_usuario = ? AND rol = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idUsuario);
            preparedStatement.setString(2, role);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar rol {0} del usuario {1}: {2}",
                    new Object[]{ role, idUsuario, sqlException.getMessage() });
            return false;
        }
    }

    private UserRole mapUserRole(ResultSet resultSet) throws SQLException {
        return new UserRole(
                resultSet.getInt("id_usuario"),
                resultSet.getString("rol")
        );
    }
}