package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Project;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IProjectDAO;

import java.sql.*;
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
                    "estado = ? WHERE id_proyecto = ?";
    private static final String UPDATE_PROJECT_STATUS_SQL =
            "UPDATE proyecto SET estado = 'Cancelado' WHERE id_proyecto = ?";
    private static final String UPDATE_AVAILABLE_SLOT_SQL =
            "UPDATE proyecto " +
                    "SET cupo_disponible = cupo_disponible - 1, " +
                    "    estado = CASE WHEN cupo_disponible - 1 = 0 THEN 'Lleno' ELSE estado END " +
                    "WHERE id_proyecto = ? AND cupo_disponible > 0";

    @Override
    public boolean saveProject(Project project) throws DatabaseException, ValidationException {
        validateProject(project);

        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(INSERT_PROJECT_SQL)) {

            ps.setInt   (1, project.getIdOrganization());
            ps.setInt   (2, project.getIdTechnicalSupervisor());
            ps.setInt   (3, 0);
            ps.setString(4, project.getName());
            ps.setString(5, project.getDescription());
            ps.setDate  (6, Date.valueOf(new java.sql.Date(project.getStartDate().getTime()).toLocalDate()));
            ps.setDate  (7, Date.valueOf(new java.sql.Date(project.getEndDate().getTime()).toLocalDate()));
            ps.setInt   (8, project.getMaximumPlaces());
            ps.setInt   (9, project.getAvaliablePlaces());
            ps.setString(10, "Disponible");

            if (ps.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar proyecto {0}: {1}",
                    new Object[]{project.getName(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al guardar el proyecto.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Project findById(int idProyecto) throws DatabaseException, ValidationException {
        if (idProyecto <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProyecto);
        }

        Project projectResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_PROJECT_BY_ID_SQL)) {

            ps.setInt(1, idProyecto);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    projectResult = mapProject(rs);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar proyecto con ID {0}: {1}",
                    new Object[]{idProyecto, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al buscar el proyecto por ID.", sqlException);
        }

        return projectResult;
    }

    @Override
    public List<Project> findAll() throws DatabaseException {
        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL_PROJECTS_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                projectList.add(mapProject(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todos los proyectos: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al recuperar la lista de proyectos.", sqlException);
        }

        return projectList;
    }

    @Override
    public List<Project> findAllAvailable() throws DatabaseException {
        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL_AVAILABLE_PROJECTS_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                projectList.add(mapProject(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar proyectos disponibles: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al recuperar los proyectos disponibles.", sqlException);
        }

        return projectList;
    }

    @Override
    public List<Project> findByCoordinator(int idCoordinador) throws DatabaseException, ValidationException {
        if (idCoordinador <= 0) {
            throw new ValidationException(
                    "El ID del coordinador debe ser mayor a cero. ID recibido: " + idCoordinador);
        }

        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_PROJECTS_BY_COORDINATOR_SQL)) {

            ps.setInt(1, idCoordinador);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projectList.add(mapProject(rs));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar proyectos del coordinador {0}: {1}",
                    new Object[]{idCoordinador, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al buscar los proyectos del coordinador.", sqlException);
        }

        return projectList;
    }

    @Override
    public boolean update(Project project) throws DatabaseException, ValidationException {
        validateProject(project);

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(UPDATE_PROJECT_SQL)) {

            ps.setInt   (1, project.getIdOrganization());
            ps.setInt   (2, project.getIdTechnicalSupervisor());
            ps.setString(3, project.getName());
            ps.setString(4, project.getDescription());
            ps.setDate  (5, Date.valueOf(new java.sql.Date(project.getStartDate().getTime()).toLocalDate()));
            ps.setDate  (6, Date.valueOf(new java.sql.Date(project.getEndDate().getTime()).toLocalDate()));
            ps.setInt   (7, project.getMaximumPlaces());
            ps.setInt   (8, project.getAvaliablePlaces());
            ps.setString(9, "Disponible");
            ps.setInt   (10, project.getIdProyect());

            if (ps.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar proyecto con ID {0}: {1}",
                    new Object[]{project.getIdProyect(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al actualizar el proyecto.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean cancelProject(int idProyecto) throws DatabaseException, ValidationException {
        if (idProyecto <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProyecto);
        }

        boolean isCanceled = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(UPDATE_PROJECT_STATUS_SQL)) {

            ps.setInt(1, idProyecto);

            if (ps.executeUpdate() > 0) {
                isCanceled = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al cancelar proyecto con ID {0}: {1}",
                    new Object[]{idProyecto, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al cancelar el proyecto.", sqlException);
        }

        return isCanceled;
    }

    @Override
    public boolean decrementAvailableSlot(int idProyecto) throws DatabaseException, ValidationException {
        if (idProyecto <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProyecto);
        }

        boolean isDecremented = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(UPDATE_AVAILABLE_SLOT_SQL)) {

            ps.setInt(1, idProyecto);

            if (ps.executeUpdate() > 0) {
                isDecremented = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al reducir cupo del proyecto {0}: {1}",
                    new Object[]{idProyecto, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al reducir el cupo del proyecto.", sqlException);
        }

        return isDecremented;
    }

    /**
     * Valida que el proyecto y sus campos obligatorios sean válidos.
     *
     * @param project Proyecto a validar.
     * @throws ValidationException Si algún campo requerido es inválido.
     */
    private void validateProject(Project project) throws ValidationException {
        if (project.getStartDate().after(project.getEndDate())) {
            throw new ValidationException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin del proyecto.");
        }
        if (project.getMaximumPlaces() <= 0) {
            throw new ValidationException(
                    "El cupo máximo debe ser mayor a cero. Valor recibido: " + project.getMaximumPlaces());
        }
    }

    private Project mapProject(ResultSet rs) throws SQLException {
        return new Project(
                rs.getInt   ("id_proyecto"),
                rs.getInt   ("id_organizacion"),
                rs.getInt   ("id_tecnico"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getDate  ("fecha_inicio"),
                rs.getDate  ("fecha_fin"),
                rs.getInt   ("cupo_disponible"),
                rs.getInt   ("cupo_maximo")
        );
    }
}
