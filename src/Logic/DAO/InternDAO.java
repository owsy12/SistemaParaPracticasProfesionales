package Logic.DAO;

import Logic.DTOs.Intern;
import Logic.Interface.IInternDAO;

import java.sql.*;
import java.util.*;

public class InternDAO extends UserDAO implements IInternDAO {
    private Connection connection;

    public InternDAO(Connection connection) {
        super(connection);
    }

    @Override
    public boolean save(Intern i) {

        String sqlIntern = "INSERT INTO practicante (id_usuario, creditos) VALUES (?,?)";

        try {
            connection.setAutoCommit(false);

            int id = saveUser(i);

            PreparedStatement ps = connection.prepareStatement(sqlIntern);
            ps.setInt(1, id);
            ps.setInt(2, i.getCredits());
            ps.executeUpdate();

            connection.commit();
            return true;

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { throw new RuntimeException(ex); }
            throw new RuntimeException(e);
        }
    }



    @Override
    public boolean update(Intern i) {

        String sqlIntern = "UPDATE practicante SET creditos=? WHERE id_usuario=?";

        try {
            connection.setAutoCommit(false);

            update(i);

            PreparedStatement ps = connection.prepareStatement(sqlIntern);
            ps.setInt(1, i.getCredits());
            ps.setInt(2, i.getId());
            ps.executeUpdate();

            connection.commit();
            return true;

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { throw new RuntimeException(ex); }
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(int id) {
        try {
            return delete(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Intern findById(int id) {

        String sql = """
            SELECT u.*, i.creditos
            FROM usuario u
            JOIN practicante i ON u.id_usuario = i.id_usuario
            WHERE u.id_usuario = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Intern(
                        rs.getInt("id_usuario"),
                        rs.getString("matricula"),
                        rs.getString("nombre"),
                        rs.getString("apellido_paterno"),
                        rs.getString("apellido_materno"),
                        rs.getString("contrasenia"),
                        rs.getString("estado"),
                        rs.getInt("creditos")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public List<Intern> findAll() {
        return new ArrayList<>();
    }
}