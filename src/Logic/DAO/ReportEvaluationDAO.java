package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.ReportEvaluation;
import Logic.Interface.IReportEvaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportEvaluationDAO implements IReportEvaluation {

    private static final String SQL_INSERT =
            "INSERT INTO evaluacion_reporte " +
                    "(id_reporte, calificacion, retroalimentacion, fecha_evaluacion) " +
                    "VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
            "SELECT id_evaluacion_reporte, id_reporte, calificacion, " +
                    "       retroalimentacion, porcentaje_avance, fecha_evaluacion " +
                    "FROM evaluacion_reporte " +
                    "WHERE id_evaluacion_reporte = ?";

    private static final String SQL_SELECT_BY_ID_REPORT =
            "SELECT id_evaluacion_reporte, id_reporte, calificacion, " +
                    "       retroalimentacion, porcentaje_avance, fecha_evaluacion " +
                    "FROM evaluacion_reporte " +
                    "WHERE id_reporte = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT id_evaluacion_reporte, id_reporte, calificacion, " +
                    "       retroalimentacion, porcentaje_avance, fecha_evaluacion " +
                    "FROM evaluacion_reporte";

    @Override
    public int save(ReportEvaluation reportEvaluation) throws SQLException {
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt   (1, reportEvaluation.getIdReport());
            statement.setInt   (2, reportEvaluation.getGrade());
            statement.setString(3, reportEvaluation.getFeedback());
            statement.setDate  (4, new java.sql.Date(
                    reportEvaluation.getEvaluationDate().getTime()));

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reportEvaluation.setIdReportEvaluation(generatedKeys.getInt(1));
                }
            }
        }

        return rowsAffected;
    }

    @Override
    public ReportEvaluation getById(int idReportEvaluation) throws SQLException {
        ReportEvaluation reportEvaluation = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idReportEvaluation);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    reportEvaluation = mapResultSet(resultSet);
                }
            }
        }

        return reportEvaluation;
    }

    @Override
    public ReportEvaluation getByIdReport(int idReport) throws SQLException {
        ReportEvaluation reportEvaluation = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID_REPORT)) {

            statement.setInt(1, idReport);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    reportEvaluation = mapResultSet(resultSet);
                }
            }
        }

        return reportEvaluation;
    }

    @Override
    public List<ReportEvaluation> getAll() throws SQLException {
        List<ReportEvaluation> reportEvaluations = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reportEvaluations.add(mapResultSet(resultSet));
            }
        }

        return reportEvaluations;
    }

    private ReportEvaluation mapResultSet(ResultSet resultSet) throws SQLException {
        ReportEvaluation reportEvaluation = new ReportEvaluation();
        reportEvaluation.setIdReportEvaluation(resultSet.getInt   ("id_evaluacion_reporte"));
        reportEvaluation.setIdReport          (resultSet.getInt   ("id_reporte"));
        reportEvaluation.setGrade             (resultSet.getInt   ("calificacion"));
        reportEvaluation.setFeedback          (resultSet.getString("retroalimentacion"));
        reportEvaluation.setEvaluationDate    (resultSet.getDate  ("fecha_evaluacion"));
        return reportEvaluation;
    }
}
