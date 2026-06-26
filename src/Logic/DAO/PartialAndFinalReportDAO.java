package Logic.DAO;

import Logic.DTOs.PartialAndFinalReport;
import Logic.DTOs.Report;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IReportDAO;
import DataAccess.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PartialAndFinalReportDAO extends ReportDAO implements IReportDAO {

    private static final Logger LOGGER = Logger.getLogger(PartialAndFinalReportDAO.class.getName());

    private static final String REPORT_TYPE_PARTIAL = "Parcial";
    private static final String REPORT_TYPE_FINAL = "Final";
    private static final String STATUS_PENDING = "Pendiente";

    private static final String SQL_INSERT_SPECIFIC =
            "INSERT INTO reporte_parcial_y_final " +
                    "(id_reporte_parcial, numero_informe, horas_cubiertas, " +
                    " objetivo_general, metodologia, resultados_obtenidos, observaciones) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BASE =
            "SELECT r.id_reporte, r.id_practicante, r.id_proyecto, r.id_profesor, " +
                    "       r.tipo_reporte, r.periodo, r.ruta_documento, r.ruta_documento_firmado, " +
                    "       r.estado, r.horas_reportadas, r.observaciones_profesor, " +
                    "       r.fecha_revision, r.fecha_entrega, " +
                    "       pf.numero_informe, pf.horas_cubiertas, pf.objetivo_general, " +
                    "       pf.metodologia, pf.resultados_obtenidos, pf.observaciones " +
                    "FROM reporte r " +
                    "JOIN reporte_parcial_y_final pf ON pf.id_reporte_parcial = r.id_reporte";

    private static final String SQL_SELECT_BY_ID =
            SQL_SELECT_BASE + " WHERE r.id_reporte = ?";

    private static final String SQL_SELECT_ALL =
            SQL_SELECT_BASE + " WHERE r.tipo_reporte IN ('" + REPORT_TYPE_PARTIAL + "', '" + REPORT_TYPE_FINAL + "')";

    private static final String SQL_SELECT_PENDING =
            SQL_SELECT_BASE +
                    " WHERE r.tipo_reporte IN ('" + REPORT_TYPE_PARTIAL + "', '" + REPORT_TYPE_FINAL + "') AND r.estado = '" + STATUS_PENDING + "'";

    @Override
    public int save(Report report) throws ServiceException, ValidationException {
        PartialAndFinalReport partialAndFinalReport = (PartialAndFinalReport) report;
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO reporte " +
                                "(id_practicante, id_proyecto, id_profesor, " +
                                " tipo_reporte, periodo, ruta_documento, estado, fecha_entrega) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

                    preparedStatement.setInt (1, partialAndFinalReport.getIdIntern());
                    preparedStatement.setInt (2, partialAndFinalReport.getIdProject());
                    preparedStatement.setInt (3, partialAndFinalReport.getIdProfessor());
                    preparedStatement.setString(4, partialAndFinalReport.getReportType());
                    preparedStatement.setString(5, partialAndFinalReport.getPeriod());
                    preparedStatement.setString(6, partialAndFinalReport.getDocumentPath());
                    preparedStatement.setString(7, partialAndFinalReport.getStatus());
                    preparedStatement.setDate (8, new java.sql.Date(partialAndFinalReport.getSubmissionDate().getTime()));

                    rowsAffected = preparedStatement.executeUpdate();

                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            partialAndFinalReport.setIdReport(generatedKeys.getInt(1));
                            partialAndFinalReport.setIdPartialAndFinalReport(generatedKeys.getInt(1));
                        }
                    }
                }

                try (PreparedStatement stmtSpecific = connection.prepareStatement(SQL_INSERT_SPECIFIC)) {
                    stmtSpecific.setInt (1, partialAndFinalReport.getIdReport());
                    stmtSpecific.setInt (2, partialAndFinalReport.getReportNumber());
                    stmtSpecific.setInt (3, partialAndFinalReport.getCoveredHours());
                    stmtSpecific.setString(4, partialAndFinalReport.getGeneralObjective());
                    stmtSpecific.setString(5, partialAndFinalReport.getMethodology());
                    stmtSpecific.setString(6, partialAndFinalReport.getObtainedResults());
                    stmtSpecific.setString(7, partialAndFinalReport.getObservations());
                    stmtSpecific.executeUpdate();
                }

                connection.commit();

            } catch (SQLException sqlException) {
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Error saving partial/final report: {0}",
                        sqlException.getMessage());
                throw new ServiceException("Error al guardar el reporte parcial/final.", sqlException);
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Connection error while saving partial/final report: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error de conexión al guardar el reporte.", sqlException);
        }

        return rowsAffected;
    }

    @Override
    public PartialAndFinalReport getById(int idReport) throws ServiceException, ValidationException {
        if (idReport <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + idReport);
        }
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
            LOGGER.log(Level.SEVERE, "Error retrieving report with ID {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar el reporte con ID " + idReport, sqlException);
        }

        return report;
    }

    @Override
    public List<Report> getAll() throws ServiceException {
        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reports.add(mapResultSet(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all partial/final reports: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar los reportes parciales y finales.", sqlException);
        }

        return reports;
    }

    @Override
    public List<Report> getByStatusPending() throws ServiceException {
        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_PENDING);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                reports.add(mapResultSet(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving pending partial/final reports: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar los reportes pendientes.", sqlException);
        }

        return reports;
    }

    private PartialAndFinalReport mapResultSet(ResultSet resultSet) throws SQLException {
        PartialAndFinalReport report = new PartialAndFinalReport();

        report.setIdReport (resultSet.getInt ("id_reporte"));
        report.setIdIntern (resultSet.getInt ("id_practicante"));
        report.setIdProject (resultSet.getInt ("id_proyecto"));
        report.setIdProfessor (resultSet.getInt ("id_profesor"));
        report.setReportType (resultSet.getString("tipo_reporte"));
        report.setPeriod (resultSet.getString("periodo"));
        report.setDocumentPath (resultSet.getString("ruta_documento"));
        report.setSignedDocumentPath (resultSet.getString("ruta_documento_firmado"));
        report.setStatus (resultSet.getString("estado"));
        report.setReportedHours (resultSet.getInt ("horas_reportadas"));
        report.setProfessorObservations (resultSet.getString("observaciones_profesor"));
        report.setSubmissionDate (resultSet.getDate ("fecha_entrega"));

        java.sql.Date reviewDate = resultSet.getDate("fecha_revision");
        if (reviewDate != null) {
            report.setReviewDate(reviewDate.toLocalDate());
        }

        report.setIdPartialAndFinalReport(resultSet.getInt ("id_reporte"));
        report.setReportNumber (resultSet.getInt ("numero_informe"));
        report.setCoveredHours (resultSet.getInt ("horas_cubiertas"));
        report.setGeneralObjective (resultSet.getString("objetivo_general"));
        report.setMethodology (resultSet.getString("metodologia"));
        report.setObtainedResults (resultSet.getString("resultados_obtenidos"));
        report.setObservations (resultSet.getString("observaciones"));

        return report;
    }
}
