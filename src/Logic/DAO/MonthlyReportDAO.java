package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.MonthlyReport;
import Logic.DTOs.Report;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IReportDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonthlyReportDAO extends ReportDAO implements IReportDAO {

    private static final Logger LOGGER = Logger.getLogger(MonthlyReportDAO.class.getName());

    private static final String SQL_INSERT_SPECIFIC =
            "INSERT INTO reporte_mensual " +
                    "(id_reporte_mensual, mes, anio, horas_reportadas, bloque, seccion) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_BASE =
            "SELECT r.id_reporte, r.id_practicante, r.id_proyecto, r.id_profesor, " +
                    "       r.tipo_reporte, r.periodo, r.ruta_documento, r.ruta_documento_firmado, " +
                    "       r.estado, r.horas_reportadas, r.observaciones_profesor, " +
                    "       r.fecha_revision, r.fecha_entrega, " +
                    "       m.mes, m.anio, m.horas_reportadas AS horas_mensual, m.bloque, m.seccion " +
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
    public int save(Report report) throws ServiceException, ValidationException {
        MonthlyReport monthlyReport = (MonthlyReport) report;
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO reporte " +
                                "(id_practicante, id_proyecto, id_profesor, " +
                                " tipo_reporte, periodo, ruta_documento, estado, " +
                                " horas_reportadas, fecha_entrega) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

                    preparedStatement.setInt   (1, monthlyReport.getIdIntern());
                    preparedStatement.setInt   (2, monthlyReport.getIdProyect());
                    preparedStatement.setInt   (3, monthlyReport.getIdProfessor());
                    preparedStatement.setString(4, monthlyReport.getReportType());
                    preparedStatement.setString(5, monthlyReport.getPeriod());
                    preparedStatement.setString(6, monthlyReport.getDocumentPath());
                    preparedStatement.setString(7, monthlyReport.getStatus());
                    preparedStatement.setInt   (8, monthlyReport.getMonthlyHours());
                    preparedStatement.setDate  (9, new java.sql.Date(monthlyReport.getSumissionDate().getTime()));

                    rowsAffected = preparedStatement.executeUpdate();

                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            monthlyReport.setIdReport(generatedKeys.getInt(1));
                            monthlyReport.setIdMonthlyReport(generatedKeys.getInt(1));
                        }
                    }
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_SPECIFIC)) {
                    preparedStatement.setInt   (1, monthlyReport.getIdReport());
                    preparedStatement.setString(2, monthlyReport.getMonth());
                    preparedStatement.setInt   (3, monthlyReport.getYear());
                    preparedStatement.setInt   (4, monthlyReport.getMonthlyHours());
                    preparedStatement.setString(5, monthlyReport.getBlock());
                    preparedStatement.setString(6, monthlyReport.getSection());
                    preparedStatement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException sqlException) {
                connection.rollback();
                LOGGER.log(Level.SEVERE, "Error al guardar reporte mensual: {0}", sqlException.getMessage());
                throw new ServiceException("Error al guardar el reporte mensual.", sqlException);
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al conectar a la base de datos: {0}", sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error de conexión al guardar el reporte mensual.", sqlException);
        }

        return rowsAffected;
    }

    @Override
    public MonthlyReport getById(int idReport) throws ServiceException, ValidationException {
        if (idReport <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + idReport);
        }
        MonthlyReport report = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idReport);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    report = mapResultSet(resultSet);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar reporte mensual con ID {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar el reporte mensual con ID " + idReport, sqlException);
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
            LOGGER.log(Level.SEVERE, "Error al recuperar todos los reportes mensuales: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar los reportes mensuales.", sqlException);
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
            LOGGER.log(Level.SEVERE, "Error al recuperar reportes mensuales pendientes: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar los reportes mensuales pendientes.", sqlException);
        }

        return reports;
    }

    private MonthlyReport mapResultSet(ResultSet resultSet) throws SQLException {
        MonthlyReport report = new MonthlyReport();

        report.setIdReport              (resultSet.getInt   ("id_reporte"));
        report.setIdIntern              (resultSet.getInt   ("id_practicante"));
        report.setIdProyect             (resultSet.getInt   ("id_proyecto"));
        report.setIdProfessor           (resultSet.getInt   ("id_profesor"));
        report.setReportType            (resultSet.getString("tipo_reporte"));
        report.setPeriod                (resultSet.getString("periodo"));
        report.setDocumentPath          (resultSet.getString("ruta_documento"));
        report.setSignedDocumentPath    (resultSet.getString("ruta_documento_firmado"));
        report.setStatus                (resultSet.getString("estado"));
        report.setProfessorObservations (resultSet.getString("observaciones_profesor"));
        report.setSumissionDate         (resultSet.getDate  ("fecha_entrega"));

        java.sql.Date reviewDate = resultSet.getDate("fecha_revision");
        if (reviewDate != null) {
            report.setReviewDate(reviewDate.toLocalDate());
        }

        report.setIdMonthlyReport(resultSet.getInt   ("id_reporte"));
        report.setMonth          (resultSet.getString("mes"));
        report.setYear           (resultSet.getInt   ("anio"));
        int monthlyHours = resultSet.getInt("horas_mensual");
        report.setMonthlyHours   (monthlyHours);
        report.setReportedHours  (monthlyHours);
        report.setBlock          (resultSet.getString("bloque"));
        report.setSection        (resultSet.getString("seccion"));

        return report;
    }
}
