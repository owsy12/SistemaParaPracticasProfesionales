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
                "(matricula, nombre, apellido_paterno, apellido_materno, contrasenia, correo) " +
                "VALUES (?, ?, ?, ?,?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, user.getMatricula());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getSecondLastName());
            preparedStatement.setString(5, user.getPassword());
            preparedStatement.setString(6, user.getEmail());

            if (preparedStatement.executeUpdate() > 0) {
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        generatedId = resultSet.getInt(1);
                        user.setId(generatedId);
                        user.setStatus("Activo");
                    }

                    UserRoleDAO userRoleDAO = new UserRoleDAO();
                    userRoleDAO.saveUserRole(user);
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

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            try (ResultSet rresultSet = preparedStatement.executeQuery()) {
                if (rresultSet.next()) {
                    userResult = mapUser(rresultSet);
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

        try (PreparedStatement ppreparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = ppreparedStatement.executeQuery()) {

            while (resultSet.next()) {
                userList.add(mapUser(resultSet));
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

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getSecondLastName());
            preparedStatement.setString(4, user.getStatus());
            preparedStatement.setInt(5, user.getId());

            if (preparedStatement.executeUpdate() > 0) {
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

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            if (preparedStatement.executeUpdate() > 0) {
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

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, matricula);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    userResult = mapUser(resultSet);
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

    @Override
    public User findByEmail(String email) throws ServiceException, ValidationException {
        User user = new User();
        String sql = "SELECT * FROM usuario WHERE correo=?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    user = mapUser(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuario por correo {0}: {1}",
                    new Object[]{email, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar usuario por correo.", sqlException);
        }
        return user;
    }

    private void validateUser(User user) throws ValidationException {
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId           (resultSet.getInt   ("id_usuario"));
        user.setMatricula    (resultSet.getString("matricula"));
        user.setFirstName    (resultSet.getString("nombre"));
        user.setLastName     (resultSet.getString("apellido_paterno"));
        user.setSecondLastName(resultSet.getString("apellido_materno"));
        user.setPassword     (resultSet.getString("contrasenia"));
        user.setEmail        (resultSet.getString("correo"));
        return user;
    }
}
