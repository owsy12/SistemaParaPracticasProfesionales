package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Assignment;
import Logic.Interface.IAssignmentDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDAO implements IAssignmentDAO {
    private static final String SQL_INSERT =
            "INSERT INTO asignacion " +
                    "(id_practicante, id_proyecto, id_solicitud, fecha_asignacion) " +
                    "VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_asignacion, id_practicante, id_proyecto, " +
                    "       id_solicitud, fecha_asignacion " +
                    "FROM asignacion " +
                    "WHERE id_asignacion = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT id_asignacion, id_practicante, id_proyecto, " +
                    "       id_solicitud, fecha_asignacion " +
                    "FROM asignacion";

    private static final String SQL_SELECT_BY_INTERN =
            "SELECT id_asignacion, id_practicante, id_proyecto, " +
                    "       id_solicitud, fecha_asignacion " +
                    "FROM asignacion " +
                    "WHERE id_practicante = ?";

    private static final String SQL_SELECT_BY_PROJECT =
            "SELECT id_asignacion, id_practicante, id_proyecto, " +
                    "       id_solicitud, fecha_asignacion " +
                    "FROM asignacion " +
                    "WHERE id_proyecto = ?";



    @Override
    public int save(Assignment assignment) throws SQLException {
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt (1, assignment.getIdIntern());
            statement.setInt (2, assignment.getIdProyect());
            statement.setInt (3, assignment.getIdApplication());
            statement.setDate(4, new java.sql.Date(
                    assignment.getAssignmentDate().getTime()));

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    assignment.setIdAssignment(generatedKeys.getInt(1));
                }
            }
        }

        return rowsAffected;
    }

    @Override
    public Assignment getById(int idAssignment) throws SQLException {
        Assignment assignment = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idAssignment);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    assignment = mapResultSet(resultSet);
                }
            }
        }

        return assignment;
    }

    @Override
    public List<Assignment> getAll() throws SQLException {
        List<Assignment> assignments = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                assignments.add(mapResultSet(resultSet));
            }
        }

        return assignments;
    }

    @Override
    public Assignment getByIdIntern(int idIntern) throws SQLException {
        Assignment assignment = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_INTERN)) {

            statement.setInt(1, idIntern);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    assignment = mapResultSet(resultSet);
                }
            }
        }

        return assignment;
    }

    @Override
    public List<Assignment> getByIdProject(int idProject) throws SQLException {
        List<Assignment> assignments = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_PROJECT)) {

            statement.setInt(1, idProject);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    assignments.add(mapResultSet(resultSet));
                }
            }
        }

        return assignments;
    }

    private Assignment mapResultSet(ResultSet resultSet) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setIdAssignment (resultSet.getInt ("id_asignacion"));
        assignment.setIdIntern     (resultSet.getInt ("id_practicante"));
        assignment.setIdProyect    (resultSet.getInt ("id_proyecto"));
        assignment.setIdApplication(resultSet.getInt ("id_solicitud"));
        assignment.setAssignmentDate(resultSet.getDate("fecha_asignacion"));
        return assignment;
    }
}
