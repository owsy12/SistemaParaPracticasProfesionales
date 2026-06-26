package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ReferentialIntegrityException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IProjectDAO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectDAO implements IProjectDAO {
    private static final String STATUS_AVAILABLE = "Disponible";


    private static final Logger LOGGER = Logger.getLogger(ProjectDAO.class.getName());

    private static final String STATUS_FULL = "Lleno";
    private static final String STATUS_CANCELLED = "Cancelado";

    private static final String INSERT_PROJECT_SQL =
            "INSERT INTO proyecto " +
                    "(id_organizacion, id_tecnico, id_profesor, nombre, descripcion, objetivo, " +
                    "fecha_inicio, fecha_fin, cupo_maximo, cupo_disponible, estado, nrc, periodo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_PROJECT_BY_ID_SQL =
            "SELECT p.id_proyecto, p.id_organizacion, p.id_tecnico, p.id_profesor, " +
            "       p.nombre, p.descripcion, p.objetivo, p.fecha_inicio, p.fecha_fin, " +
            "       p.cupo_maximo, p.cupo_disponible, p.estado, p.nrc, p.periodo, " +
            "       ov.nombre_organizacion " +
            "FROM proyecto p " +
            "JOIN spp.organizacion_vinculada ov ON ov.id_organizacion = p.id_organizacion " +
            "WHERE p.id_proyecto = ?";
    private static final String SELECT_ALL_PROJECTS_SQL =
            "SELECT p.id_proyecto, p.id_tecnico, p.id_profesor, " +
                    "p.nombre, p.descripcion, p.objetivo, p.fecha_inicio, p.fecha_fin, " +
                    "p.cupo_maximo, p.cupo_disponible, p.estado, p.nrc, p.periodo, " +
                    "ov.id_organizacion, ov.nombre_organizacion " +
                    "FROM proyecto p " +
                    "JOIN spp.organizacion_vinculada ov " +
                    "ON ov.id_organizacion = p.id_organizacion;";
    private static final String SELECT_ALL_AVAILABLE_PROJECTS_SQL =
            "SELECT p.id_proyecto, p.id_tecnico, p.id_profesor, " +
                    "p.nombre, p.descripcion, p.fecha_inicio, p.fecha_fin, p.cupo_maximo, " +
                    "p.cupo_disponible, p.nrc, p.periodo, ov.id_organizacion, ov.nombre_organizacion " +
                    "FROM proyecto p " +
                    "JOIN spp.organizacion_vinculada ov ON ov.id_organizacion = p.id_organizacion " +
                    "WHERE p.estado = '" + STATUS_AVAILABLE + "' AND p.cupo_disponible > 0";
    private static final String SELECT_PROJECTS_BY_COORDINATOR_SQL =
            "SELECT id_proyecto, id_organizacion, id_tecnico, id_coordinador, " +
                    "nombre, descripcion, fecha_inicio, fecha_fin, cupo_maximo, " +
                    "cupo_disponible, estado FROM proyecto WHERE id_coordinador = ?";
    private static final String UPDATE_PROJECT_SQL =
            "UPDATE proyecto " +
                    "SET id_organizacion = ?, id_tecnico = ?, nombre = ?, descripcion = ?, objetivo = ?, " +
                    "fecha_inicio = ?, fecha_fin = ?, cupo_maximo = ?, cupo_disponible = ?, " +
                    "estado = ? WHERE id_proyecto = ?";
    private static final String UPDATE_PROJECT_STATUS_SQL =
            "UPDATE proyecto SET estado = '" + STATUS_CANCELLED + "' WHERE id_proyecto = ?";
    private static final String UPDATE_AVAILABLE_SLOT_SQL =
            "UPDATE proyecto " +
                    "SET cupo_disponible = cupo_disponible - 1, " +
                    "    estado = CASE WHEN cupo_disponible - 1 = 0 THEN '" + STATUS_FULL + "' ELSE estado END " +
                    "WHERE id_proyecto = ? AND cupo_disponible > 0";
    private static final String DELETE_PROYECT =
            "DELETE FROM proyecto WHERE id_proyecto = ?";

    private static final String SQL_SELECT_BY_PROFESSOR_AVAILABLE =
            "SELECT p.id_proyecto, p.id_organizacion, p.id_tecnico, p.id_profesor, " +
            "       p.nombre, p.descripcion, p.objetivo, p.fecha_inicio, p.fecha_fin, " +
            "       p.cupo_maximo, p.cupo_disponible, p.estado, " +
            "       ov.nombre_organizacion " +
            "FROM proyecto p " +
            "JOIN spp.organizacion_vinculada ov ON ov.id_organizacion = p.id_organizacion " +
            "WHERE p.id_profesor = ? AND p.estado = '" + STATUS_AVAILABLE + "'";

    private static final String SQL_SELECT_BY_EDUCATIONAL_EXPERIENCE =
            "SELECT p.id_proyecto, p.id_tecnico, p.id_profesor, " +
                    "p.nombre, p.descripcion, p.objetivo, p.fecha_inicio, p.fecha_fin, " +
                    "p.cupo_maximo, p.cupo_disponible, p.estado, p.nrc, p.periodo, " +
                    "ov.id_organizacion, ov.nombre_organizacion " +
                    "FROM proyecto p " +
                    "JOIN spp.organizacion_vinculada ov ON ov.id_organizacion = p.id_organizacion " +
                    "WHERE p.nrc = ? AND p.periodo = ? " +
                    "ORDER BY p.nombre ASC";

    private static final String SQL_EXISTS_BY_NRC =
            "SELECT COUNT(*) AS total FROM proyecto WHERE nrc = ? AND periodo = ?";

    private static final String SQL_INCREMENT_AVAILABLE_SLOT =
            "UPDATE proyecto " +
            "SET cupo_disponible = cupo_disponible + 1, " +
            "    estado = CASE WHEN estado = '" + STATUS_FULL + "' THEN '" + STATUS_AVAILABLE + "' ELSE estado END " +
            "WHERE id_proyecto = ?";



    @Override
    public boolean saveProject(Project project) throws ServiceException, ValidationException {
        validateProject(project);

        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     INSERT_PROJECT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, project.getIdOrganization());
            preparedStatement.setInt(2, project.getIdTechnicalResponsible());
            preparedStatement.setInt(3, project.getIdProfessor());
            preparedStatement.setString(4, project.getName());
            preparedStatement.setString(5, project.getDescription());
            preparedStatement.setString(6, project.getObjective());
            preparedStatement.setDate(7, Date.valueOf(project.getStartDate()));
            preparedStatement.setDate(8, Date.valueOf(project.getEndDate()));
            preparedStatement.setInt(9, project.getMaximumPlaces());
            preparedStatement.setInt(10, project.getAvailablePlaces());
            preparedStatement.setString(11, STATUS_AVAILABLE);
            preparedStatement.setString(12, project.getNrc());
            preparedStatement.setString(13, project.getPeriod());

            if (preparedStatement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        project.setIdProject(generatedKeys.getInt(1));
                    }
                }
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving project {0}: {1}",
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
    public Project findById(int idProjecto) throws ServiceException, ValidationException {
        if (idProjecto <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProjecto);
        }

        Project projectResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PROJECT_BY_ID_SQL)) {

            preparedStatement.setInt(1, idProjecto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    projectResult = mapProject(resultSet);
                    projectResult.setIdProfessor(resultSet.getInt("id_profesor"));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding project with ID {0}: {1}",
                    new Object[]{idProjecto, sqlException.getMessage()});
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
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ALL_PROJECTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {

                Project project = new Project();

                project.setIdProject(resultSet.getInt("id_proyecto"));
                project.setIdOrganization(resultSet.getInt("id_organizacion"));
                project.setIdTechnicalResponsible(resultSet.getInt("id_tecnico"));
                project.setIdProfessor(resultSet.getInt("id_profesor"));
                project.setName(resultSet.getString("nombre"));
                project.setDescription(resultSet.getString("descripcion"));
                project.setObjective(resultSet.getString("objetivo"));
                project.setStartDate(resultSet.getDate("fecha_inicio").toLocalDate());
                project.setEndDate(resultSet.getDate("fecha_fin").toLocalDate());
                project.setMaximumPlaces(resultSet.getInt("cupo_maximo"));
                project.setAvailablePlaces(resultSet.getInt("cupo_disponible"));
                project.setStatus(resultSet.getString("estado"));
                project.setNrc(resultSet.getString("nrc"));
                project.setPeriod(resultSet.getString("periodo"));
                project.setOrganizationName(resultSet.getString("nombre_organizacion"));

                projectList.add(project);
            }

        } catch (SQLException sqlException) {

            LOGGER.log(Level.SEVERE, "Error retrieving all projects: {0}", sqlException.getMessage());

            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {

                throw new DuplicateEntryException("Ya existe un registro con esa clave en la base de datos.", sqlException);
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
            LOGGER.log(Level.SEVERE, "Error retrieving available projects: {0}",
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
            LOGGER.log(Level.SEVERE, "Error finding projects for coordinator {0}: {1}",
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

            preparedStatement.setInt (1, project.getIdOrganization());
            preparedStatement.setInt (2, project.getIdTechnicalResponsible());
            preparedStatement.setString(3, project.getName());
            preparedStatement.setString(4, project.getDescription());
            preparedStatement.setString(5, project.getObjective());
            preparedStatement.setDate (6, java.sql.Date.valueOf(project.getStartDate()));
            preparedStatement.setDate (7, java.sql.Date.valueOf(project.getEndDate()));
            preparedStatement.setInt (8, project.getMaximumPlaces());
            preparedStatement.setInt (9, project.getAvailablePlaces());
            preparedStatement.setString(10, project.getStatus());
            preparedStatement.setInt (11, project.getIdProject());

            if (preparedStatement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error updating project with ID {0}: {1}",
                    new Object[]{project.getIdProject(), sqlException.getMessage()});
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
    public boolean cancelProject(int idProject) throws ServiceException, ValidationException {
        if (idProject <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProject);
        }

        boolean isCanceled = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PROJECT_STATUS_SQL)) {

            preparedStatement.setInt(1, idProject);

            if (preparedStatement.executeUpdate() > 0) {
                isCanceled = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error cancelling project with ID {0}: {1}",
                    new Object[]{idProject, sqlException.getMessage()});
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
    public boolean decrementAvailableSlot(int idProject) throws ServiceException, ValidationException {
        if (idProject <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProject);
        }

        boolean isDecremented = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_AVAILABLE_SLOT_SQL)) {

            preparedStatement.setInt(1, idProject);

            if (preparedStatement.executeUpdate() > 0) {
                isDecremented = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error decreasing slots for project {0}: {1}",
                    new Object[]{idProject, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al reducir el cupo del proyecto.", sqlException);
        }

        return isDecremented;
    }

    public List<Project> findByProfessorAvailable(int professorId)
            throws ServiceException, ValidationException {
        if (professorId <= 0) {
            throw new ValidationException(
                    "El ID del profesor debe ser mayor a cero. ID recibido: " + professorId);
        }

        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SQL_SELECT_BY_PROFESSOR_AVAILABLE)) {

            preparedStatement.setInt(1, professorId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Project project = mapProject(resultSet);
                    project.setIdProfessor(resultSet.getInt("id_profesor"));
                    project.setStatus(resultSet.getString("estado"));
                    project.setOrganizationName(resultSet.getString("nombre_organizacion"));
                    projectList.add(project);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error retrieving available projects for professor {0}: {1}",
                    new Object[]{professorId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al recuperar los proyectos del profesor.", sqlException);
        }

        return projectList;
    }

    public List<Project> findByEducationalExperience(String nrc, String period)
            throws ServiceException, ValidationException {
        if (nrc == null || nrc.isBlank()) {
            throw new ValidationException("El NRC de la experiencia educativa no puede estar vacío.");
        }
        if (period == null || period.isBlank()) {
            throw new ValidationException("El periodo de la experiencia educativa no puede estar vacío.");
        }

        List<Project> projectList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SQL_SELECT_BY_EDUCATIONAL_EXPERIENCE)) {

            preparedStatement.setString(1, nrc);
            preparedStatement.setString(2, period);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Project project = mapProject(resultSet);
                    project.setIdProfessor(resultSet.getInt("id_profesor"));
                    project.setStatus(resultSet.getString("estado"));
                    projectList.add(project);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error retrieving projects for educational experience {0}-{1}: {2}",
                    new Object[]{nrc, period, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al recuperar los proyectos de la experiencia educativa.", sqlException);
        }

        return projectList;
    }

    public boolean incrementAvailableSlot(int idProject)
            throws ServiceException, ValidationException {
        if (idProject <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProject);
        }

        boolean isIncremented = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SQL_INCREMENT_AVAILABLE_SLOT)) {

            preparedStatement.setInt(1, idProject);

            if (preparedStatement.executeUpdate() > 0) {
                isIncremented = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error increasing slots for project {0}: {1}",
                    new Object[]{idProject, sqlException.getMessage()});
            throw new ServiceException("Error al incrementar el cupo del proyecto.", sqlException);
        }

        return isIncremented;
    }

    public boolean existsByNrcAndPeriod(String nrc, String period)
            throws ServiceException, ValidationException {
        if (nrc == null || nrc.isBlank()) {
            throw new ValidationException("El NRC de la experiencia educativa no puede estar vacío.");
        }
        if (period == null || period.isBlank()) {
            throw new ValidationException("El periodo de la experiencia educativa no puede estar vacío.");
        }

        boolean exists = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_EXISTS_BY_NRC)) {

            statement.setString(1, nrc);
            statement.setString(2, period);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    exists = resultSet.getInt("total") > 0;
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error verifying project existence by NRC {0} and period {1}: {2}",
                    new Object[]{nrc, period, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al verificar el NRC de la experiencia educativa.", sqlException);
        }

        return exists;
    }

    @Override
    public int deleteProject(int idProject) throws ServiceException, ValidationException {
        if (idProject <= 0){
            throw new ValidationException("ID no valido debe de ser un valor numerico positivo" + idProject);
        }

        int deletedRows = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_PROYECT)){
            preparedStatement.setInt(1, idProject);

            deletedRows = preparedStatement.executeUpdate();

        }catch (SQLException sqlException){
            LOGGER.log(Level.SEVERE, "Error deleting project {0}: {1}",
                    new Object[]{idProject, sqlException.getMessage()});
            if (ReferentialIntegrityException.isForeignKeyViolation(sqlException)) {
                throw new ReferentialIntegrityException(
                        "El proyecto tiene registros asociados.", sqlException);
            }
            throw new ServiceException("Error al eliminar proyecto", sqlException);
        }

        return deletedRows;
    }

    private void validateProject(Project project) throws ValidationException {
        if (project.getStartDate().isAfter(project.getEndDate())) {
            throw new ValidationException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin del proyecto.");
        }
        if (!isWithinAcademicPeriod(project)) {
            throw new ValidationException(
                    "Las fechas del proyecto deben estar dentro del período académico de la experiencia educativa.");
        }
        if (project.getMaximumPlaces() <= 0) {
            throw new ValidationException(
                    "El cupo máximo debe ser mayor a cero. Valor recibido: " + project.getMaximumPlaces());
        }
    }

    private boolean isWithinAcademicPeriod(Project project) {
        boolean isWithin = true;
        String period = project.getPeriod();
        LocalDate periodStart = null;
        LocalDate periodEnd = null;
        if (period != null && period.startsWith("FEB-JUL-")) {
            int year = parsePeriodYear(period, "FEB-JUL-");
            if (year > 0) {
                periodStart = LocalDate.of(year, 2, 1);
                periodEnd = LocalDate.of(year, 7, 31);
            }
        } else if (period != null && period.startsWith("AUG-ENE-")) {
            int year = parsePeriodYear(period, "AUG-ENE-");
            if (year > 0) {
                periodStart = LocalDate.of(year, 8, 1);
                periodEnd = LocalDate.of(year + 1, 1, 31);
            }
        }
        boolean hasBounds = periodStart != null && periodEnd != null;
        if (hasBounds) {
            isWithin = !project.getStartDate().isBefore(periodStart)
                    && !project.getEndDate().isAfter(periodEnd);
        }
        return isWithin;
    }

    private int parsePeriodYear(String period, String prefix) {
        int year = 0;
        String yearText = period.substring(prefix.length()).trim();
        if (yearText.matches("\\d{4}")) {
            year = Integer.parseInt(yearText);
        }
        return year;
    }

    private Project mapProject(ResultSet resultSet) throws SQLException {
        Project project = new Project(
                resultSet.getInt ("id_proyecto"),
                resultSet.getInt ("id_organizacion"),
                resultSet.getInt ("id_tecnico"),
                resultSet.getString("nombre"),
                resultSet.getString("descripcion"),
                resultSet.getDate ("fecha_inicio").toLocalDate(),
                resultSet.getDate ("fecha_fin").toLocalDate(),
                resultSet.getInt ("cupo_disponible"),
                resultSet.getInt ("cupo_maximo")
        );
        try {
            project.setObjective(resultSet.getString("objetivo"));
        } catch (SQLException ignored) {
        }
        try {
            project.setNrc(resultSet.getString("nrc"));
        } catch (SQLException ignored) {
        }
        try {
            project.setPeriod(resultSet.getString("periodo"));
        } catch (SQLException ignored) {
        }
        try {
            project.setOrganizationName(resultSet.getString("nombre_organizacion"));
        } catch (SQLException ignored) {
        }
        return project;
    }
}
