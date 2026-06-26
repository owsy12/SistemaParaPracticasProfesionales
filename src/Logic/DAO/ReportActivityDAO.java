package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportDeliverable;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IReportActivityDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportActivityDAO implements IReportActivityDAO {

    private static final Logger LOGGER = Logger.getLogger(ReportActivityDAO.class.getName());

    private static final String REPORT_TYPE_MONTHLY = "Mensual";

    private static final String SQL_INSERT_ACTIVITY =
            "INSERT INTO reporte_actividad " +
            "(id_reporte, id_actividad, periodo, plan_semanas, real_semanas, " +
            " porcentaje_avance, observaciones) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_REPORT =
            "SELECT ra.id_reporte_actividad, ra.id_reporte, ra.id_actividad, " +
            "       a.nombre AS actividad_nombre, " +
            "       ra.periodo, ra.plan_semanas, ra.real_semanas, " +
            "       ra.porcentaje_avance, ra.observaciones " +
            "FROM reporte_actividad ra " +
            "JOIN actividad a ON a.id_actividad = ra.id_actividad " +
            "WHERE ra.id_reporte = ? ORDER BY ra.id_reporte_actividad ASC";

    private static final String SQL_SELECT_ACTIVITY_IDS_IN_MONTHLY_REPORTS =
            "SELECT DISTINCT ra.id_actividad " +
            "FROM reporte_actividad ra " +
            "JOIN reporte r ON r.id_reporte = ra.id_reporte " +
            "WHERE r.id_practicante = ? AND r.tipo_reporte = '" + REPORT_TYPE_MONTHLY + "'";

    private static final String SQL_EXISTS_BY_ACTIVITY =
            "SELECT COUNT(*) AS total FROM reporte_actividad WHERE id_actividad = ?";

    private static final String SQL_INSERT_DELIVERABLE =
            "INSERT INTO reporte_entregable " +
            "(id_reporte, resultado, descripcion, porcentaje_avance, observaciones) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_DELIVERABLES_BY_REPORT =
            "SELECT id_reporte_entregable, id_reporte, resultado, descripcion, " +
            "       porcentaje_avance, observaciones " +
            "FROM reporte_entregable " +
            "WHERE id_reporte = ? ORDER BY id_reporte_entregable ASC";

    @Override
    public int save(ReportActivity ra) throws ServiceException, ValidationException {
        if (ra == null) {
            throw new ValidationException("La actividad del reporte no puede ser nula.");
        }
        if (ra.getIdReport() <= 0 || ra.getIdActivity() <= 0) {
            throw new ValidationException(
                    "El ID de reporte e ID de actividad deben ser mayores a cero.");
        }

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT_ACTIVITY, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt (1, ra.getIdReport());
            statement.setInt (2, ra.getIdActivity());
            statement.setString(3, ra.getPeriod());
            statement.setString(4, ra.getWeeklyPlan());
            statement.setString(5, ra.getRealWeeks());
            statement.setInt (6, ra.getAdvancePercentage());
            statement.setString(7, ra.getObservations());

            int rows = statement.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) ra.setIdReportActivity(keys.getInt(1));
                }
            }
            return rows;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error saving report activity {0}: {1}",
                    new Object[]{ra.getIdReport(), sqlException.getMessage()});
            throw new ServiceException(
                    "Error al guardar la actividad del reporte.", sqlException);
        }
    }

    @Override
    public List<ReportActivity> findByReport(int idReport)
            throws ServiceException, ValidationException {

        if (idReport <= 0) {
            throw new ValidationException("El ID del reporte debe ser mayor a cero.");
        }

        List<ReportActivity> list = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_REPORT)) {

            statement.setInt(1, idReport);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapActivity(resultSet));
                }
            }
            return list;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error finding activities for report {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al buscar las actividades del reporte.", sqlException);
        }
    }

    @Override
    public int saveDeliverable(ReportDeliverable rd)
            throws ServiceException, ValidationException {

        if (rd == null) {
            throw new ValidationException("El entregable del reporte no puede ser nulo.");
        }
        if (rd.getIdReport() <= 0) {
            throw new ValidationException("El ID de reporte debe ser mayor a cero.");
        }
        if (rd.getResult() == null || rd.getResult().isBlank()) {
            throw new ValidationException("El resultado del entregable es obligatorio.");
        }

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT_DELIVERABLE, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt (1, rd.getIdReport());
            statement.setString(2, rd.getResult());
            statement.setString(3, rd.getDescription());
            statement.setInt (4, rd.getAdvancePercentage());
            statement.setString(5, rd.getObservations());

            int rows = statement.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) rd.setIdReportDeliverable(keys.getInt(1));
                }
            }
            return rows;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error saving report deliverable {0}: {1}",
                    new Object[]{rd.getIdReport(), sqlException.getMessage()});
            throw new ServiceException(
                    "Error al guardar el entregable del reporte.", sqlException);
        }
    }

    @Override
    public List<ReportDeliverable> findDeliverablesByReport(int idReport)
            throws ServiceException, ValidationException {

        if (idReport <= 0) {
            throw new ValidationException("El ID del reporte debe ser mayor a cero.");
        }

        List<ReportDeliverable> list = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_SELECT_DELIVERABLES_BY_REPORT)) {

            statement.setInt(1, idReport);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapDeliverable(resultSet));
                }
            }
            return list;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error finding deliverables for report {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al buscar los entregables del reporte.", sqlException);
        }
    }

    @Override
    public List<Integer> findActivityIdsInMonthlyReportsByIntern(int internId)
            throws ServiceException, ValidationException {

        if (internId <= 0) {
            throw new ValidationException("El ID del practicante debe ser mayor a cero.");
        }

        List<Integer> activityIds = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_SELECT_ACTIVITY_IDS_IN_MONTHLY_REPORTS)) {

            statement.setInt(1, internId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int activityId = resultSet.getInt("id_actividad");
                    activityIds.add(activityId);
                }
            }
            return activityIds;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error finding activity IDs in monthly reports for intern {0}: {1}",
                    new Object[]{internId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al buscar las actividades de reportes mensuales.", sqlException);
        }
    }

    public boolean existsByActivity(int idActivity) throws ServiceException, ValidationException {
        if (idActivity <= 0) {
            throw new ValidationException(
                    "El ID de la actividad debe ser mayor a cero. ID recibido: " + idActivity);
        }

        boolean exists = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_EXISTS_BY_ACTIVITY)) {

            statement.setInt(1, idActivity);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    exists = resultSet.getInt("total") > 0;
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error verifying whether activity {0} belongs to a report: {1}",
                    new Object[]{idActivity, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al verificar la actividad en reportes.", sqlException);
        }

        return exists;
    }

    private ReportActivity mapActivity(ResultSet rs) throws SQLException {
        ReportActivity ra = new ReportActivity();
        ra.setIdReportActivity(rs.getInt ("id_reporte_actividad"));
        ra.setIdReport (rs.getInt ("id_reporte"));
        ra.setIdActivity (rs.getInt ("id_actividad"));
        ra.setActivityName (rs.getString("actividad_nombre"));
        ra.setPeriod (rs.getString("periodo"));
        ra.setWeeklyPlan(rs.getString("plan_semanas"));
        ra.setRealWeeks(rs.getString("real_semanas"));
        ra.setAdvancePercentage (rs.getInt ("porcentaje_avance"));
        ra.setObservations(rs.getString("observaciones"));
        return ra;
    }

    private ReportDeliverable mapDeliverable(ResultSet rs) throws SQLException {
        ReportDeliverable rd = new ReportDeliverable();
        rd.setIdReportDeliverable(rs.getInt ("id_reporte_entregable"));
        rd.setIdReport (rs.getInt ("id_reporte"));
        rd.setResult (rs.getString("resultado"));
        rd.setDescription (rs.getString("descripcion"));
        rd.setAdvancePercentage (rs.getInt ("porcentaje_avance"));
        rd.setObservations (rs.getString("observaciones"));
        return rd;
    }
}
