package Logic.DAO;

import Logic.DTOs.User;
import Logic.Interface.IUserDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO implements IUserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveUser(User user) {
        String sql = "INSERT INTO usuario " +
                "(matricula, nombre, apellido_paterno, apellido_materno, contrasena, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getMatricula());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getSecondLastName());
            preparedStatement.setString(5, user.getPassword());
            preparedStatement.setString(6, user.getStatus());

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar usuario con matricula {0}: {1}",
                    new Object[]{ user.getMatricula(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuario con id {0}: {1}",
                    new Object[]{ id, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public User findByMatricula(String matricula) {
        String sql = "SELECT * FROM usuario WHERE matricula = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, matricula);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuario con matricula {0}: {1}",
                    new Object[]{ matricula, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                userList.add(mapUser(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los usuarios: {0}",
                    sqlException.getMessage());
        }
        return userList;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE usuario " +
                "SET nombre = ?, apellido_paterno = ?, apellido_materno = ?, estado = ? " +
                "WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getSecondLastName());
            preparedStatement.setString(4, user.getStatus());
            preparedStatement.setInt(5, user.getId());

            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar usuario con id {0}: {1}",
                    new Object[]{ user.getId(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar usuario con id {0}: {1}",
                    new Object[]{ id, sqlException.getMessage() });
            return false;
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("matricula"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido_paterno"),
                resultSet.getString("apellido_materno"),
                resultSet.getString("contrasena"),
                resultSet.getString("estado")
        );
    }
}