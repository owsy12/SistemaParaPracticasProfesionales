package Logic.DAO;

import Logic.DTOs.Report;
import Logic.Interface.IReportDAO;
import DataAccess.BDConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO implements IReportDAO {
    private static final String SQL_INSERT =
            "INSERT INTO reporte " +
                    "(id_practicante, id_proyecto, id_profesor, " +
                    " tipo_reporte, periodo, ruta_documento, estado, fecha_entrega) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_reporte, id_practicante, id_proyecto, id_profesor, " +
                    "       tipo_reporte, periodo, ruta_documento, estado, fecha_entrega " +
                    "FROM reporte " +
                    "WHERE id_reporte = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT id_reporte, id_practicante, id_proyecto, id_profesor, " +
                    "       tipo_reporte, periodo, ruta_documento, estado, fecha_entrega " +
                    "FROM reporte";

    private static final String SQL_SELECT_PENDING =
            "SELECT id_reporte, id_practicante, id_proyecto, id_profesor, " +
                    "       tipo_reporte, periodo, ruta_documento, estado, fecha_entrega " +
                    "FROM reporte " +
                    "WHERE estado = 'Pendiente'";

    @Override
    public int save(Report report) throws SQLException {
        int rowsAffected = 0;

        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt   (1, report.getIdIntern());
            statement.setInt   (2, report.getIdProyect());
            statement.setInt   (3, report.getIdProfessor());
            statement.setString(4, report.getReportType());
            statement.setString(5, report.getPeriod());
            statement.setString(6, report.getDocumentPath());
            statement.setString(7, report.getStatus());
            statement.setDate  (8, new java.sql.Date(report.getSumissionDate().getTime()));

            rowsAffected = statement.executeUpdate();

            // Recuperar el ID generado y asignarlo al DTO
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    report.setIdReport(generatedKeys.getInt(1));
                }
            }
        }

        return rowsAffected;
    }

    @Override
    public Report getById(int idReport) throws SQLException {
        Report report = null;

        try (Connection connection = BDConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idReport);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    report = mapResultSetToReport(resultSet);
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
                reports.add(mapResultSetToReport(resultSet));
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
                reports.add(mapResultSetToReport(resultSet));
            }
        }

        return reports;
    }

    protected Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setIdReport    (rs.getInt   ("id_reporte"));
        report.setIdIntern    (rs.getInt   ("id_practicante"));
        report.setIdProyect   (rs.getInt   ("id_proyecto"));
        report.setIdProfessor (rs.getInt   ("id_profesor"));
        report.setReportType  (rs.getString("tipo_reporte"));
        report.setPeriod      (rs.getString("periodo"));
        report.setDocumentPath(rs.getString("ruta_documento"));
        report.setStatus      (rs.getString("estado"));
        report.setSumissionDate(rs.getDate ("fecha_entrega"));
        return report;
    }
}
