package Logic.DAO;

import Logic.DTOs.Project;
import Logic.Interface.IProjectDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectDAO implements IProjectDAO {

    private static final Logger LOGGER = Logger.getLogger(ProjectDAO.class.getName());

    private final Connection connection;

    public ProjectDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveProject(Project project) {
        String sql = "INSERT INTO proyecto " +
                "(id_organizacion, id_tecnico, id_coordinador, nombre, descripcion, " +
                "fecha_inicio, fecha_fin, cupo_maximo, cupo_disponible, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, project.getIdOrganizacion());
            preparedStatement.setInt(2, project.getIdTecnico());
            preparedStatement.setInt(3, project.getIdCoordinador());
            preparedStatement.setString(4, project.getNombre());
            preparedStatement.setString(5, project.getDescripcion());
            preparedStatement.setDate(6, Date.valueOf(project.getFechaInicio()));
            preparedStatement.setDate(7, Date.valueOf(project.getFechaFin()));
            preparedStatement.setInt(8, project.getCupoMaximo());
            preparedStatement.setInt(9, project.getCupoDisponible());
            preparedStatement.setString(10, project.getEstado());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar proyecto {0}: {1}",
                    new Object[]{ project.getNombre(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public Project findById(int idProyecto) {
        String sql = "SELECT * FROM proyecto WHERE id_proyecto = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idProyecto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapProject(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar proyecto con id {0}: {1}",
                    new Object[]{ idProyecto, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public List<Project> findAll() {
        List<Project> projectList = new ArrayList<>();
        String sql = "SELECT * FROM proyecto";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                projectList.add(mapProject(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los proyectos: {0}",
                    sqlException.getMessage());
        }
        return projectList;
    }

    @Override
    public List<Project> findAllAvailable() {
        List<Project> projectList = new ArrayList<>();
        String sql = "SELECT * FROM proyecto WHERE estado = 'Disponible'";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                projectList.add(mapProject(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al obtener proyectos disponibles: {0}",
                    sqlException.getMessage());
        }
        return projectList;
    }

    @Override
    public List<Project> findByCoordinator(int idCoordinador) {
        List<Project> projectList = new ArrayList<>();
        String sql = "SELECT * FROM proyecto WHERE id_coordinador = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idCoordinador);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    projectList.add(mapProject(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar proyectos del coordinador {0}: {1}",
                    new Object[]{ idCoordinador, sqlException.getMessage() });
        }
        return projectList;
    }

    @Override
    public boolean update(Project project) {
        String sql = "UPDATE proyecto " +
                "SET id_organizacion = ?, id_tecnico = ?, nombre = ?, descripcion = ?, " +
                "fecha_inicio = ?, fecha_fin = ?, cupo_maximo = ?, cupo_disponible = ?, " +
                "estado = ? " +
                "WHERE id_proyecto = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, project.getIdOrganizacion());
            preparedStatement.setInt(2, project.getIdTecnico());
            preparedStatement.setString(3, project.getNombre());
            preparedStatement.setString(4, project.getDescripcion());
            preparedStatement.setDate(5, Date.valueOf(project.getFechaInicio()));
            preparedStatement.setDate(6, Date.valueOf(project.getFechaFin()));
            preparedStatement.setInt(7, project.getCupoMaximo());
            preparedStatement.setInt(8, project.getCupoDisponible());
            preparedStatement.setString(9, project.getEstado());
            preparedStatement.setInt(10, project.getIdProyecto());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar proyecto con id {0}: {1}",
                    new Object[]{ project.getIdProyecto(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public boolean cancelProject(int idProyecto) {
        String sql = "UPDATE proyecto SET estado = 'Cancelado' WHERE id_proyecto = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idProyecto);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al cancelar proyecto con id {0}: {1}",
                    new Object[]{ idProyecto, sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public boolean decrementAvailableSlot(int idProyecto) {
        String sql = "UPDATE proyecto " +
                "SET cupo_disponible = cupo_disponible - 1, " +
                "    estado = CASE WHEN cupo_disponible - 1 = 0 THEN 'Lleno' ELSE estado END " +
                "WHERE id_proyecto = ? AND cupo_disponible > 0";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idProyecto);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al decrementar cupo del proyecto {0}: {1}",
                    new Object[]{ idProyecto, sqlException.getMessage() });
            return false;
        }
    }

    private Project mapProject(ResultSet resultSet) throws SQLException {
        return new Project(
                resultSet.getInt("id_proyecto"),
                resultSet.getInt("id_organizacion"),
                resultSet.getInt("id_tecnico"),
                resultSet.getInt("id_coordinador"),
                resultSet.getString("nombre"),
                resultSet.getString("descripcion"),
                resultSet.getDate("fecha_inicio").toLocalDate(),
                resultSet.getDate("fecha_fin").toLocalDate(),
                resultSet.getInt("cupo_maximo"),
                resultSet.getInt("cupo_disponible"),
                resultSet.getString("estado")
        );
    }
}