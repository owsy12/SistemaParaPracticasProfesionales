package Logic.DAO;

import Logic.DTOs.UserDTO;
import Logic.Interface.IUserDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDAO {

    private Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean save(UserDTO user) {
        String sql = "INSERT INTO usuario (matricula, nombre, apellido_paterno, apellido_materno, contrasenia, rol, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement prepareStatement = connection.prepareStatement(sql)) {

            prepareStatement.setString(1, user.getMatricula());
            prepareStatement.setString(2, user.getName());
            prepareStatement.setString(3, user.getLastName());
            prepareStatement.setString(4, user.getSecondLastName());
            prepareStatement.setString(5, user.getPassword());
            prepareStatement.setString(6, user.getRole());
            prepareStatement.setString(7, user.getStatus());

            return prepareStatement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("General SQL error in UserDAO.save()", e);
        }
    }

    @Override
    public UserDTO findById(int id) {
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<UserDTO> findAll() {
        List<UserDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapUser(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean update(UserDTO user) {
        String sql = "UPDATE usuario SET nombre=?, apellido_paterno=?, apellido_materno=?, estado=? WHERE id_usuario=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getSecondLastName());
            ps.setString(4, user.getStatus());
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM usuario WHERE id_usuario=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public UserDTO findByMatricula(String matricula) {
        String sql = "SELECT * FROM usuario WHERE matricula=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private UserDTO mapUser(ResultSet rs) throws Exception {
        UserDTO user = new UserDTO();

        user.setId(rs.getInt("id_usuario"));
        user.setMatricula(rs.getString("matricula"));
        user.setName(rs.getString("nombre"));
        user.setLastName(rs.getString("apellido_paterno"));
        user.setSecondLastName(rs.getString("apellido_materno"));
        user.setPassword(rs.getString("contrasenia"));
        user.setRole(rs.getString("rol"));
        user.setStatus(rs.getString("estado"));

        return user;
    }
}