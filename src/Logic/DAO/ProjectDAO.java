package Logic.DAO;

import Logic.DTOs.Proyect;
import Logic.Interface.IProjectDAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO implements IProjectDAO {

    private Connection connection;

    public ProjectDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean save(Proyect project) {

        String sql = """
            INSERT INTO proyecto
            (id_organizacion, id_tecnico, id_coordinador, nombre, descripcion,
             fecha_inicio, fecha_fin, cupo_maximo, cupo_disponible, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Disponible')
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, project.getIdOrganization());
            ps.setInt(2, project.getIdTechnician());
            ps.setInt(3, project.getIdCoordinator());
            ps.setString(4, project.getName());
            ps.setString(5, project.getDescription());
            ps.setDate(6, Date.valueOf(project.getStartDate()));
            ps.setDate(7, Date.valueOf(project.getEndDate()));
            ps.setInt(8, project.getMaximumCapacity());
            ps.setInt(9, project.getMaximumCapacity());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Proyect findById(int id) {

        String sql = """
            SELECT id_proyecto, id_organizacion, id_tecnico, id_coordinador,
                   nombre, descripcion, fecha_inicio, fecha_fin,
                   cupo_maximo, cupo_disponible, estado
            FROM proyecto
            WHERE id_proyecto = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapProject(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public List<Proyect> findAll() {

        List<Proyect> list = new ArrayList<>();

        String sql = """
            SELECT id_proyecto, id_organizacion, id_tecnico, id_coordinador,
                   nombre, descripcion, fecha_inicio, fecha_fin,
                   cupo_maximo, cupo_disponible, estado
            FROM proyecto
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapProject(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public boolean update(Proyect project) {

        String sql = """
            UPDATE proyecto
            SET nombre = ?, descripcion = ?, fecha_inicio = ?, fecha_fin = ?,
                cupo_maximo = ?, estado = ?
            WHERE id_proyecto = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setDate(3, Date.valueOf(project.getStartDate()));
            ps.setDate(4, Date.valueOf(project.getEndDate()));
            ps.setInt(5, project.getMaximumCapacity());
            ps.setString(6, project.getState());
            ps.setInt(7, project.getIdProject());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(int id) {

        String sql = "DELETE FROM proyecto WHERE id_proyecto = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Proyect mapProject(ResultSet rs) throws SQLException {
        return new Proyect(
                rs.getInt("id_proyecto"),
                rs.getInt("id_organizacion"),
                rs.getInt("id_tecnico"),
                rs.getInt("id_coordinador"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDate("fecha_inicio").toLocalDate(),
                rs.getDate("fecha_fin").toLocalDate(),
                rs.getInt("cupo_maximo"),
                rs.getInt("cupo_disponible"),
                rs.getString("estado")
        );
    }
}