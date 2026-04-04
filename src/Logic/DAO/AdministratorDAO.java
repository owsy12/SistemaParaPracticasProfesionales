package Logic.DAO;

import Logic.DTOs.Administrator;
import Logic.Interface.IAdminDAO;

import java.sql.*;
import java.util.*;

public class AdministratorDAO implements IAdminDAO {

    private Connection connection;

    public AdministratorDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Administrator findById(int id) {

        String sql = """
            SELECT u.*
            FROM usuario u
            WHERE u.id_usuario = ? AND u.rol = 'Administrador'
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Administrator(
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
    public List<Administrator> findAll() {

        List<Administrator> list = new ArrayList<>();

        String sql = "SELECT * FROM usuario WHERE rol = 'Administrador'";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Administrator(
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

    @Override
    public boolean save(Administrator a) {

        String sql = """
            INSERT INTO usuario 
            (matricula,nombre,apellido_paterno,apellido_materno,contrasenia,rol,estado)
            VALUES (?,?,?,?,?,'Administrador','Activo')
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, a.getMatricula());
            ps.setString(2, a.getFirstName());
            ps.setString(3, a.getLastName());
            ps.setString(4, a.getSecondLastName());
            ps.setString(5, a.getPassword());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public boolean update(Administrator a) {

        String sql = """
            UPDATE usuario 
            SET matricula=?, nombre=?, apellido_paterno=?, apellido_materno=?, contrasenia=?, estado=?
            WHERE id_usuario=? AND rol='Administrador'
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, a.getMatricula());
            ps.setString(2, a.getFirstName());
            ps.setString(3, a.getLastName());
            ps.setString(4, a.getSecondLastName());
            ps.setString(5, a.getPassword());
            ps.setString(6, a.getStatus());
            ps.setInt(7, a.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(int id) {

        String sql = "DELETE FROM usuario WHERE id_usuario=? AND rol='Administrador'";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}