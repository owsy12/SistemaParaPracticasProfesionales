package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.Interface.IUserRoleDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRoleDAO implements IUserRoleDAO {

    private static final Logger LOGGER = Logger.getLogger(UserRoleDAO.class.getName());
    private static final String INSERT_USER_ROLE_SQL =
            "INSERT INTO usuario_rol (id_usuario, rol) VALUES (?, ?)";
    private static final String SELECT_ROLES_BY_USER_ID_SQL =
            "SELECT id_usuario, rol FROM usuario_rol WHERE id_usuario = ?";
    private static final String SELECT_USERS_BY_ROLE_SQL =
            "SELECT id_usuario, rol FROM usuario_rol WHERE rol = ?";
    private static final String DELETE_USER_ROLE_SQL =
            "DELETE FROM usuario_rol WHERE id_usuario = ? AND rol = ?";

    @Override
    public boolean saveUserRole(int userId, String role) {
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(INSERT_USER_ROLE_SQL)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, role);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error saving role {0} for user {1}: {2}",
                    new Object[]{role, userId, sqlException.getMessage()});
            return false;
        }
    }

    @Override
    public List<String> findRolesByUserId(int userId) {
        List<String> roleList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ROLES_BY_USER_ID_SQL)) {
            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    roleList.add(resultSet.getString("rol"));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error finding roles for user {0}: {1}",
                    new Object[]{userId, sqlException.getMessage()});
        }
        return roleList;
    }

    @Override
    public List<Map<String, Object>> findUsersByRole(String role) {
        List<Map<String, Object>> userList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_USERS_BY_ROLE_SQL)) {
            preparedStatement.setString(1, role);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> userRole = new HashMap<>();
                    userRole.put("userId", resultSet.getInt("id_usuario"));
                    userRole.put("role", resultSet.getString("rol"));
                    userList.add(userRole);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error finding users with role {0}: {1}",
                    new Object[]{role, sqlException.getMessage()});
        }
        return userList;
    }

    @Override
    public boolean deleteUserRole(int userId, String role) {
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(DELETE_USER_ROLE_SQL)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, role);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error deleting role {0} from user {1}: {2}",
                    new Object[]{role, userId, sqlException.getMessage()});
            return false;
        }
    }
}