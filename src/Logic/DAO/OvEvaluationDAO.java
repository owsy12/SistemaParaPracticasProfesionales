package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.OvEvaluation;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OvEvaluationDAO {
    private static final String STATUS_SUBMITTED = "Entregada";


    private static final Logger LOGGER = Logger.getLogger(OvEvaluationDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO evaluacion_ov " +
            "(id_practicante, id_proyecto, ruta_documento, estado, fecha_entrega) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_INTERN_AND_PROJECT =
            "SELECT id_evaluacion_ov, id_practicante, id_proyecto, " +
            "       ruta_documento, estado, fecha_entrega " +
            "FROM evaluacion_ov " +
            "WHERE id_practicante = ? AND id_proyecto = ?";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE evaluacion_ov SET estado = ? WHERE id_evaluacion_ov = ?";

    public int save(OvEvaluation ovEvaluation) throws ServiceException, ValidationException {
        if (ovEvaluation.getIdIntern() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: "
                    + ovEvaluation.getIdIntern());
        }
        if (ovEvaluation.getIdProject() <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: "
                    + ovEvaluation.getIdProject());
        }

        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt (1, ovEvaluation.getIdIntern());
            statement.setInt (2, ovEvaluation.getIdProject());
            statement.setString(3, ovEvaluation.getDocumentPath());
            statement.setString(4, ovEvaluation.getStatus() != null
                    ? ovEvaluation.getStatus() : STATUS_SUBMITTED);
            statement.setTimestamp(5, Timestamp.valueOf(
                    ovEvaluation.getDeliveryDate() != null
                    ? ovEvaluation.getDeliveryDate()
                    : LocalDateTime.now()));

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ovEvaluation.setIdOvEvaluation(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error saving OV evaluation for intern {0} in project {1}: {2}",
                    new Object[]{ovEvaluation.getIdIntern(), ovEvaluation.getIdProject(),
                                 sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe una evaluación OV para este practicante en este proyecto.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar la evaluación OV.", sqlException);
        }

        return rowsAffected;
    }

    public OvEvaluation findByInternAndProject(int internId, int projectId)
            throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }
        if (projectId <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + projectId);
        }

        OvEvaluation ovEvaluation = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement =
                     connection.prepareStatement(SQL_SELECT_BY_INTERN_AND_PROJECT)) {

            statement.setInt(1, internId);
            statement.setInt(2, projectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    ovEvaluation = mapResultSet(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error finding OV evaluation for intern {0} in project {1}: {2}",
                    new Object[]{internId, projectId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al buscar la evaluación OV.", sqlException);
        }

        return ovEvaluation;
    }

    public boolean updateStatus(int idOvEvaluation, String status)
            throws ServiceException, ValidationException {
        if (idOvEvaluation <= 0) {
            throw new ValidationException(
                    "El ID de la evaluación OV debe ser mayor a cero. ID recibido: " + idOvEvaluation);
        }
        if (status == null || status.isBlank()) {
            throw new ValidationException("El estado no puede estar vacío.");
        }

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_STATUS)) {

            statement.setString(1, status);
            statement.setInt (2, idOvEvaluation);

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error updating OV evaluation status {0}: {1}",
                    new Object[]{idOvEvaluation, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al actualizar el estado de la evaluación OV.", sqlException);
        }

        return isUpdated;
    }

    private OvEvaluation mapResultSet(ResultSet resultSet) throws SQLException {
        OvEvaluation ovEvaluation = new OvEvaluation();
        ovEvaluation.setIdOvEvaluation(resultSet.getInt ("id_evaluacion_ov"));
        ovEvaluation.setIdIntern (resultSet.getInt ("id_practicante"));
        ovEvaluation.setIdProject (resultSet.getInt ("id_proyecto"));
        ovEvaluation.setDocumentPath (resultSet.getString("ruta_documento"));
        ovEvaluation.setStatus (resultSet.getString("estado"));

        Timestamp deliveryDate = resultSet.getTimestamp("fecha_entrega");
        if (deliveryDate != null) {
            ovEvaluation.setDeliveryDate(deliveryDate.toLocalDateTime());
        }

        return ovEvaluation;
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

        String sql = "DELETE FROM evaluacion_ov WHERE id_practicante = ? AND id_proyecto = ?";
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, internId);
            statement.setInt(2, projectId);
            rowsAffected = statement.executeUpdate();

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error deleting OV evaluations for intern {0} in project {1}: {2}",
                    new Object[]{internId, projectId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al eliminar evaluaciones OV del practicante.", sqlException);
        }

        return rowsAffected >= 0;
    }
}
