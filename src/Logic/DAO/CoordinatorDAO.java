package Logic.DAO;

import Logic.DTOs.Coordinator;
import Logic.Interface.ICoordinatorDAO;

import java.sql.*;
import java.util.*;

public class CoordinatorDAO extends UserDAO implements ICoordinatorDAO {

    public CoordinatorDAO(Connection connection) {
        super(connection);
    }

    @Override
    public boolean save(Coordinator c) {
        try {
            insertUser(c, "Coordinador");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(Coordinator c) {
        try {
            return updateUser(c);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(int id) {
        try {
            return deleteUser(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CoordinatorDTO findById(int id) {

        String sql = "SELECT * FROM usuario WHERE id_usuario=? AND rol='Coordinador'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new CoordinatorDTO(
                        rs.getInt("id_usuario"),
                        rs.getString("matricula"),
                        rs.getString("nombre"),
                        rs.getString("apellido_paterno"),
                        rs.getString("apellido_materno"),
                        rs.getString("contrasenia"),
                        rs.getString("estado")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public List<Coordinator> findAll() {

        List<CoordinatorDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM usuario WHERE rol='Coordinador'";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new CoordinatorDTO(
                        rs.getInt("id_usuario"),
                        rs.getString("matricula"),
                        rs.getString("nombre"),
                        rs.getString("apellido_paterno"),
                        rs.getString("apellido_materno"),
                        rs.getString("contrasenia"),
                        rs.getString("estado")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}