package Logic.DAO;

import Logic.DTOs.Report;
import Logic.Exceptions.DataAccessException;
import Logic.Interface.IReportDAO;
import DataAccess.DataBaseConnection;

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
    public int save(Report report) throws DataAccessException{
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
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

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    report.setIdReport(generatedKeys.getInt(1));
                }
            }
        }catch (SQLException sqlException){
            throw new DataAccessException("Error saving report " + sqlException.getMessage(), sqlException);
        }

        return rowsAffected;
    }

    @Override
    public Report getById(int idReport) throws DataAccessException {
        Report report = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idReport);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    report = mapResultSetToReport(resultSet);
                }
            }
        }catch (SQLException sqlException){
            throw new DataAccessException("Error retrieving report with ID " + idReport, sqlException);
        }

        return report;
    }

    @Override
    public List<Report> getAll() throws DataAccessException {
        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reports.add(mapResultSetToReport(resultSet));
            }
        }catch (SQLException sqlException){
            throw new DataAccessException("Error retrieving all reports", sqlException);
        }

        return reports;
    }

    @Override
    public List<Report> getByStatusPending() throws DataAccessException {
        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_PENDING);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reports.add(mapResultSetToReport(resultSet));
            }
        }catch (SQLException sqlException){
            throw new DataAccessException("Error retrieving pending reports", sqlException);
        }

        return reports;
    }

    protected Report mapResultSetToReport(ResultSet rresultSet) throws SQLException {
        Report report = new Report();
        report.setIdReport    (rresultSet.getInt   ("id_reporte"));
        report.setIdIntern    (rresultSet.getInt   ("id_practicante"));
        report.setIdProyect   (rresultSet.getInt   ("id_proyecto"));
        report.setIdProfessor (rresultSet.getInt   ("id_profesor"));
        report.setReportType  (rresultSet.getString("tipo_reporte"));
        report.setPeriod      (rresultSet.getString("periodo"));
        report.setDocumentPath(rresultSet.getString("ruta_documento"));
        report.setStatus      (rresultSet.getString("estado"));
        report.setSumissionDate(rresultSet.getDate ("fecha_entrega"));
        return report;
    }
}
