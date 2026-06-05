package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Administrator;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IAdministratorDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdministratorDAO implements IAdministratorDAO {

    private static final Logger LOGGER = Logger.getLogger(AdministratorDAO.class.getName());
    private static final String INSERT_ADMINISTRATOR_SQL =
            "INSERT INTO administrador (id_usuario) VALUES (?)";
    private static final String SELECT_ADMINISTRATOR_BY_ID_SQL =
            "SELECT u.id_usuario, u.matricula, u.nombre, u.apellido_paterno, " +
                    "u.apellido_materno, u.contrasenia, u.estado FROM usuario u " +
                    "JOIN administrador a ON u.id_usuario = a.id_usuario " +
                    "WHERE u.id_usuario = ?";
    private static final String SELECT_ALL_ADMINISTRATORS_SQL =
            "SELECT u.id_usuario, u.matricula, u.nombre, u.apellido_paterno, " +
                    "u.apellido_materno, u.contrasenia, u.estado FROM usuario u " +
                    "JOIN administrador a ON u.id_usuario = a.id_usuario";

    @Override
    public boolean saveAdmin(Administrator administrator) throws ServiceException, ValidationException {
        if (administrator.getId() <= 0) {
            throw new ValidationException(
                    "El ID del administrador debe ser mayor a cero. ID recibido: " + administrator.getId());
        }
        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ADMINISTRATOR_SQL)) {

            preparedStatement.setInt(1, administrator.getId());
            if (preparedStatement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar administrador con ID {0}: {1}",
                    new Object[]{administrator.getId(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar el administrador en la base de datos.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Administrator findById(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    "El ID del administrador debe ser mayor a cero. ID recibido: " + id);
        }
        Administrator administratorResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ADMINISTRATOR_BY_ID_SQL)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    administratorResult = mapAdministrator(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar administrador con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar el administrador por ID.", sqlException);
        }

        return administratorResult;
    }

    @Override
    public List<Administrator> findAll() throws ServiceException {
        List<Administrator> administratorList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_ADMINISTRATORS_SQL);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                administratorList.add(mapAdministrator(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la lista de administradores: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar la lista de administradores.", sqlException);
        }

        return administratorList;
    }

    private Administrator mapAdministrator(ResultSet resultSet) throws SQLException {
        Administrator administrator = new Administrator();
        administrator.setId(resultSet.getInt("id_usuario"));
        administrator.setRegistrationNumber(resultSet.getString("matricula"));
        administrator.setFirstName(resultSet.getString("nombre"));
        administrator.setLastName(resultSet.getString("apellido_paterno"));
        administrator.setSecondLastName(resultSet.getString("apellido_materno"));
        administrator.setPassword(resultSet.getString("contrasenia"));
        administrator.setStatus(resultSet.getString("estado"));

        return administrator;
    }
}
