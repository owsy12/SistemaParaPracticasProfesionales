package Logic.DAO;

import Logic.DTOs.PartialAndFinalReport;
import Logic.DTOs.Report;
import Logic.Exceptions.DatabaseException;
import Logic.Interface.IReportDAO;
import DataAccess.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PartialAndFinalReportDAO extends ReportDAO implements IReportDAO {

    private static final Logger LOGGER = Logger.getLogger(PartialAndFinalReportDAO.class.getName());

    private static final String SQL_INSERT_SPECIFIC =
            "INSERT INTO reporte_parcial_y_final " +
                    "(id_reporte_parcial, numero_informe, horas_cubiertas, " +
                    " objetivo_general, metodologia, resultados_obtenidos, observaciones) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BASE =
            "SELECT r.id_reporte, r.id_practicante, r.id_proyecto, r.id_profesor, " +
                    "       r.tipo_reporte, r.periodo, r.ruta_documento, r.estado, r.fecha_entrega, " +
                    "       pf.numero_informe, pf.horas_cubiertas, pf.objetivo_general, " +
                    "       pf.metodologia, pf.resultados_obtenidos, pf.observaciones " +
                    "FROM reporte r " +
                    "JOIN reporte_parcial_y_final pf ON pf.id_reporte_parcial = r.id_reporte";

    private static final String SQL_SELECT_BY_ID =
            SQL_SELECT_BASE + " WHERE r.id_reporte = ?";

    private static final String SQL_SELECT_ALL =
            SQL_SELECT_BASE + " WHERE r.tipo_reporte IN ('Parcial', 'Final')";

    private static final String SQL_SELECT_PENDING =
            SQL_SELECT_BASE +
                    " WHERE r.tipo_reporte IN ('Parcial', 'Final') AND r.estado = 'Pendiente'";

    @Override
    public int save(Report report) throws DatabaseException {
        PartialAndFinalReport pfReport = (PartialAndFinalReport) report;
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement stmtBase = connection.prepareStatement(
                        "INSERT INTO reporte " +
                                "(id_practicante, id_proyecto, id_profesor, " +
                                " tipo_reporte, periodo, ruta_documento, estado, fecha_entrega) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

                    stmtBase.setInt   (1, pfReport.getIdIntern());
                    stmtBase.setInt   (2, pfReport.getIdProyect());
                    stmtBase.setInt   (3, pfReport.getIdProfessor());
                    stmtBase.setString(4, pfReport.getReportType());
                    stmtBase.setString(5, pfReport.getPeriod());
                    stmtBase.setString(6, pfReport.getDocumentPath());
                    stmtBase.setString(7, pfReport.getStatus());
                    stmtBase.setDate  (8, new java.sql.Date(pfReport.getSumissionDate().getTime()));

                    rowsAffected = stmtBase.executeUpdate();

                    try (ResultSet generatedKeys = stmtBase.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            pfReport.setIdReport(generatedKeys.getInt(1));
                            pfReport.setIdPartialAndFinalReport(generatedKeys.getInt(1));
                        }
                    }
                }

                try (PreparedStatement stmtSpecific = connection.prepareStatement(SQL_INSERT_SPECIFIC)) {
                    stmtSpecific.setInt   (1, pfReport.getIdReport());
                    stmtSpecific.setInt   (2, pfReport.getReportNumber());
                    stmtSpecific.setInt   (3, pfReport.getCoveredHours());
                    stmtSpecific.setString(4, pfReport.getGeneralObjective());
                    stmtSpecific.setString(5, pfReport.getMethodology());
                    stmtSpecific.setString(6, pfReport.getObtainedResults());
                    stmtSpecific.setString(7, pfReport.getObservations());
                    stmtSpecific.executeUpdate();
                }

                connection.commit();

            } catch (SQLException sqlException) {
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Error al guardar reporte parcial/final: {0}",
                        sqlException.getMessage());
                throw new DatabaseException("Error al guardar el reporte parcial/final.", sqlException);
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error de conexión al guardar reporte parcial/final: {0}",
                    sqlException.getMessage());
            throw new DatabaseException("Error de conexión al guardar el reporte.", sqlException);
        }

        return rowsAffected;
    }

    @Override
    public PartialAndFinalReport getById(int idReport) throws DatabaseException {
        PartialAndFinalReport report = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idReport);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    report = mapResultSet(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar reporte con ID {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new DatabaseException("Error al recuperar el reporte con ID " + idReport, sqlException);
        }

        return report;
    }

    @Override
    public List<Report> getAll() throws DatabaseException {
        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reports.add(mapResultSet(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todos los reportes parciales/finales: {0}",
                    sqlException.getMessage());
            throw new DatabaseException("Error al recuperar los reportes parciales y finales.", sqlException);
        }

        return reports;
    }

    @Override
    public List<Report> getByStatusPending() throws DatabaseException {
        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_PENDING);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reports.add(mapResultSet(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar reportes parciales/finales pendientes: {0}",
                    sqlException.getMessage());
            throw new DatabaseException("Error al recuperar los reportes pendientes.", sqlException);
        }

        return reports;
    }

    private PartialAndFinalReport mapResultSet(ResultSet resultSet) throws SQLException {
        PartialAndFinalReport report = new PartialAndFinalReport();

        report.setIdReport               (resultSet.getInt   ("id_reporte"));
        report.setIdIntern               (resultSet.getInt   ("id_practicante"));
        report.setIdProyect              (resultSet.getInt   ("id_proyecto"));
        report.setIdProfessor            (resultSet.getInt   ("id_profesor"));
        report.setReportType             (resultSet.getString("tipo_reporte"));
        report.setPeriod                 (resultSet.getString("periodo"));
        report.setDocumentPath           (resultSet.getString("ruta_documento"));
        report.setStatus                 (resultSet.getString("estado"));
        report.setSumissionDate          (resultSet.getDate  ("fecha_entrega"));
        report.setIdPartialAndFinalReport(resultSet.getInt   ("id_reporte"));
        report.setReportNumber           (resultSet.getInt   ("numero_informe"));
        report.setCoveredHours           (resultSet.getInt   ("horas_cubiertas"));
        report.setGeneralObjective       (resultSet.getString("objetivo_general"));
        report.setMethodology            (resultSet.getString("metodologia"));
        report.setObtainedResults        (resultSet.getString("resultados_obtenidos"));
        report.setObservations           (resultSet.getString("observaciones"));

        return report;
    }
}
