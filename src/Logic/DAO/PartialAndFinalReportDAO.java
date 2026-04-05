package Logic.DAO;

import Logic.DTOs.PartialAndFinalReport;
import Logic.DTOs.Report;
import Logic.Interface.IReportDAO;
import DataAccess.BDConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartialAndFinalReportDAO extends ReportDAO implements IReportDAO {
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
    public int save(Report report) throws SQLException {
        PartialAndFinalReport pfReport = (PartialAndFinalReport) report;
        int rowsAffected = 0;

        Connection connection = BDConnection.connectDatabase();
        try {
            connection.setAutoCommit(false);

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
    public PartialAndFinalReport getById(int idReport) throws SQLException {
        PartialAndFinalReport report = null;

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


    private PartialAndFinalReport mapResultSet(ResultSet rs) throws SQLException {
        PartialAndFinalReport report = new PartialAndFinalReport();

        // Campos base (heredados de Report)
        report.setIdReport    (rs.getInt   ("id_reporte"));
        report.setIdIntern    (rs.getInt   ("id_practicante"));
        report.setIdProyect   (rs.getInt   ("id_proyecto"));
        report.setIdProfessor (rs.getInt   ("id_profesor"));
        report.setReportType  (rs.getString("tipo_reporte"));
        report.setPeriod      (rs.getString("periodo"));
        report.setDocumentPath(rs.getString("ruta_documento"));
        report.setStatus      (rs.getString("estado"));
        report.setSumissionDate(rs.getDate ("fecha_entrega"));

        // Campos específicos
        report.setIdPartialAndFinalReport(rs.getInt   ("id_reporte"));
        report.setReportNumber           (rs.getInt   ("numero_informe"));
        report.setCoveredHours           (rs.getInt   ("horas_cubiertas"));
        report.setGeneralObjective       (rs.getString("objetivo_general"));
        report.setMethodology            (rs.getString("metodologia"));
        report.setObtainedResults        (rs.getString("resultados_obtenidos"));
        report.setObservations           (rs.getString("observaciones"));

        return report;
    }
}
