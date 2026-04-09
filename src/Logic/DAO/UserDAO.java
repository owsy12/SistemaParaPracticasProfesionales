package Logic.DAO;

import Logic.DTOs.User;
import Logic.Exceptions.DataAccessException;
import Logic.Interface.IUserDAO;

import java.sql.*;
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
    public boolean saveUser(User user) throws DataAccessException {
        boolean isSaved = false;
        String sql = "INSERT INTO usuario (matricula, nombre, apellido_paterno, apellido_materno, contrasenia, rol, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement prepareStatement = connection.prepareStatement(sql)) {

            prepareStatement.setString(1, user.getMatricula());
            prepareStatement.setString(2, user.getFirstName());
            prepareStatement.setString(3, user.getLastName());
            prepareStatement.setString(4, user.getSecondLastName());
            prepareStatement.setString(5, user.getPassword());
            prepareStatement.setString(7, user.getStatus());

            if (prepareStatement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "General SQL error in UserDAO.save(): {0}", sqlException.getMessage());
            throw new DataAccessException("Error al guardar el usuario en la base de datos.", sqlException);
        }

        return isSaved;
    }

    @Override
    public User findById(int id) throws DataAccessException {
        User userResult = null;
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userResult = mapUser(rs);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding user by ID: {0}", sqlException.getMessage());
            throw new DataAccessException("Error al buscar el usuario por ID.", sqlException);
        }

        return userResult;
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                userList.add(mapUser(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all users: {0}", sqlException.getMessage());
            throw new DataAccessException("Error al recuperar la lista de usuarios.", sqlException);
        }

        return userList;
    }

    @Override
    public boolean update(User user) throws DataAccessException {
        boolean isUpdated = false;
        String sql = "UPDATE usuario SET nombre=?, apellido_paterno=?, apellido_materno=?, estado=? WHERE id_usuario=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getSecondLastName());
            ps.setString(4, user.getStatus());
            ps.setInt(5, user.getId());

            if (ps.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error updating user: {0}", sqlException.getMessage());
            throw new DataAccessException("Error al actualizar el usuario.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean delete(int id) throws DataAccessException {
        boolean isDeleted = false;
        String sql = "DELETE FROM usuario WHERE id_usuario=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deleting user: {0}", sqlException.getMessage());
            throw new DataAccessException("Error al eliminar el usuario.", sqlException);
        }

        return isDeleted;
    }

    @Override
    public User findByMatricula(String matricula) throws DataAccessException {
        User userResult = null;
        String sql = "SELECT * FROM usuario WHERE matricula=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, matricula);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userResult = mapUser(rs);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding user by matricula: {0}", sqlException.getMessage());
            throw new DataAccessException("Error al buscar usuario por matrícula.", sqlException);
        }

        return userResult;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("id_usuario"));
        user.setMatricula(rs.getString("matricula"));
        user.setName(rs.getString("nombre"));
        user.setLastName(rs.getString("apellido_paterno"));
        user.setSecondLastName(rs.getString("apellido_materno"));
        user.setPassword(rs.getString("contrasenia"));
        user.setStatus(rs.getString("estado"));

        return user;
    }
}