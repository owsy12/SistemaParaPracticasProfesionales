package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Activity;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IActivityDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.sql.Date.valueOf;
import static java.sql.Types.DATE;

public class ActivityDAO implements IActivityDAO {

    private static final Logger LOGGER = Logger.getLogger(ActivityDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO actividad (id_proyecto, id_practicante, nombre, descripcion, " +
            "fecha_inicio, fecha_fin, fecha_creacion, estado) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_actividad, id_proyecto, id_practicante, nombre, descripcion, " +
            "fecha_inicio, fecha_fin, fecha_creacion, estado " +
            "FROM actividad WHERE id_actividad = ?";

    private static final String SQL_SELECT_BY_PROJECT =
            "SELECT id_actividad, id_proyecto, id_practicante, nombre, descripcion, " +
            "fecha_inicio, fecha_fin, fecha_creacion, estado " +
            "FROM actividad WHERE id_proyecto = ? " +
            "AND estado = 'Activa' " +
            "ORDER BY fecha_creacion ASC";

    private static final String SQL_SELECT_BY_INTERN_AND_PROJECT =
            "SELECT id_actividad, id_proyecto, id_practicante, nombre, descripcion, " +
            "fecha_inicio, fecha_fin, fecha_creacion, estado " +
            "FROM actividad WHERE id_practicante = ? AND id_proyecto = ? " +
            "AND estado = 'Activa' " +
            "ORDER BY fecha_creacion ASC";

    private static final String SQL_UPDATE =
            "UPDATE actividad SET nombre = ?, descripcion = ?, " +
            "fecha_inicio = ?, fecha_fin = ?, estado = ? " +
            "WHERE id_actividad = ?";

    private static final String SQL_DEACTIVATE =
            "UPDATE actividad SET estado = 'Inactiva' WHERE id_actividad = ?";

    private static final String SQL_DELETE =
            "DELETE FROM actividad WHERE id_actividad = ?";

    @Override
    public int save(Activity activity) throws ServiceException, ValidationException {
        if (activity.getIdProject() <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + activity.getIdProject());
        }
        if (activity.getIdIntern() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + activity.getIdIntern());
        }
        if (activity.getName() == null || activity.getName().isBlank()) {
            throw new ValidationException("El nombre de la actividad no puede estar vacío.");
        }

        int generatedId = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt (1, activity.getIdProject());
            statement.setInt (2, activity.getIdIntern());
            statement.setString(3, activity.getName());
            statement.setString(4, activity.getDescription());
            if (activity.getStartDate() != null) {
                statement.setDate(5, valueOf(activity.getStartDate()));
            } else {
                statement.setNull(5, DATE);
            }
            if (activity.getEndDate() != null) {
                statement.setDate(6, valueOf(activity.getEndDate()));
            } else {
                statement.setNull(6, DATE);
            }
            statement.setDate (7, activity.getCreationDate() != null
                    ? valueOf(activity.getCreationDate())
                    : new java.sql.Date(System.currentTimeMillis()));
            statement.setString(8, activity.getStatus() != null ? activity.getStatus() : "Activa");

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                    activity.setIdActivity(generatedId);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving activity for project {0}: {1}",
                    new Object[]{activity.getIdProject(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.", sqlException);
            }
            throw new ServiceException("Error al guardar la actividad.", sqlException);
        }

        return generatedId;
    }

    @Override
    public Activity findById(int idActivity) throws ServiceException, ValidationException {
        if (idActivity <= 0) {
            throw new ValidationException(
                    "El ID de la actividad debe ser mayor a cero. ID recibido: " + idActivity);
        }

        Activity activity = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idActivity);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    activity = mapResultSet(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding activity with ID {0}: {1}",
                    new Object[]{idActivity, sqlException.getMessage()});
            throw new ServiceException("Error al buscar la actividad por ID.", sqlException);
        }

        return activity;
    }

    @Override
    public List<Activity> findByProject(int idProject) throws ServiceException, ValidationException {
        if (idProject <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProject);
        }

        List<Activity> activities = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_PROJECT)) {

            statement.setInt(1, idProject);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    activities.add(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving activities for project {0}: {1}",
                    new Object[]{idProject, sqlException.getMessage()});
            throw new ServiceException("Error al recuperar las actividades del proyecto.", sqlException);
        }

        return activities;
    }

    public List<Activity> findByInternAndProject(int idIntern, int idProject)
            throws ServiceException, ValidationException {
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }
        if (idProject <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + idProject);
        }

        List<Activity> activities = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_SELECT_BY_INTERN_AND_PROJECT)) {

            statement.setInt(1, idIntern);
            statement.setInt(2, idProject);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    activities.add(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error retrieving activities for intern {0} in project {1}: {2}",
                    new Object[]{idIntern, idProject, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al recuperar las actividades del practicante.", sqlException);
        }

        return activities;
    }

    @Override
    public boolean update(Activity activity) throws ServiceException, ValidationException {
        if (activity.getIdActivity() <= 0) {
            throw new ValidationException(
                    "El ID de la actividad debe ser mayor a cero. ID recibido: "
                    + activity.getIdActivity());
        }
        if (activity.getName() == null || activity.getName().isBlank()) {
            throw new ValidationException("El nombre de la actividad no puede estar vacío.");
        }

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {

            statement.setString(1, activity.getName());
            statement.setString(2, activity.getDescription());
            if (activity.getStartDate() != null) {
                statement.setDate(3, valueOf(activity.getStartDate()));
            } else {
                statement.setNull(3, DATE);
            }
            if (activity.getEndDate() != null) {
                statement.setDate(4, valueOf(activity.getEndDate()));
            } else {
                statement.setNull(4, DATE);
            }
            statement.setString(5, activity.getStatus());
            statement.setInt (6, activity.getIdActivity());

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error updating activity with ID {0}: {1}",
                    new Object[]{activity.getIdActivity(), sqlException.getMessage()});
            throw new ServiceException("Error al actualizar la actividad.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean deactivate(int idActivity) throws ServiceException, ValidationException {
        if (idActivity <= 0) {
            throw new ValidationException(
                    "El ID de la actividad debe ser mayor a cero. ID recibido: " + idActivity);
        }

        boolean isDeactivated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_DEACTIVATE)) {

            statement.setInt(1, idActivity);

            if (statement.executeUpdate() > 0) {
                isDeactivated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deactivating activity with ID {0}: {1}",
                    new Object[]{idActivity, sqlException.getMessage()});
            throw new ServiceException("Error al desactivar la actividad.", sqlException);
        }

        return isDeactivated;
    }

    @Override
    public boolean delete(int idActivity) throws ServiceException, ValidationException {
        if (idActivity <= 0) {
            throw new ValidationException(
                    "El ID de la actividad debe ser mayor a cero. ID recibido: " + idActivity);
        }

        boolean isDeleted = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setInt(1, idActivity);

            if (statement.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deleting activity with ID {0}: {1}",
                    new Object[]{idActivity, sqlException.getMessage()});
            throw new ServiceException("Error al eliminar la actividad.", sqlException);
        }

        return isDeleted;
    }

    private Activity mapResultSet(ResultSet resultSet) throws SQLException {
        Activity activity = new Activity();
        activity.setIdActivity (resultSet.getInt ("id_actividad"));
        activity.setIdProject (resultSet.getInt ("id_proyecto"));
        activity.setIdIntern (resultSet.getInt ("id_practicante"));
        activity.setName (resultSet.getString("nombre"));
        activity.setDescription(resultSet.getString("descripcion"));
        activity.setStatus (resultSet.getString("estado"));

        java.sql.Date creationDate = resultSet.getDate("fecha_creacion");
        if (creationDate != null) {
            activity.setCreationDate(creationDate.toLocalDate());
        }

        java.sql.Date startDate = resultSet.getDate("fecha_inicio");
        if (startDate != null) {
            activity.setStartDate(startDate.toLocalDate());
        }

        java.sql.Date endDate = resultSet.getDate("fecha_fin");
        if (endDate != null) {
            activity.setEndDate(endDate.toLocalDate());
        }

        return activity;
    }
}
