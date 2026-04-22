package Logic.DAO;

import Logic.DTOs.Coordinator;
import Logic.Exceptions.DatabaseException;
import Logic.Interface.ICoordinatorDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorDAO extends UserDAO implements ICoordinatorDAO {

    private Connection connection;

    public CoordinatorDAO(Connection connection) {
        super(connection);
        this.connection = connection;
    }

    @Override
    public boolean save(Coordinator c) throws DatabaseException {
        boolean isSaved = false;
        if (super.saveUser(c)) {
            isSaved = true;
        }
        return isSaved;
    }

    @Override
    public boolean update(Coordinator c) throws DatabaseException {
        boolean isUpdated = false;
        if (super.update(c)) {
            isUpdated = true;
        }
        return isUpdated;
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        boolean isDeleted = false;
        if (super.delete(id)) {
            isDeleted = true;
        }
        return isDeleted;
    }

    @Override
    public Coordinator findById(int id) throws DatabaseException {
        Coordinator coordinatorResult = null;
        String sql = "SELECT * FROM usuario WHERE id_usuario=? AND rol='Coordinador'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    coordinatorResult = mapCoordinator(rs);
                }
            }

        } catch (SQLException sqlException) {
            throw new DatabaseException("Error al buscar el coordinador por ID.", sqlException);
        }

        return coordinatorResult;
    }

    @Override
    public List<Coordinator> findAllCoordinators() throws DatabaseException {
        List<Coordinator> list = new ArrayList<>();
        String sql = "SELECT * FROM usuario WHERE rol='Coordinador'";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapCoordinator(rs));
            }

        } catch (SQLException sqlException) {
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