package Logic.DAO;

import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IUserDAO;
import static Logic.Utils.Connection.createdConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO implements IUserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private final Connection connection;

    public UserDAO() throws ServiceException {
        connection = createdConnection();
    }

    @Override
    public int saveUser(User user) throws ServiceException, ValidationException {
        validateUser(user);

        int generatedId = -1;
        String sql = "INSERT INTO usuario " +
                "(matricula, nombre, apellido_paterno, apellido_materno, contrasenia, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getMatricula());
            ps.setString(2, user.getFirstName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getSecondLastName());
            ps.setString(5, user.getPassword());
            ps.setString(6, user.getStatus());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        user.setId(generatedId);
                    }
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar usuario con matrícula {0}: {1}",
                    new Object[]{user.getMatricula(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar el usuario en la base de datos.", sqlException);
        }

        return generatedId;
    }

    @Override
    public User findById(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID del usuario debe ser mayor a cero. ID recibido: " + id);
        }

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
            LOGGER.log(Level.SEVERE, "Error al buscar usuario con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar el usuario por ID.", sqlException);
        }

        return userResult;
    }

    @Override
    public List<User> findAll() throws ServiceException {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                userList.add(mapUser(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la lista de usuarios: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar la lista de usuarios.", sqlException);
        }

        return userList;
    }

    @Override
    public boolean update(User user) throws ServiceException, ValidationException {
        validateUser(user);

        boolean isUpdated = false;
        String sql = "UPDATE usuario SET nombre=?, apellido_paterno=?, apellido_materno=?, estado=? " +
                "WHERE id_usuario=?";

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
            LOGGER.log(Level.SEVERE, "Error al actualizar usuario con ID {0}: {1}",
                    new Object[]{user.getId(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al actualizar el usuario.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean delete(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID del usuario debe ser mayor a cero. ID recibido: " + id);
        }

        boolean isDeleted = false;
        String sql = "DELETE FROM usuario WHERE id_usuario=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar usuario con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al eliminar el usuario.", sqlException);
        }

        return isDeleted;
    }

    @Override
    public User findByMatricula(String matricula) throws ServiceException, ValidationException {

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
            LOGGER.log(Level.SEVERE, "Error al buscar usuario por matrícula {0}: {1}",
                    new Object[]{matricula, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar usuario por matrícula.", sqlException);
        }

        return userResult;
    }

    private void validateUser(User user) throws ValidationException {
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId           (rs.getInt   ("id_usuario"));
        user.setMatricula    (rs.getString("matricula"));
        user.setFirstName    (rs.getString("nombre"));
        user.setLastName     (rs.getString("apellido_paterno"));
        user.setSecondLastName(rs.getString("apellido_materno"));
        user.setPassword     (rs.getString("contrasenia"));
        user.setStatus       (rs.getString("estado"));
        return user;
    }
}
