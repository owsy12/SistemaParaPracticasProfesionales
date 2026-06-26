package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.InternActivity;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IInternActivityDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InternActivityDAO implements IInternActivityDAO {
    private static final String STATUS_PENDING = "Pendiente";


    private static final Logger LOGGER = Logger.getLogger(InternActivityDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO actividad_practicante " +
            "(id_actividad, id_practicante, horas_dedicadas, estado, fecha_realizacion, observaciones) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ACTIVITY_AND_INTERN =
            "SELECT ap.id_actividad_practicante, ap.id_actividad, ap.id_practicante, " +
            "       ap.horas_dedicadas, ap.estado, ap.fecha_realizacion, ap.observaciones, " +
            "       a.nombre AS nombre_actividad " +
            "FROM actividad_practicante ap " +
            "JOIN actividad a ON a.id_actividad = ap.id_actividad " +
            "WHERE ap.id_actividad = ? AND ap.id_practicante = ?";

    private static final String SQL_SELECT_BY_INTERN_AND_PROJECT =
            "SELECT ap.id_actividad_practicante, ap.id_actividad, ap.id_practicante, " +
            "       ap.horas_dedicadas, ap.estado, ap.fecha_realizacion, ap.observaciones, " +
            "       a.nombre AS nombre_actividad " +
            "FROM actividad_practicante ap " +
            "JOIN actividad a ON a.id_actividad = ap.id_actividad " +
            "WHERE ap.id_practicante = ? AND a.id_proyecto = ? " +
            "ORDER BY a.nombre ASC";

    private static final String SQL_SUM_HOURS_BY_INTERN =
            "SELECT COALESCE(SUM(ap.horas_dedicadas), 0) AS total_horas " +
            "FROM actividad_practicante ap " +
            "WHERE ap.id_practicante = ?";

    private static final String SQL_UPDATE =
            "UPDATE actividad_practicante " +
            "SET horas_dedicadas = ?, estado = ?, fecha_realizacion = ?, observaciones = ? " +
            "WHERE id_actividad_practicante = ?";

    @Override
    public int save(InternActivity internActivity) throws ServiceException, ValidationException {
        if (internActivity.getIdActivity() <= 0) {
            throw new ValidationException(
                    "El ID de la actividad debe ser mayor a cero. ID recibido: "
                    + internActivity.getIdActivity());
        }
        if (internActivity.getIdIntern() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: "
                    + internActivity.getIdIntern());
        }

        int generatedId = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt (1, internActivity.getIdActivity());
            statement.setInt (2, internActivity.getIdIntern());
            statement.setInt (3, internActivity.getDedicatedHours());
            statement.setString(4, internActivity.getStatus() != null
                    ? internActivity.getStatus() : STATUS_PENDING);

            if (internActivity.getCompletionDate() != null) {
                statement.setDate(5, java.sql.Date.valueOf(internActivity.getCompletionDate()));
            } else {
                statement.setNull(5, Types.DATE);
            }

            statement.setString(6, internActivity.getObservations());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                    internActivity.setIdInternActivity(generatedId);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error saving progress for activity {0} for intern {1}: {2}",
                    new Object[]{internActivity.getIdActivity(),
                                 internActivity.getIdIntern(),
                                 sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro de esta actividad para el practicante.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar el avance de actividad.", sqlException);
        }

        return generatedId;
    }

    @Override
    public InternActivity findByActivityAndIntern(int idActivity, int idIntern)
            throws ServiceException, ValidationException {
        if (idActivity <= 0) {
            throw new ValidationException(
                    "El ID de la actividad debe ser mayor a cero. ID recibido: " + idActivity);
        }
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }

        InternActivity internActivity = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement =
                     connection.prepareStatement(SQL_SELECT_BY_ACTIVITY_AND_INTERN)) {

            statement.setInt(1, idActivity);
            statement.setInt(2, idIntern);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    internActivity = mapResultSet(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error finding progress for activity {0} for intern {1}: {2}",
                    new Object[]{idActivity, idIntern, sqlException.getMessage()});
            throw new ServiceException("Error al buscar el avance de la actividad.", sqlException);
        }

        return internActivity;
    }

    @Override
    public List<InternActivity> findByInternAndProject(int idIntern, int idProject)
            throws ServiceException, ValidationException {
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }
        if (idProject <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProject);
        }

        List<InternActivity> internActivities = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement =
                     connection.prepareStatement(SQL_SELECT_BY_INTERN_AND_PROJECT)) {

            statement.setInt(1, idIntern);
            statement.setInt(2, idProject);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    internActivities.add(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error retrieving progress for intern {0} in project {1}: {2}",
                    new Object[]{idIntern, idProject, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al recuperar los avances del practicante.", sqlException);
        }

        return internActivities;
    }

    @Override
    public int getTotalHoursByIntern(int idIntern)
            throws ServiceException, ValidationException {
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }

        int totalHours = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement =
                     connection.prepareStatement(SQL_SUM_HOURS_BY_INTERN)) {

            statement.setInt(1, idIntern);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    totalHours = resultSet.getInt("total_horas");
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error calculating hours for intern {0}: {1}",
                    new Object[]{idIntern, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al calcular las horas del practicante.", sqlException);
        }

        return totalHours;
    }

    @Override
    public boolean update(InternActivity internActivity) throws ServiceException, ValidationException {
        if (internActivity.getIdInternActivity() <= 0) {
            throw new ValidationException(
                    "El ID del avance debe ser mayor a cero. ID recibido: "
                    + internActivity.getIdInternActivity());
        }

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {

            statement.setInt (1, internActivity.getDedicatedHours());
            statement.setString(2, internActivity.getStatus());

            if (internActivity.getCompletionDate() != null) {
                statement.setDate(3, java.sql.Date.valueOf(internActivity.getCompletionDate()));
            } else {
                statement.setNull(3, Types.DATE);
            }

            statement.setString(4, internActivity.getObservations());
            statement.setInt (5, internActivity.getIdInternActivity());

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error updating progress with ID {0}: {1}",
                    new Object[]{internActivity.getIdInternActivity(), sqlException.getMessage()});
            throw new ServiceException("Error al actualizar el avance de actividad.", sqlException);
        }

        return isUpdated;
    }

    private InternActivity mapResultSet(ResultSet resultSet) throws SQLException {
        InternActivity internActivity = new InternActivity();
        internActivity.setIdInternActivity(resultSet.getInt ("id_actividad_practicante"));
        internActivity.setIdActivity (resultSet.getInt ("id_actividad"));
        internActivity.setIdIntern (resultSet.getInt ("id_practicante"));
        internActivity.setDedicatedHours (resultSet.getInt ("horas_dedicadas"));
        internActivity.setStatus (resultSet.getString("estado"));
        internActivity.setObservations (resultSet.getString("observaciones"));
        internActivity.setActivityName (resultSet.getString("nombre_actividad"));

        java.sql.Date completionDate = resultSet.getDate("fecha_realizacion");
        if (completionDate != null) {
            internActivity.setCompletionDate(completionDate.toLocalDate());
        }

        return internActivity;
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

        String sql =
                "DELETE FROM actividad_practicante " +
                "WHERE id_practicante = ? " +
                "  AND id_actividad IN (SELECT id_actividad FROM actividad WHERE id_proyecto = ?)";
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, internId);
            statement.setInt(2, projectId);
            rowsAffected = statement.executeUpdate();

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error deleting activities for intern {0} in project {1}: {2}",
                    new Object[]{internId, projectId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al eliminar actividades del practicante.", sqlException);
        }

        return rowsAffected >= 0;
    }
}
