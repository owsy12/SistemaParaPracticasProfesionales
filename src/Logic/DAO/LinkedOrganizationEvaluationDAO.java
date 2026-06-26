package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.LinkedOrganizationEvaluation;
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

public class LinkedOrganizationEvaluationDAO {
    private static final String STATUS_SUBMITTED = "Entregada";


    private static final Logger LOGGER = Logger.getLogger(LinkedOrganizationEvaluationDAO.class.getName());

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

    public int save(LinkedOrganizationEvaluation linkedOrganizationEvaluation) throws ServiceException, ValidationException {
        if (linkedOrganizationEvaluation.getIdIntern() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: "
                    + linkedOrganizationEvaluation.getIdIntern());
        }
        if (linkedOrganizationEvaluation.getIdProject() <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: "
                    + linkedOrganizationEvaluation.getIdProject());
        }

        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt (1, linkedOrganizationEvaluation.getIdIntern());
            statement.setInt (2, linkedOrganizationEvaluation.getIdProject());
            statement.setString(3, linkedOrganizationEvaluation.getDocumentPath());
            statement.setString(4, linkedOrganizationEvaluation.getStatus() != null
                    ? linkedOrganizationEvaluation.getStatus() : STATUS_SUBMITTED);
            statement.setTimestamp(5, Timestamp.valueOf(
                    linkedOrganizationEvaluation.getDeliveryDate() != null
                    ? linkedOrganizationEvaluation.getDeliveryDate()
                    : LocalDateTime.now()));

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    linkedOrganizationEvaluation.setIdLinkedOrganizationEvaluation(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error saving OV evaluation for intern {0} in project {1}: {2}",
                    new Object[]{linkedOrganizationEvaluation.getIdIntern(), linkedOrganizationEvaluation.getIdProject(),
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

    public LinkedOrganizationEvaluation findByInternAndProject(int internId, int projectId)
            throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }
        if (projectId <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + projectId);
        }

        LinkedOrganizationEvaluation linkedOrganizationEvaluation = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement =
                     connection.prepareStatement(SQL_SELECT_BY_INTERN_AND_PROJECT)) {

            statement.setInt(1, internId);
            statement.setInt(2, projectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    linkedOrganizationEvaluation = mapResultSet(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error finding OV evaluation for intern {0} in project {1}: {2}",
                    new Object[]{internId, projectId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al buscar la evaluación OV.", sqlException);
        }

        return linkedOrganizationEvaluation;
    }

    public boolean updateStatus(int evaluationId, String status)
            throws ServiceException, ValidationException {
        if (evaluationId <= 0) {
            throw new ValidationException(
                    "El ID de la evaluación OV debe ser mayor a cero. ID recibido: " + evaluationId);
        }
        if (status == null || status.isBlank()) {
            throw new ValidationException("El estado no puede estar vacío.");
        }

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_STATUS)) {

            statement.setString(1, status);
            statement.setInt (2, evaluationId);

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error updating OV evaluation status {0}: {1}",
                    new Object[]{evaluationId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al actualizar el estado de la evaluación OV.", sqlException);
        }

        return isUpdated;
    }

    private LinkedOrganizationEvaluation mapResultSet(ResultSet resultSet) throws SQLException {
        LinkedOrganizationEvaluation linkedOrganizationEvaluation = new LinkedOrganizationEvaluation();
        linkedOrganizationEvaluation.setIdLinkedOrganizationEvaluation(resultSet.getInt("id_evaluacion_ov"));
        linkedOrganizationEvaluation.setIdIntern (resultSet.getInt ("id_practicante"));
        linkedOrganizationEvaluation.setIdProject (resultSet.getInt ("id_proyecto"));
        linkedOrganizationEvaluation.setDocumentPath (resultSet.getString("ruta_documento"));
        linkedOrganizationEvaluation.setStatus (resultSet.getString("estado"));

        Timestamp deliveryDate = resultSet.getTimestamp("fecha_entrega");
        if (deliveryDate != null) {
            linkedOrganizationEvaluation.setDeliveryDate(deliveryDate.toLocalDateTime());
        }

        return linkedOrganizationEvaluation;
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
