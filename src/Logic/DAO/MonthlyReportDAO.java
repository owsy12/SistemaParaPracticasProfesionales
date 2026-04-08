package Logic.DAO;

import DataAccess.BDConnection;
import Logic.DTOs.MonthlyReport;
import Logic.DTOs.Report;
import Logic.Interface.IReportDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonthlyReportDAO extends ReportDAO implements IReportDAO {
    private static final String SQL_INSERT_SPECIFIC =
            "INSERT INTO reporte_mensual " +
                    "(id_reporte_mensual, mes, anio, horas_reportadas, bloque, seccion) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BASE =
            "SELECT r.id_reporte, r.id_practicante, r.id_proyecto, r.id_profesor, " +
                    "       r.tipo_reporte, r.periodo, r.ruta_documento, r.estado, r.fecha_entrega, " +
                    "       m.mes, m.anio, m.horas_reportadas, m.bloque, m.seccion " +
                    "FROM reporte r " +
                    "JOIN reporte_mensual m ON m.id_reporte_mensual = r.id_reporte";

    private static final String SQL_SELECT_BY_ID =
            SQL_SELECT_BASE + " WHERE r.id_reporte = ?";

    private static final String SQL_SELECT_ALL =
            SQL_SELECT_BASE + " WHERE r.tipo_reporte = 'Mensual'";

    private static final String SQL_SELECT_PENDING =
            SQL_SELECT_BASE +
                    " WHERE r.tipo_reporte = 'Mensual' AND r.estado = 'Pendiente'";

    @Override
    public int save(Report report) throws SQLException {
        MonthlyReport monthlyReport = (MonthlyReport) report;
        int rowsAffected = 0;

        Connection connection = BDConnection.connectDatabase();
        try {
            connection.setAutoCommit(false);

            // Paso 1: insertar en tabla base
            try (PreparedStatement stmtBase = connection.prepareStatement(
                    "INSERT INTO reporte " +
                            "(id_practicante, id_proyecto, id_profesor, " +
                            " tipo_reporte, periodo, ruta_documento, estado, fecha_entrega) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                stmtBase.setInt   (1, monthlyReport.getIdIntern());
                stmtBase.setInt   (2, monthlyReport.getIdProyect());
                stmtBase.setInt   (3, monthlyReport.getIdProfessor());
                stmtBase.setString(4, monthlyReport.getReportType());
                stmtBase.setString(5, monthlyReport.getPeriod());
                stmtBase.setString(6, monthlyReport.getDocumentPath());
                stmtBase.setString(7, monthlyReport.getStatus());
                stmtBase.setDate  (8, new java.sql.Date(monthlyReport.getSumissionDate().getTime()));

                rowsAffected = stmtBase.executeUpdate();

                try (ResultSet generatedKeys = stmtBase.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        monthlyReport.setIdReport(generatedKeys.getInt(1));
                        monthlyReport.setIdMonthlyReport(generatedKeys.getInt(1));
                    }
                }
            }

            try (PreparedStatement stmtSpecific = connection.prepareStatement(SQL_INSERT_SPECIFIC)) {
                stmtSpecific.setInt   (1, monthlyReport.getIdReport());
                stmtSpecific.setString(2, monthlyReport.getMonth());
                stmtSpecific.setInt   (3, monthlyReport.getYear());
                stmtSpecific.setFloat (4, 0);     // horas_reportadas no está en el DTO aún
                stmtSpecific.setString(5, monthlyReport.getBlock());
                stmtSpecific.setString(6, monthlyReport.getSection());
                stmtSpecific.executeUpdate();
            }

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }

        return rowsAffected;
    }

    @Override
    public MonthlyReport getById(int idReport) throws SQLException {
        MonthlyReport report = null;

        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idReport);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    report = mapResultSet(resultSet);
                }
            }
        }

        return report;
    }

    @Override
    public List<Report> getAll() throws SQLException {
        List<Report> reports = new ArrayList<>();

        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reports.add(mapResultSet(resultSet));
            }
        }

        return reports;
    }

    @Override
    public List<Report> getByStatusPending() throws SQLException {
        List<Report> reports = new ArrayList<>();

        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_PENDING);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reports.add(mapResultSet(resultSet));
            }
        }

        return reports;
    }

    private MonthlyReport mapResultSet(ResultSet resultSet) throws SQLException {
        MonthlyReport report = new MonthlyReport();

        // Campos base (heredados de Report)
        report.setIdReport    (resultSet.getInt   ("id_reporte"));
        report.setIdIntern    (resultSet.getInt   ("id_practicante"));
        report.setIdProyect   (resultSet.getInt   ("id_proyecto"));
        report.setIdProfessor (resultSet.getInt   ("id_profesor"));
        report.setReportType  (resultSet.getString("tipo_reporte"));
        report.setPeriod      (resultSet.getString("periodo"));
        report.setDocumentPath(resultSet.getString("ruta_documento"));
        report.setStatus      (resultSet.getString("estado"));
        report.setSumissionDate(resultSet.getDate ("fecha_entrega"));

        // Campos específicos
        report.setIdMonthlyReport(resultSet.getInt   ("id_reporte"));
        report.setMonth          (resultSet.getString("mes"));
        report.setYear           (resultSet.getInt   ("anio"));
        report.setBlock          (resultSet.getString("bloque"));
        report.setSection        (resultSet.getString("seccion"));

        return report;
    }
}
