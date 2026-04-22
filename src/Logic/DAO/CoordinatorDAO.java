package Logic.DAO;

import Logic.DTOs.Coordinator;
import Logic.Exceptions.DatabaseException;
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

    private final Connection connection;

    public CoordinatorDAO(Connection connection) throws ValidationException {
        super(connection);
        this.connection = connection;
    }

    @Override
    public boolean save(Coordinator c) throws DatabaseException, ValidationException {
        return super.saveUser(c);
    }

    @Override
    public boolean update(Coordinator c) throws DatabaseException, ValidationException {
        return super.update(c);
    }

    @Override
    public boolean delete(int id) throws DatabaseException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("El ID del coordinador debe ser mayor a cero. ID recibido: " + id);
        }
        return super.delete(id);
    }

    @Override
    public Coordinator findById(int id) throws DatabaseException, ValidationException {
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
            throw new DatabaseException("Error al buscar el coordinador por ID.", sqlException);
        }

        return coordinatorResult;
    }

    @Override
    public List<Coordinator> findAllCoordinators() throws DatabaseException {
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
            throw new DatabaseException("Error al recuperar la lista de coordinadores.", sqlException);
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
