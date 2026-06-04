package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.ReportEvaluation;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IReportEvaluation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportEvaluationDAO implements IReportEvaluation {

    private static final Logger LOGGER = Logger.getLogger(ReportEvaluationDAO.class.getName());
    private static final String SQL_INSERT =
            "INSERT INTO evaluacion_reporte " +
                    "(id_reporte, calificacion, retroalimentacion, fecha_evaluacion) " +
                    "VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
            "SELECT id_evaluacion_reporte, id_reporte, calificacion, " +
                    "       retroalimentacion, porcentaje_avance, fecha_evaluacion " +
                    "FROM evaluacion_reporte WHERE id_evaluacion_reporte = ?";
    private static final String SQL_SELECT_BY_ID_REPORT =
            "SELECT id_evaluacion_reporte, id_reporte, calificacion, " +
                    "       retroalimentacion, porcentaje_avance, fecha_evaluacion " +
                    "FROM evaluacion_reporte WHERE id_reporte = ?";
    private static final String SQL_SELECT_ALL =
            "SELECT id_evaluacion_reporte, id_reporte, calificacion, " +
                    "       retroalimentacion, porcentaje_avance, fecha_evaluacion " +
                    "FROM evaluacion_reporte";

    @Override
    public int save(ReportEvaluation reportEvaluation) throws ServiceException, ValidationException {
        if (reportEvaluation.getIdReport() <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: "
                            + reportEvaluation.getIdReport());
        }
        if (reportEvaluation.getGrade() < 0 || reportEvaluation.getGrade() > 10) {
            throw new ValidationException(
                    "La calificación debe estar entre 0 y 10. Valor recibido: "
                            + reportEvaluation.getGrade());
        }
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt (1, reportEvaluation.getIdReport());
            statement.setInt (2, reportEvaluation.getGrade());
            statement.setString(3, reportEvaluation.getFeedback());
            statement.setDate (4, new java.sql.Date(reportEvaluation.getEvaluationDate().getTime()));

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reportEvaluation.setIdReportEvaluation(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar evaluación del reporte {0}: {1}",
                    new Object[]{reportEvaluation.getIdReport(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar la evaluación de reporte.", sqlException);
        }

        return rowsAffected;
    }

    @Override
    public ReportEvaluation getById(int idReportEvaluation)
            throws ServiceException, ValidationException {
        if (idReportEvaluation <= 0) {
            throw new ValidationException(
                    "El ID de la evaluación debe ser mayor a cero. ID recibido: "
                            + idReportEvaluation);
        }
        ReportEvaluation reportEvaluation = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idReportEvaluation);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    reportEvaluation = mapResultSet(resultSet);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar evaluación con ID {0}: {1}",
                    new Object[]{idReportEvaluation, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar la evaluación de reporte por ID.", sqlException);
        }

        return reportEvaluation;
    }

    @Override
    public ReportEvaluation getByIdReport(int idReport) throws ServiceException, ValidationException {
        if (idReport <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + idReport);
        }
        ReportEvaluation reportEvaluation = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID_REPORT)) {

            statement.setInt(1, idReport);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    reportEvaluation = mapResultSet(resultSet);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar evaluación para reporte {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar la evaluación de reporte por ID de reporte.", sqlException);
        }

        return reportEvaluation;
    }

    @Override
    public List<ReportEvaluation> getAll() throws ServiceException {
        List<ReportEvaluation> reportEvaluations = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reportEvaluations.add(mapResultSet(resultSet));
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todas las evaluaciones de reporte: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar todas las evaluaciones de reporte.", sqlException);
        }

        return reportEvaluations;
    }

    private ReportEvaluation mapResultSet(ResultSet resultSet) throws SQLException {
        ReportEvaluation reportEvaluation = new ReportEvaluation();
        reportEvaluation.setIdReportEvaluation(resultSet.getInt ("id_evaluacion_reporte"));
        reportEvaluation.setIdReport (resultSet.getInt ("id_reporte"));
        reportEvaluation.setGrade (resultSet.getInt ("calificacion"));
        reportEvaluation.setFeedback (resultSet.getString("retroalimentacion"));
        reportEvaluation.setEvaluationDate (resultSet.getDate ("fecha_evaluacion"));
        return reportEvaluation;
    }
}
