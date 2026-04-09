package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Assignment;
import Logic.Interface.IAssignmentDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssignmentDAO implements IAssignmentDAO {

    private static final Logger LOGGER = Logger.getLogger(AssignmentDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO asignacion " +
                    "(id_practicante, id_proyecto, id_solicitud, fecha_asignacion) " +
                    "VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_asignacion, id_practicante, id_proyecto, " +
                    "id_solicitud, fecha_asignacion " +
                    "FROM asignacion WHERE id_asignacion = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT id_asignacion, id_practicante, id_proyecto, " +
                    "id_solicitud, fecha_asignacion FROM asignacion";

    private static final String SQL_SELECT_BY_INTERN =
            "SELECT id_asignacion, id_practicante, id_proyecto, " +
                    "id_solicitud, fecha_asignacion " +
                    "FROM asignacion WHERE id_practicante = ?";

    private static final String SQL_SELECT_BY_PROJECT =
            "SELECT id_asignacion, id_practicante, id_proyecto, " +
                    "id_solicitud, fecha_asignacion " +
                    "FROM asignacion WHERE id_proyecto = ?";

    @Override
    public int save(Assignment assignment) {
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, assignment.getIdIntern());
            preparedStatement.setInt(2, assignment.getIdProyect());
            preparedStatement.setInt(3, assignment.getIdApplication());
            preparedStatement.setDate(4, new java.sql.Date(assignment.getAssignmentDate().getTime()));

            rowsAffected = preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    assignment.setIdAssignment(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving assignment: {0}", sqlException.getMessage());
        }

        return rowsAffected;
    }

    @Override
    public Optional<Assignment> getById(int idAssignment) {
        Optional<Assignment> assignmentResult = Optional.empty();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            preparedStatement.setInt(1, idAssignment);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    assignmentResult = Optional.of(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding assignment by ID {0}: {1}",
                    new Object[]{idAssignment, sqlException.getMessage()});
        }

        return assignmentResult;
    }

    @Override
    public List<Assignment> getAll() {
        List<Assignment> assignments = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                assignments.add(mapResultSet(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all assignments: {0}", sqlException.getMessage());
        }

        return assignments;
    }

    @Override
    public Optional<Assignment> getByIdIntern(int idIntern) {
        Optional<Assignment> assignmentResult = Optional.empty();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_BY_INTERN)) {

            preparedStatement.setInt(1, idIntern);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    assignmentResult = Optional.of(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding assignment by intern ID {0}: {1}",
                    new Object[]{idIntern, sqlException.getMessage()});
        }

        return assignmentResult;
    }

    @Override
    public List<Assignment> getByIdProject(int idProject) {
        List<Assignment> assignments = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_BY_PROJECT)) {

            preparedStatement.setInt(1, idProject);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    assignments.add(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding assignments by project ID {0}: {1}",
                    new Object[]{idProject, sqlException.getMessage()});
        }

        return assignments;
    }

    private Assignment mapResultSet(ResultSet resultSet) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setIdAssignment(resultSet.getInt("id_asignacion"));
        assignment.setIdIntern(resultSet.getInt("id_practicante"));
        assignment.setIdProyect(resultSet.getInt("id_proyecto"));
        assignment.setIdApplication(resultSet.getInt("id_solicitud"));
        assignment.setAssignmentDate(resultSet.getDate("fecha_asignacion"));
        return assignment;
    }
}