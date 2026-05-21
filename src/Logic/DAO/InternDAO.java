package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Intern;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IInternDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InternDAO extends UserDAO implements IInternDAO {

    private static final Logger LOGGER = Logger.getLogger(InternDAO.class.getName());
    private static final String INSERT_INTERN_SQL =
            "INSERT INTO practicante (id_usuario, creditos) VALUES (?, ?)";
    private static final String SELECT_INTERN_BY_ID_SQL =
            "SELECT u.id_usuario, u.matricula, u.nombre, u.apellido_paterno, " +
                    "u.apellido_materno, u.contrasenia, p.creditos FROM usuario u " +
                    "JOIN practicante p ON u.id_usuario = p.id_usuario " +
                    "WHERE u.id_usuario = ?";
    private static final String SELECT_ALL_INTERNS_SQL =
            "SELECT u.id_usuario, u.matricula, u.nombre, u.apellido_paterno, " +
                    "u.apellido_materno, u.contrasenia, u.estado, p.creditos FROM usuario u " +
                    "JOIN practicante p ON u.id_usuario = p.id_usuario";
    private static final String UPDATE_INTERN_STATUS_SQL =
            "UPDATE usuario_rol SET estado = 'Inactivo' WHERE id_usuario = ? AND rol = 'Practicante'";
    private static final String UPDATE_INTERN_CREDITS_SQL =
            "UPDATE practicante SET creditos = ? WHERE id_usuario = ?";
    private static final String FIND_ALL_ACTIVE_INTERNS =
            "SELECT u.* FROM usuario u JOIN usuario_rol  ur ON u.id_usuario = ur.id_usuario WHERE ur.rol = 'Practicante' AND ur.estado = 'Activo'";

    public InternDAO() throws ServiceException {
    }

    @Override
    public boolean saveIntern(Intern intern) throws ServiceException, ValidationException {
        boolean isSaved = false;
        int userId = super.saveUser(intern);

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTERN_SQL)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, intern.getCredits());

            if (preparedStatement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar practicante con ID {0}: {1}",
                    new Object[]{intern.getId(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar el practicante en la base de datos.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Intern findById(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + id);
        }
        Intern internResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_INTERN_BY_ID_SQL)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    internResult = mapIntern(resultSet);
                    internResult.setCredits(resultSet.getInt("creditos"));
                }

            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar practicante con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});

            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar el practicante por ID.", sqlException);
        }

        return internResult;
    }

    @Override
    public List<Intern> findAllCoordinators() throws ServiceException {
        List<Intern> internList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_INTERNS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                internList.add(mapIntern(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la lista de practicantes: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar la lista de practicantes.", sqlException);
        }

        return internList;
    }

    @Override
    public boolean deactivateIntern(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + id);
        }
        boolean isDeactivated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_INTERN_STATUS_SQL)) {

            preparedStatement.setInt(1, id);

            if (preparedStatement.executeUpdate() > 0) {
                isDeactivated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al desactivar practicante con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al desactivar el practicante.", sqlException);
        }

        return isDeactivated;
    }

    @Override
    public boolean updateCredits(int id, int credits) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + id);
        }
        if (credits < 0) {
            throw new ValidationException(
                    "Los créditos no pueden ser negativos. Valor recibido: " + credits);
        }
        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_INTERN_CREDITS_SQL)) {

            preparedStatement.setInt(1, credits);
            preparedStatement.setInt(2, id);

            if (preparedStatement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar créditos del practicante con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al actualizar los créditos del practicante.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public List<Intern> findAllActiveinterns() throws ServiceException, ValidationException {
        List<Intern> internList = new ArrayList<>();
        try (Connection connection = DataBaseConnection.connectDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_ACTIVE_INTERNS)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()){
                    internList.add(mapIntern(resultSet));
                }
            }

        }catch (SQLException sqlException) {
            throw new ServiceException("Error en servicio, no se logro recuperar los practicantes",sqlException);
        }

        return internList;
    }

    private Intern mapIntern(ResultSet resultSet) throws SQLException {
        Intern intern = new Intern();
        intern.setId(resultSet.getInt("id_usuario"));
        intern.setMatricula(resultSet.getString("matricula"));
        intern.setFirstName(resultSet.getString("nombre"));
        intern.setLastName(resultSet.getString("apellido_paterno"));
        intern.setSecondLastName(resultSet.getString("apellido_materno"));
        intern.setPassword(resultSet.getString("contrasenia"));

        return intern;
    }
}
