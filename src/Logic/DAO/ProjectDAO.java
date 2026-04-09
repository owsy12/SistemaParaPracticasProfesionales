package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Project;
import Logic.Interface.IProjectDAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectDAO implements IProjectDAO {

    private static final Logger LOGGER = Logger.getLogger(ProjectDAO.class.getName());
    private static final String INSERT_PROJECT_SQL =
            "INSERT INTO proyecto " +
            "(id_organizacion, id_tecnico, id_coordinador, nombre, descripcion, " +
            "fecha_inicio, fecha_fin, cupo_maximo, cupo_disponible, estado) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_PROJECT_BY_ID_SQL =
            "SELECT id_proyecto, id_organizacion, id_tecnico, id_coordinador, " +
            "nombre, descripcion, fecha_inicio, fecha_fin, cupo_maximo, " +
            "cupo_disponible, estado FROM proyecto WHERE id_proyecto = ?";
    private static final String SELECT_ALL_PROJECTS_SQL =
            "SELECT id_proyecto, id_organizacion, id_tecnico, id_coordinador, " +
            "nombre, descripcion, fecha_inicio, fecha_fin, cupo_maximo, " +
            "cupo_disponible, estado FROM proyecto";
    private static final String SELECT_ALL_AVAILABLE_PROJECTS_SQL =
            "SELECT id_proyecto, id_organizacion, id_tecnico, id_coordinador, " +
            "nombre, descripcion, fecha_inicio, fecha_fin, cupo_maximo, " +
            "cupo_disponible, estado FROM proyecto WHERE estado = 'Disponible'";
    private static final String SELECT_PROJECTS_BY_COORDINATOR_SQL =
            "SELECT id_proyecto, id_organizacion, id_tecnico, id_coordinador, " +
            "nombre, descripcion, fecha_inicio, fecha_fin, cupo_maximo, " +
            "cupo_disponible, estado FROM proyecto WHERE id_coordinador = ?";
    private static final String UPDATE_PROJECT_SQL =
            "UPDATE proyecto " +
            "SET id_organizacion = ?, id_tecnico = ?, nombre = ?, descripcion = ?, " +
            "fecha_inicio = ?, fecha_fin = ?, cupo_maximo = ?, cupo_disponible = ?, " +
            "estado = ? " +
            "WHERE id_proyecto = ?";
    private static final String UPDATE_PROJECT_STATUS_SQL =
            "UPDATE proyecto SET estado = 'Cancelado' WHERE id_proyecto = ?";
    private static final String UPDATE_AVAILABLE_SLOT_SQL =
            "UPDATE proyecto " +
            "SET cupo_disponible = cupo_disponible - 1, " +
            "    estado = CASE WHEN cupo_disponible - 1 = 0 THEN 'Lleno' ELSE estado END " +
            "WHERE id_proyecto = ? AND cupo_disponible > 0";

    @Override
    public boolean saveProject(Project project) {
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(INSERT_PROJECT_SQL)) {
            preparedStatement.setInt(1, project.getIdOrganization());
            preparedStatement.setInt(2, project.getIdTechnicalSupervisor());
            preparedStatement.setInt(3, 0);  // ID Coordinator not in DTO
            preparedStatement.setString(4, project.getName());
            preparedStatement.setString(5, project.getDescription());
            preparedStatement.setDate(6, Date.valueOf(
                    new java.sql.Date(project.getStartDate().getTime()).toLocalDate()));
            preparedStatement.setDate(7, Date.valueOf(
                    new java.sql.Date(project.getEndDate().getTime()).toLocalDate()));
            preparedStatement.setInt(8, project.getMaximumPlaces());
            preparedStatement.setInt(9, project.getAvaliablePlaces());
            preparedStatement.setString(10, "Disponible");
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error saving project {0}: {1}",
                    new Object[]{project.getName(), sqlException.getMessage()});
            return false;
        }
    }

    @Override
    public Project findById(int idProyecto) {
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_PROJECT_BY_ID_SQL)) {
            preparedStatement.setInt(1, idProyecto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapProject(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error finding project with ID {0}: {1}",
                    new Object[]{idProyecto, sqlException.getMessage()});
        }
        return null;
    }

    @Override
    public List<Project> findAll() {
        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ALL_PROJECTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                projectList.add(mapProject(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error retrieving all projects: {0}",
                    sqlException.getMessage());
        }
        return projectList;
    }

    @Override
    public List<Project> findAllAvailable() {
        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ALL_AVAILABLE_PROJECTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                projectList.add(mapProject(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error retrieving available projects: {0}",
                    sqlException.getMessage());
        }
        return projectList;
    }

    @Override
    public List<Project> findByCoordinator(int idCoordinador) {
        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_PROJECTS_BY_COORDINATOR_SQL)) {
            preparedStatement.setInt(1, idCoordinador);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    projectList.add(mapProject(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error finding projects for coordinator {0}: {1}",
                    new Object[]{idCoordinador, sqlException.getMessage()});
        }
        return projectList;
    }

    @Override
    public boolean update(Project project) {
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UPDATE_PROJECT_SQL)) {
            preparedStatement.setInt(1, project.getIdOrganization());
            preparedStatement.setInt(2, project.getIdTechnicalSupervisor());
            preparedStatement.setString(3, project.getName());
            preparedStatement.setString(4, project.getDescription());
            preparedStatement.setDate(5, Date.valueOf(
                    new java.sql.Date(project.getStartDate().getTime()).toLocalDate()));
            preparedStatement.setDate(6, Date.valueOf(
                    new java.sql.Date(project.getEndDate().getTime()).toLocalDate()));
            preparedStatement.setInt(7, project.getMaximumPlaces());
            preparedStatement.setInt(8, project.getAvaliablePlaces());
            preparedStatement.setString(9, "Disponible");
            preparedStatement.setInt(10, project.getIdProyect());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error updating project with ID {0}: {1}",
                    new Object[]{project.getIdProyect(), sqlException.getMessage()});
            return false;
        }
    }

    @Override
    public boolean cancelProject(int idProyecto) {
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UPDATE_PROJECT_STATUS_SQL)) {
            preparedStatement.setInt(1, idProyecto);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error canceling project with ID {0}: {1}",
                    new Object[]{idProyecto, sqlException.getMessage()});
            return false;
        }
    }

    @Override
    public boolean decrementAvailableSlot(int idProyecto) {
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UPDATE_AVAILABLE_SLOT_SQL)) {
            preparedStatement.setInt(1, idProyecto);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error decrementing available slot for project {0}: {1}",
                    new Object[]{idProyecto, sqlException.getMessage()});
            return false;
        }
    }

    private Project mapProject(ResultSet resultSet) throws SQLException {
        return new Project(
                resultSet.getInt("id_proyecto"),
                resultSet.getInt("id_organizacion"),
                resultSet.getInt("id_tecnico"),
                resultSet.getString("nombre"),
                resultSet.getString("descripcion"),
                resultSet.getDate("fecha_inicio"),
                resultSet.getDate("fecha_fin"),
                resultSet.getInt("cupo_disponible"),
                resultSet.getInt("cupo_maximo")
        );
    }
}