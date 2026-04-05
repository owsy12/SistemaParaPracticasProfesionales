package Logic.DAO;

import DataAccess.BDConnection;
import Logic.DTOs.Assignment;
import Logic.Interface.IAssignmentDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssignmentDAO implements IAssignmentDAO {

    private static final Logger LOGGER = Logger.getLogger(AssignmentDAO.class.getName());
    private static final String INSERT_ASSIGNMENT_SQL =
            "INSERT INTO asignacion (id_practicante, id_proyecto, id_solicitud) " +
            "VALUES (?, ?, ?)";
    private static final String SELECT_ASSIGNMENT_BY_ID_SQL =
            "SELECT id_asignacion, id_practicante, id_proyecto, id_solicitud, " +
            "fecha_asignacion FROM asignacion WHERE id_asignacion = ?";
    private static final String SELECT_ASSIGNMENT_BY_INTERN_SQL =
            "SELECT id_asignacion, id_practicante, id_proyecto, id_solicitud, " +
            "fecha_asignacion FROM asignacion WHERE id_practicante = ?";
    private static final String SELECT_ASSIGNMENT_BY_PROJECT_SQL =
            "SELECT id_asignacion, id_practicante, id_proyecto, id_solicitud, " +
            "fecha_asignacion FROM asignacion WHERE id_proyecto = ?";
    private static final String SELECT_ALL_ASSIGNMENTS_SQL =
            "SELECT id_asignacion, id_practicante, id_proyecto, id_solicitud, " +
            "fecha_asignacion FROM asignacion";

    @Override
    public boolean saveAssignment(Assignment assignment) {
        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ASSIGNMENT_SQL)) {
            preparedStatement.setInt(1, assignment.getIdIntern());
            preparedStatement.setInt(2, assignment.getIdProyect());
            preparedStatement.setInt(3, assignment.getIdApplication());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al guardar asignación para practicante {0}: {1}",
                    new Object[]{assignment.getIdIntern(), sqlException.getMessage()});
            return false;
        }
    }

    @Override
    public Assignment findById(int assignmentId) {
        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ASSIGNMENT_BY_ID_SQL)) {
            preparedStatement.setInt(1, assignmentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAssignment(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar asignación con ID {0}: {1}",
                    new Object[]{assignmentId, sqlException.getMessage()});
        }
        return null;
    }

    @Override
    public Assignment findByIntern(int internId) {
        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ASSIGNMENT_BY_INTERN_SQL)) {
            preparedStatement.setInt(1, internId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapAssignment(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar asignación para practicante {0}: {1}",
                    new Object[]{internId, sqlException.getMessage()});
        }
        return null;
    }

    @Override
    public List<Assignment> findByProject(int projectId) {
        List<Assignment> assignmentList = new ArrayList<>();

        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ASSIGNMENT_BY_PROJECT_SQL)) {
            preparedStatement.setInt(1, projectId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    assignmentList.add(mapAssignment(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar asignaciones para proyecto {0}: {1}",
                    new Object[]{projectId, sqlException.getMessage()});
        }
        return assignmentList;
    }

    @Override
    public List<Assignment> findAll() {
        List<Assignment> assignmentList = new ArrayList<>();

        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ALL_ASSIGNMENTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                assignmentList.add(mapAssignment(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al recuperar todas las asignaciones: {0}",
                    sqlException.getMessage());
        }
        return assignmentList;
    }

    private Assignment mapAssignment(ResultSet resultSet) throws SQLException {
        return new Assignment(
                resultSet.getInt("id_asignacion"),
                resultSet.getInt("id_practicante"),
                resultSet.getInt("id_proyecto"),
                resultSet.getInt("id_solicitud"),
                resultSet.getDate("fecha_asignacion")
        );
    }
}
