package Logic.DAO;

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

import static Logic.Utils.Connection.createdConnection;

public class CoordinatorDAO extends UserDAO implements ICoordinatorDAO {

    private static final Logger LOGGER = Logger.getLogger(CoordinatorDAO.class.getName());

    private  Connection connection;

    public CoordinatorDAO() throws ServiceException {
        super();
       connection = createdConnection();
    }


    @Override
    public boolean save(Coordinator coordinator) throws ServiceException, ValidationException {
        boolean isSaved = false;
        try {
            connection.setAutoCommit(false);
            int userId = super.saveUser(coordinator);
            if (userId > 0) {
                String sql = "INSERT INTO coordinador (id_usuario) VALUES (?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    if (ps.executeUpdate() > 0) {
                        connection.commit();
                        isSaved = true;
                    } else {
                        connection.rollback();
                    }
                }
            } else {
                connection.rollback();
            }
        } catch (SQLException sqlException) {
            try { connection.rollback(); } catch (SQLException rollbackEx) { /* Ignore */ }
            throw new ServiceException("Error al registrar coordinador.", sqlException);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ex) { /* Ignore */ }
        }
        return isSaved;
    }

    @Override
    public boolean update(Coordinator c) throws ServiceException, ValidationException {
        return super.update(c);
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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    coordinatorResult = mapCoordinator(rs);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar coordinador con ID {0}: {1}",
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

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapCoordinator(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la lista de coordinadores: {0}",
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
        String sql = "SELECT u.* FROM usuario u " +
                     "JOIN coordinador c ON u.id_usuario = c.id_usuario " +
                     "LEFT JOIN profesor p ON u.id_usuario = p.id_usuario " +
                     "WHERE p.id_usuario IS NULL AND u.estado = 'Activo'";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCoordinator(rs));
            }
        } catch (SQLException sqlException) {
            throw new ServiceException("Error al recuperar coordinadores sin rol de profesor.", sqlException);
        }
        return list;
    }

    private Coordinator mapCoordinator(ResultSet rs) throws SQLException {
        return new Coordinator(
                rs.getInt("id_usuario"),
                rs.getString("matricula"),
                rs.getString("nombre"),
                rs.getString("apellido_paterno"),
                rs.getString("apellido_materno"),
                rs.getString("contrasenia"),
                rs.getString("estado")
        );
    }
}
