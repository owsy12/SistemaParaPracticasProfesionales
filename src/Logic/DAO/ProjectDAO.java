package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
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
                    "(id_organizacion, id_tecnico, id_profesor, nombre, descripcion, " +
                    "fecha_inicio, fecha_fin, cupo_maximo, cupo_disponible, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_PROJECT_BY_ID_SQL =
            "SELECT id_proyecto, id_organizacion, id_tecnico, id_profesor, " +
                    "nombre, descripcion, fecha_inicio, fecha_fin, cupo_maximo, " +
                    "cupo_disponible, estado FROM proyecto WHERE id_proyecto = ?";
    private static final String SELECT_ALL_PROJECTS_SQL =
            "SELECT id_proyecto, id_organizacion, id_tecnico, id_coordinador, " +
                    "nombre, descripcion, fecha_inicio, fecha_fin, cupo_maximo, " +
                    "cupo_disponible, estado FROM proyecto";
    private static final String SELECT_ALL_AVAILABLE_PROJECTS_SQL =
            "SELECT p.id_proyecto, p.id_tecnico, p.id_profesor,\n" +
                    "p.nombre, p.descripcion, p.fecha_inicio, p.fecha_fin, p.cupo_maximo,\n" +
                    "p.cupo_disponible, ov.id_organizacion, ov.nombre_organizacion " +
                    "FROM proyecto p JOIN spp.organizacion_vinculada ov on ov.id_organizacion = p.id_organizacion WHERE p.estado = 'Disponible';";
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
    public boolean saveProject(Project project) throws ServiceException, ValidationException {
        validateProject(project);

        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PROJECT_SQL)) {

            preparedStatement.setInt   (1, project.getIdOrganization());
            preparedStatement.setInt   (2, project.getIdTechnicalSupervisor());
            preparedStatement.setInt   (3, project.getIdProfessor());
            preparedStatement.setString(4, project.getName());
            preparedStatement.setString(5, project.getDescription());
            preparedStatement.setDate  (6, java.sql.Date.valueOf(project.getStartDate()));
            preparedStatement.setDate  (7, java.sql.Date.valueOf(project.getEndDate()));
            preparedStatement.setInt   (8, project.getMaximumPlaces());
            preparedStatement.setInt   (9, project.getAvaliablePlaces());
            preparedStatement.setString(10, "Disponible");

            if (preparedStatement.executeUpdate() > 0) {
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
            throw new ServiceException("Error al guardar el proyecto.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Project findById(int idProyecto) throws ServiceException, ValidationException {
        if (idProyecto <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProyecto);
        }

        Project projectResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PROJECT_BY_ID_SQL)) {

            preparedStatement.setInt(1, idProyecto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    projectResult = mapProject(resultSet);
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
            throw new ServiceException("Error al buscar el proyecto por ID.", sqlException);
        }

        return projectResult;
    }

    @Override
    public List<Project> findAll() throws ServiceException {
        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_PROJECTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                projectList.add(mapProject(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todos los proyectos: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar la lista de proyectos.", sqlException);
        }

        return projectList;
    }

    @Override
    public List<Project> findAllAvailable() throws ServiceException {
        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_AVAILABLE_PROJECTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Project project = mapProject(resultSet);
                project.setOrganizationName(resultSet.getString("nombre_organizacion"));
                projectList.add(project);
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar proyectos disponibles: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar los proyectos disponibles.", sqlException);
        }

        return projectList;
    }

    @Override
    public List<Project> findByCoordinator(int idCoordinador) throws ServiceException, ValidationException {
        if (idCoordinador <= 0) {
            throw new ValidationException(
                    "El ID del coordinador debe ser mayor a cero. ID recibido: " + idCoordinador);
        }

        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PROJECTS_BY_COORDINATOR_SQL)) {

            preparedStatement.setInt(1, idCoordinador);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    projectList.add(mapProject(resultSet));
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
            throw new ServiceException("Error al buscar los proyectos del coordinador.", sqlException);
        }

        return projectList;
    }

    @Override
    public boolean update(Project project) throws ServiceException, ValidationException {
        validateProject(project);

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PROJECT_SQL)) {

            preparedStatement.setInt   (1, project.getIdOrganization());
            preparedStatement.setInt   (2, project.getIdTechnicalSupervisor());
            preparedStatement.setString(3, project.getName());
            preparedStatement.setString(4, project.getDescription());
            preparedStatement.setDate  (5, java.sql.Date.valueOf(project.getStartDate()));
            preparedStatement.setDate  (6, java.sql.Date.valueOf(project.getStartDate()));
            preparedStatement.setInt   (7, project.getMaximumPlaces());
            preparedStatement.setInt   (8, project.getAvaliablePlaces());
            preparedStatement.setString(9, "Disponible");
            preparedStatement.setInt   (10, project.getIdProyect());

            if (preparedStatement.executeUpdate() > 0) {
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
            throw new ServiceException("Error al actualizar el proyecto.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean cancelProject(int idProyecto) throws ServiceException, ValidationException {
        if (idProyecto <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProyecto);
        }

        boolean isCanceled = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PROJECT_STATUS_SQL)) {

            preparedStatement.setInt(1, idProyecto);

            if (preparedStatement.executeUpdate() > 0) {
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
            throw new ServiceException("Error al cancelar el proyecto.", sqlException);
        }

        return isCanceled;
    }

    @Override
    public boolean decrementAvailableSlot(int idProyecto) throws ServiceException, ValidationException {
        if (idProyecto <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProyecto);
        }

        boolean isDecremented = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_AVAILABLE_SLOT_SQL)) {

            preparedStatement.setInt(1, idProyecto);

            if (preparedStatement.executeUpdate() > 0) {
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
            throw new ServiceException("Error al reducir el cupo del proyecto.", sqlException);
        }

        return isDecremented;
    }

    private void validateProject(Project project) throws ValidationException {
        if (project.getStartDate().isAfter(project.getEndDate())) {
            throw new ValidationException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin del proyecto.");
        }
        if (project.getMaximumPlaces() <= 0) {
            throw new ValidationException(
                    "El cupo máximo debe ser mayor a cero. Valor recibido: " + project.getMaximumPlaces());
        }
    }

    private Project mapProject(ResultSet resultSet) throws SQLException {
        return new Project(
                resultSet.getInt   ("id_proyecto"),
                resultSet.getInt   ("id_organizacion"),
                resultSet.getInt   ("id_tecnico"),
                resultSet.getString("nombre"),
                resultSet.getString("descripcion"),
                resultSet.getDate  ("fecha_inicio").toLocalDate(),
                resultSet.getDate  ("fecha_fin").toLocalDate(),
                resultSet.getInt   ("cupo_disponible"),
                resultSet.getInt   ("cupo_maximo")
        );
    }
}
