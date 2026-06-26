package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Assignment;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IAssignmentDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssignmentDAO implements IAssignmentDAO {

    private static final Logger LOGGER = Logger.getLogger(AssignmentDAO.class.getName());
    private static final String SQL_INSERT =
            "INSERT INTO asignacion (id_practicante, id_proyecto, id_solicitud, fecha_asignacion, razon_asignacion) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_COLUMNS =
            "SELECT id_asignacion, id_practicante, id_proyecto, id_solicitud, " +
                    "fecha_asignacion, estado, razon_asignacion ";
    private static final String SQL_SELECT_BY_ID =
            "SELECT id_asignacion, id_practicante, id_proyecto, id_solicitud, " +
                    "fecha_asignacion, estado, razon_asignacion " +
                    "FROM asignacion WHERE id_asignacion = ?";
    private static final String SQL_SELECT_ALL =
            "SELECT id_asignacion, id_practicante, id_proyecto, id_solicitud, " +
                    "fecha_asignacion, estado, razon_asignacion " +
                    "FROM asignacion";
    private static final String SQL_SELECT_BY_INTERN =
            "SELECT id_asignacion, id_practicante, id_proyecto, id_solicitud, " +
                    "fecha_asignacion, estado, razon_asignacion " +
                    "FROM asignacion WHERE id_practicante = ?";
    private static final String SQL_SELECT_BY_PROJECT =
            "SELECT id_asignacion, id_practicante, id_proyecto, id_solicitud, " +
                    "fecha_asignacion, estado, razon_asignacion " +
                    "FROM asignacion WHERE id_proyecto = ?";
    private static final String SQL_GET_ACTIVE_BY_USER =
            "SELECT id_asignacion, id_practicante, id_proyecto, id_solicitud, " +
                    "fecha_asignacion, estado, razon_asignacion " +
                    "FROM asignacion WHERE id_practicante = ? AND estado = 'Activa'";

    private static final String SQL_DELETE_BY_INTERN_AND_PROJECT =
            "DELETE FROM asignacion WHERE id_practicante = ? AND id_proyecto = ?";

    @Override
    public int save(Assignment assignment) throws ServiceException, ValidationException {
        if (assignment.getIdIntern() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: "
                            + assignment.getIdIntern());
        }
        if (assignment.getIdProject() <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: "
                            + assignment.getIdProject());
        }
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, assignment.getIdIntern());
            statement.setInt(2, assignment.getIdProject());
            statement.setInt(3, assignment.getIdApplication());
            statement.setDate(4, Date.valueOf(assignment.getAssignmentDate()));
            statement.setString(5, assignment.getAssignmentReason());

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    assignment.setIdAssignment(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving assignment for intern {0}: {1}",
                    new Object[]{assignment.getIdIntern(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar la asignación.", sqlException);
        }

        return rowsAffected;
    }

    @Override
    public Assignment getById(int idAssignment) throws ServiceException, ValidationException {
        if (idAssignment <= 0) {
            throw new ValidationException(
                    "El ID de la asignación debe ser mayor a cero. ID recibido: " + idAssignment);
        }
        Assignment assignmentResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idAssignment);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    assignmentResult = mapResultSet(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding assignment with ID {0}: {1}",
                    new Object[]{idAssignment, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar la asignación.", sqlException);
        }

        return assignmentResult;
    }

    @Override
    public List<Assignment> getAll() throws ServiceException {
        List<Assignment> assignments = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                assignments.add(mapResultSet(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all assignments: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar las asignaciones.", sqlException);
        }

        return assignments;
    }

    @Override
    public List<Assignment> getByIdIntern(int idIntern) throws ServiceException, ValidationException {
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }
        List<Assignment> assignmentResult = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_INTERN)) {

            statement.setInt(1, idIntern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    assignmentResult.add(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding assignment by intern {0}: {1}",
                    new Object[]{idIntern, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar la asignación del practicante.", sqlException);
        }

        return assignmentResult;
    }

    @Override
    public List<Assignment> getByIdProject(int idProject) throws ServiceException, ValidationException {
        if (idProject <= 0) {
            throw new ValidationException("El ID del proyecto debe ser mayor a cero. ID recibido: " + idProject);
        }
        
        List<Assignment> assignments = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_PROJECT)) {

            statement.setInt(1, idProject);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    assignments.add(mapResultSet(resultSet));
                }

            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding assignments by project {0}: {1}",
                    new Object[]{idProject, sqlException.getMessage()});

            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }

            throw new ServiceException(
                    "Error al recuperar las asignaciones del proyecto.", sqlException);
        }

        return assignments;
    }

    @Override
    public Assignment getActiveByIdIntern(int idIntern) throws ServiceException {
        Assignment assignment = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_ACTIVE_BY_USER)){
            preparedStatement.setInt(1, idIntern);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    assignment = mapResultSet(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            throw new ServiceException("Error al recuperar la asignación activa.", sqlException);
        }

        return assignment;
    }

    public boolean deleteByInternAndProject(int internId, int projectId)
            throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }
        if (projectId <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + projectId);
        }

        boolean isDeleted = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_DELETE_BY_INTERN_AND_PROJECT)) {

            statement.setInt(1, internId);
            statement.setInt(2, projectId);

            if (statement.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error deleting assignment for intern {0} from project {1}: {2}",
                    new Object[]{internId, projectId, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.", sqlException);
            }
            throw new ServiceException("Error al eliminar la asignación.", sqlException);
        }

        return isDeleted;
    }

    private Assignment mapResultSet(ResultSet resultSet) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setIdAssignment(resultSet.getInt("id_asignacion"));
        assignment.setIdIntern(resultSet.getInt("id_practicante"));
        assignment.setIdProject(resultSet.getInt("id_proyecto"));
        assignment.setIdApplication(resultSet.getInt("id_solicitud"));
        assignment.setAssignmentDate(resultSet.getDate("fecha_asignacion").toLocalDate());
        assignment.setStatus(resultSet.getString("estado"));
        assignment.setAssignmentReason(resultSet.getString("razon_asignacion"));
        return assignment;
    }
}
