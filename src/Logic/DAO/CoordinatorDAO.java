package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Coordinator;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.ICoordinatorDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoordinatorDAO extends UserDAO implements ICoordinatorDAO {

    private static final Logger LOGGER = Logger.getLogger(CoordinatorDAO.class.getName());

    public CoordinatorDAO() throws ServiceException {
        super();
    }

    @Override
    public boolean save(Coordinator coordinator) throws ServiceException, ValidationException {
        if (countActiveCoordinators() >= 1) {
            throw new ValidationException("Ya existe un coordinador activo en el sistema.");
        }
        boolean isSaved = false;
        int userId = super.saveUser(coordinator);
        if (userId > 0) {
            String sql = "INSERT INTO coordinador (id_usuario) VALUES (?)";
            try (Connection connection = DataBaseConnection.connectDatabase();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, userId);
                if (preparedStatement.executeUpdate() > 0) {
                    isSaved = true;
                }
            } catch (SQLException sqlException) {
                LOGGER.log(Level.SEVERE, "Error registering coordinator: {0}",
                        sqlException.getMessage());
                throw new ServiceException("Error al registrar coordinador.", sqlException);
            }
        }
        return isSaved;
    }

    @Override
    public boolean update(Coordinator coordinator) throws ServiceException, ValidationException {
        return super.update(coordinator);
    }

    @Override
    public boolean delete(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID del coordinador debe ser mayor a cero. ID recibido: " + id);
        }
        return super.delete(id);
    }

    @Override
    public Coordinator findById(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID del coordinador debe ser mayor a cero. ID recibido: " + id);
        }
        Coordinator coordinatorResult = null;
        String sql = "SELECT * FROM usuario u " +
                "JOIN coordinador c ON u.id_usuario = c.id_usuario " +
                "WHERE u.id_usuario = ?";

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    coordinatorResult = mapCoordinator(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding coordinator with ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});

            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }

            throw new ServiceException("Error al buscar el coordinador por ID.", sqlException);
        }

        return coordinatorResult;
    }

    @Override
    public List<Coordinator> findAllCoordinators() throws ServiceException {
        List<Coordinator> list = new ArrayList<>();
        String sql = "SELECT u.* FROM usuario u " +
                "JOIN coordinador c ON u.id_usuario = c.id_usuario";

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                list.add(mapCoordinator(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving coordinator list: {0}",
                    sqlException.getMessage());

            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }

            throw new ServiceException("Error al recuperar la lista de coordinadores.", sqlException);
        }

        return list;
    }

    @Override
    public List<Coordinator> findCoordinatorsWithoutProfessorRole() throws ServiceException {
        List<Coordinator> list = new ArrayList<>();
        String sql = "SELECT u.*" +
                "FROM usuario u " +
                "JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                "WHERE ur.rol = 'Coordinador' " +
                "AND ur.estado = 'Activo' " +
                "AND NOT EXISTS ( " +
                "    SELECT 1 " +
                "    FROM usuario_rol ur2 " +
                "    WHERE ur2.id_usuario = u.id_usuario " +
                "    AND ur2.rol <> 'Coordinador'" +
                "    AND ur2.estado = 'Activo')";

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                list.add(mapCoordinator(resultSet));
            }

        } catch (SQLException sqlException) {
            throw new ServiceException("Error al recuperar coordinadores sin rol de profesor.", sqlException);
        }

        return list;
    }

    @Override
    public List<Coordinator> findActiveCoordinators() throws ServiceException {
        List<Coordinator> coordinators = new ArrayList<>();
        String sql = "SELECT u.* " +
                "FROM usuario u " +
                "JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                "WHERE ur.estado = 'Activo' " +
                "AND ur.rol = 'Coordinador' ";

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                coordinators.add(mapCoordinator(resultSet));
            }

        } catch (SQLException sqlException) {
            throw new ServiceException("Error al recuperar coordinadores activos.", sqlException);
        }
        return coordinators;
    }

    private int countActiveCoordinators() throws ServiceException {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM usuario_rol WHERE rol = 'Coordinador' AND estado = 'Activo'";
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error counting active coordinators: {0}",
                    sqlException.getMessage());
            throw new ServiceException("Error al verificar coordinadores activos.", sqlException);
        }
        return count;
    }

    private Coordinator mapCoordinator(ResultSet resultSet) throws SQLException {
        Coordinator coordinator = new Coordinator();
        coordinator.setIdUser(resultSet.getInt("id_usuario"));
        coordinator.setRegistrationNumber(resultSet.getString("matricula"));
        coordinator.setFirstName(resultSet.getString("nombre"));
        coordinator.setLastName(resultSet.getString("apellido_paterno"));
        coordinator.setSecondLastName(resultSet.getString("apellido_materno"));
        coordinator.setPassword(resultSet.getString("contrasenia"));

        return coordinator;
    }
}
