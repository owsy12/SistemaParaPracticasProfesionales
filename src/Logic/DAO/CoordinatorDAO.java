package Logic.DAO;

import Logic.DTOs.Coordinator;
import Logic.Interface.ICoordinatorDAO;

import java.sql.*;
import java.util.*;

public class CoordinatorDAO extends UserDAO implements ICoordinatorDAO {

    private Connection connection;

    public CoordinatorDAO(Connection connection) {
        super(connection);
        this.connection = connection;
    }

    @Override
    public boolean save(Coordinator c) {
        try {
            saveUser(c);

            return true;
        } /*catch (SQLException e) {
            throw new RuntimeException(e);
        }*/ finally {

        }
    }

    @Override
    public boolean update(Coordinator c) {
        try {
            return update(c);
        } /*catch (SQLException e) {
            throw new RuntimeException(e);
        }*/finally {

        }
    }

    @Override
    public boolean delete(int id) {
        try {
            return delete(id);
        } /*catch (SQLException e) {
            throw new RuntimeException(e);
        }*/finally {

        }
    }

    @Override
    public Coordinator findById(int id) {

        String sql = "SELECT * FROM usuario WHERE id_usuario=? AND rol='Coordinador'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
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

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public List<Coordinator> findAllCoordinators() {

        List<Coordinator> list = new ArrayList<>();
        String sql = "SELECT * FROM usuario WHERE rol='Coordinador'";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Coordinator(
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