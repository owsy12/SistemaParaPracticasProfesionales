package Logic.DAO;

import Logic.DTOs.User;
import Logic.Interface.IUserDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO implements IUserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    private static final String INSERT_USER_SQL =
            "INSERT INTO usuario (matricula, nombre, apellido_paterno, apellido_materno, contrasenia, rol, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_USER_BY_ID_SQL =
            "SELECT * FROM usuario WHERE id_usuario = ?";
    private static final String SELECT_ALL_USERS_SQL =
            "SELECT * FROM usuario";
    private static final String UPDATE_USER_SQL =
            "UPDATE usuario SET nombre=?, apellido_paterno=?, apellido_materno=?, estado=? WHERE id_usuario=?";
    private static final String DELETE_USER_SQL =
            "DELETE FROM usuario WHERE id_usuario=?";
    private static final String SELECT_USER_BY_MATRICULA_SQL =
            "SELECT * FROM usuario WHERE matricula=?";

    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveUser(User user) {
        boolean isSaved = false;

        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER_SQL)) {

            preparedStatement.setString(1, user.getMatricula());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getSecondLastName());
            preparedStatement.setString(5, user.getPassword());
            preparedStatement.setString(7, user.getStatus());

            if (preparedStatement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "General SQL error in UserDAO.save(): {0}", sqlException.getMessage());
        }

        return isSaved;
    }

    @Override
    public Optional<User> findById(int id) {
        Optional<User> userResult = Optional.empty();

        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    userResult = Optional.of(mapUser(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding user by ID: {0}", sqlException.getMessage());
        }

        return userResult;
    }

    @Override
    public List<User> findAll() {
        List<User> userList = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                userList.add(mapUser(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all users: {0}", sqlException.getMessage());
        }

        return userList;
    }

    @Override
    public boolean update(User user) {
        boolean isUpdated = false;

        try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_USER_SQL)) {

            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getSecondLastName());
            preparedStatement.setString(4, user.getStatus());
            preparedStatement.setInt(5, user.getId());

            if (preparedStatement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error updating user: {0}", sqlException.getMessage());
        }

        return isUpdated;
    }

    @Override
    public boolean delete(int id) {
        boolean isDeleted = false;

        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_USER_SQL)) {
            preparedStatement.setInt(1, id);

            if (preparedStatement.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deleting user: {0}", sqlException.getMessage());
        }

        return isDeleted;
    }

    @Override
    public Optional<User> findByMatricula(String matricula) {
        Optional<User> userResult = Optional.empty();

        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_MATRICULA_SQL)) {
            preparedStatement.setString(1, matricula);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    userResult = Optional.of(mapUser(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding user by matricula: {0}", sqlException.getMessage());
        }

        return userResult;
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id_usuario"));
        user.setMatricula(resultSet.getString("matricula"));
        user.setName(resultSet.getString("nombre"));
        user.setLastName(resultSet.getString("apellido_paterno"));
        user.setSecondLastName(resultSet.getString("apellido_materno"));
        user.setPassword(resultSet.getString("contrasenia"));
        user.setStatus(resultSet.getString("estado"));
        return user;
    }
}