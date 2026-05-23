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
        if (ra.getIdReporte() <= 0 || ra.getIdActividad() <= 0) {
            throw new ValidationException(
                    "El ID de reporte e ID de actividad deben ser mayores a cero.");
        }

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT_ACTIVITY, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt   (1, ra.getIdReporte());
            statement.setInt   (2, ra.getIdActividad());
            statement.setString(3, ra.getPeriodo());
            statement.setString(4, ra.getPlanSemanas());
            statement.setString(5, ra.getRealSemanas());
            statement.setInt   (6, ra.getPorcentajeAvance());
            statement.setString(7, ra.getObservaciones());

            int rows = statement.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) ra.setIdReporteActividad(keys.getInt(1));
                }
            }
            return rows;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al guardar actividad del reporte {0}: {1}",
                    new Object[]{ra.getIdReporte(), sqlException.getMessage()});
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
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapActivity(rs));
                }
            }
            return list;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar actividades del reporte {0}: {1}",
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
        if (rd.getIdReporte() <= 0) {
            throw new ValidationException("El ID de reporte debe ser mayor a cero.");
        }
        if (rd.getResultado() == null || rd.getResultado().isBlank()) {
            throw new ValidationException("El resultado del entregable es obligatorio.");
        }

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT_DELIVERABLE, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt   (1, rd.getIdReporte());
            statement.setString(2, rd.getResultado());
            statement.setString(3, rd.getDescripcion());
            statement.setInt   (4, rd.getPorcentajeAvance());
            statement.setString(5, rd.getObservaciones());

            int rows = statement.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) rd.setIdReporteEntregable(keys.getInt(1));
                }
            }
            return rows;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al guardar entregable del reporte {0}: {1}",
                    new Object[]{rd.getIdReporte(), sqlException.getMessage()});
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
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDeliverable(rs));
                }
            }
            return list;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar entregables del reporte {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al buscar los entregables del reporte.", sqlException);
        }
    }

    private ReportActivity mapActivity(ResultSet rs) throws SQLException {
        ReportActivity ra = new ReportActivity();
        ra.setIdReporteActividad(rs.getInt   ("id_reporte_actividad"));
        ra.setIdReporte         (rs.getInt   ("id_reporte"));
        ra.setIdActividad       (rs.getInt   ("id_actividad"));
        ra.setActivityName      (rs.getString("actividad_nombre"));
        ra.setPeriodo           (rs.getString("periodo"));
        ra.setPlanSemanas       (rs.getString("plan_semanas"));
        ra.setRealSemanas       (rs.getString("real_semanas"));
        ra.setPorcentajeAvance  (rs.getInt   ("porcentaje_avance"));
        ra.setObservaciones     (rs.getString("observaciones"));
        return ra;
    }

    private ReportDeliverable mapDeliverable(ResultSet rs) throws SQLException {
        ReportDeliverable rd = new ReportDeliverable();
        rd.setIdReporteEntregable(rs.getInt   ("id_reporte_entregable"));
        rd.setIdReporte          (rs.getInt   ("id_reporte"));
        rd.setResultado          (rs.getString("resultado"));
        rd.setDescripcion        (rs.getString("descripcion"));
        rd.setPorcentajeAvance   (rs.getInt   ("porcentaje_avance"));
        rd.setObservaciones      (rs.getString("observaciones"));
        return rd;
    }
}
