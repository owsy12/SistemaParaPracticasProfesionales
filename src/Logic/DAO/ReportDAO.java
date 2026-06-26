package Logic.DAO;

import Logic.DTOs.Report;
import Logic.DTOs.ReportStatusUpdate;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IReportDAO;
import DataAccess.DataBaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportDAO implements IReportDAO {

    private static final Logger LOGGER = Logger.getLogger(ReportDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO reporte " +
            "(id_practicante, id_proyecto, id_profesor, tipo_reporte, periodo, " +
            " ruta_documento, estado, horas_reportadas, fecha_entrega, fecha_limite, entrega_tardia) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_COLUMNS =
            "SELECT id_reporte, id_practicante, id_proyecto, id_profesor, tipo_reporte, " +
            "       periodo, ruta_documento, ruta_documento_firmado, estado, horas_reportadas, " +
            "       observaciones_profesor, fecha_revision, calificacion, fecha_evaluacion, " +
            "       fecha_entrega, fecha_limite, entrega_tardia ";

    private static final String SQL_SELECT_BY_ID =
            SQL_SELECT_COLUMNS + "FROM reporte WHERE id_reporte = ?";

    private static final String SQL_SELECT_ALL =
            SQL_SELECT_COLUMNS + "FROM reporte";

    private static final String SQL_SELECT_PENDING =
            SQL_SELECT_COLUMNS + "FROM reporte WHERE estado = 'Pendiente'";

    private static final String SQL_SELECT_BY_INTERN =
            SQL_SELECT_COLUMNS + "FROM reporte WHERE id_practicante = ? ORDER BY fecha_entrega DESC";

    private static final String SQL_SELECT_BY_INTERN_WITH_MONTH =
            "SELECT r.id_reporte, r.id_practicante, r.id_proyecto, r.id_profesor, r.tipo_reporte, " +
            "       r.periodo, r.ruta_documento, r.ruta_documento_firmado, r.estado, r.horas_reportadas, " +
            "       r.observaciones_profesor, r.fecha_revision, r.calificacion, r.fecha_evaluacion, " +
            "       r.fecha_entrega, r.fecha_limite, " +
            "       r.entrega_tardia, rm.mes " +
            "FROM reporte r " +
            "LEFT JOIN reporte_mensual rm ON rm.id_reporte_mensual = r.id_reporte " +
            "WHERE r.id_practicante = ? ORDER BY r.fecha_entrega DESC";

    private static final String SQL_SELECT_BY_PROFESSOR =
            SQL_SELECT_COLUMNS +
            "FROM reporte WHERE id_profesor = ? ORDER BY fecha_entrega DESC";

    private static final String SQL_SELECT_BY_INTERN_AND_PROFESSOR =
            SQL_SELECT_COLUMNS +
            "FROM reporte WHERE id_practicante = ? AND id_profesor = ? " +
            "ORDER BY fecha_entrega DESC";

    private static final String SQL_SELECT_BY_INTERN_AND_PROJECT =
            SQL_SELECT_COLUMNS +
            "FROM reporte WHERE id_practicante = ? AND id_proyecto = ? " +
            "ORDER BY fecha_entrega DESC";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE reporte " +
            "SET estado = ?, observaciones_profesor = ?, fecha_revision = ? " +
            "WHERE id_reporte = ?";

    private static final String SQL_UPDATE_GRADE =
            "UPDATE reporte " +
            "SET calificacion = ?, fecha_evaluacion = ? " +
            "WHERE id_reporte = ?";

    private static final String SQL_AVERAGE_BY_INTERN =
            "SELECT AVG(calificacion) AS average_grade " +
            "FROM reporte WHERE id_practicante = ? AND calificacion IS NOT NULL";

    private static final String SQL_UPDATE_DOCUMENT_PATH =
            "UPDATE reporte SET ruta_documento = ? WHERE id_reporte = ?";

    private static final String SQL_UPDATE_SIGNED_PATH =
            "UPDATE reporte SET ruta_documento_firmado = ?, estado = 'En revision' " +
            "WHERE id_reporte = ?";

    private static final String SQL_MARK_LATE_DELIVERY =
            "UPDATE reporte SET entrega_tardia = 1 WHERE id_reporte = ?";

    private static final String SQL_SUM_APPROVED_HOURS =
            "SELECT COALESCE(SUM(rm.horas_reportadas), 0) AS total_horas " +
            "FROM reporte r " +
            "JOIN reporte_mensual rm ON rm.id_reporte_mensual = r.id_reporte " +
            "WHERE r.id_practicante = ? " +
            "  AND (r.estado = 'Evaluado' OR r.estado = 'Aprobado')";

    private static final String SQL_EXISTS_MONTHLY =
            "SELECT COUNT(*) AS total " +
            "FROM reporte r " +
            "JOIN reporte_mensual rm ON rm.id_reporte_mensual = r.id_reporte " +
            "WHERE r.id_practicante = ? AND rm.mes = ? AND rm.anio = ?";

    private static final String SQL_EXISTS_PARTIAL =
            "SELECT COUNT(*) AS total FROM reporte " +
            "WHERE id_practicante = ? AND id_proyecto = ? AND tipo_reporte = 'Parcial'";

    private static final String SQL_EXISTS_FINAL =
            "SELECT COUNT(*) AS total FROM reporte " +
            "WHERE id_practicante = ? AND id_proyecto = ? AND tipo_reporte = 'Final'";

    @Override
    public int save(Report report) throws ServiceException, ValidationException {
        if (report.getIdIntern() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: "
                    + report.getIdIntern());
        }

        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt (1, report.getIdIntern());
            statement.setInt (2, report.getIdProject());
            statement.setInt (3, report.getIdProfessor());
            statement.setString(4, report.getReportType());
            statement.setString(5, report.getPeriod());
            statement.setString(6, report.getDocumentPath());
            statement.setString(7, report.getStatus());
            statement.setInt(8, report.getReportedHours());
            statement.setDate(9, new java.sql.Date(report.getSubmissionDate().getTime()));
            statement.setDate(10, report.getDeadline() != null
                    ? java.sql.Date.valueOf(report.getDeadline()) : null);
            statement.setBoolean(11, report.isLateDelivery());

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    report.setIdReport(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving report for intern {0}: {1}",
                    new Object[]{report.getIdIntern(), sqlException.getMessage()});
            throw new ServiceException("Error al guardar el reporte.", sqlException);
        }

        return rowsAffected;
    }

    @Override
    public Report getById(int idReport) throws ServiceException, ValidationException {
        if (idReport <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + idReport);
        }

        Report report = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idReport);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    report = mapResultSetToReport(resultSet);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving report with ID {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new ServiceException("Error al recuperar el reporte con ID " + idReport,
                    sqlException);
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
                reports.add(mapResultSetToReport(resultSet));
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all reports: {0}",
                    sqlException.getMessage());
            throw new ServiceException("Error al recuperar la lista de reportes.", sqlException);
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
                reports.add(mapResultSetToReport(resultSet));
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving pending reports: {0}",
                    sqlException.getMessage());
            throw new ServiceException("Error al recuperar los reportes pendientes.", sqlException);
        }

        return reports;
    }

    @Override
    public List<Report> getByIdIntern(int idIntern) throws ServiceException, ValidationException {
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }

        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_INTERN)) {

            statement.setInt(1, idIntern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reports.add(mapResultSetToReport(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving reports for intern {0}: {1}",
                    new Object[]{idIntern, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al recuperar los reportes del practicante.", sqlException);
        }

        return reports;
    }

    public List<Report> getByIdInternWithMonth(int idIntern)
            throws ServiceException, ValidationException {
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }

        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_SELECT_BY_INTERN_WITH_MONTH)) {

            statement.setInt(1, idIntern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reports.add(mapResultSetToReportWithMonth(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving reports with month for intern {0}: {1}",
                    new Object[]{idIntern, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al recuperar los reportes del practicante.", sqlException);
        }

        return reports;
    }

    private Report mapResultSetToReportWithMonth(ResultSet resultSet) throws SQLException {
        Report report = mapResultSetToReport(resultSet);
        String mes = resultSet.getString("mes");
        report.setMonthName(mes);
        return report;
    }

    @Override
    public List<Report> getByIdProfessor(int idProfessor)
            throws ServiceException, ValidationException {
        if (idProfessor <= 0) {
            throw new ValidationException(
                    "El ID del profesor debe ser mayor a cero. ID recibido: " + idProfessor);
        }

        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_PROFESSOR)) {

            statement.setInt(1, idProfessor);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reports.add(mapResultSetToReport(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving reports for professor {0}: {1}",
                    new Object[]{idProfessor, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al recuperar los reportes del profesor.", sqlException);
        }

        return reports;
    }

    public List<Report> getByInternAndProfessor(int internId, int professorId)
            throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }
        if (professorId <= 0) {
            throw new ValidationException(
                    "El ID del profesor debe ser mayor a cero. ID recibido: " + professorId);
        }

        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_SELECT_BY_INTERN_AND_PROFESSOR)) {

            statement.setInt(1, internId);
            statement.setInt(2, professorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reports.add(mapResultSetToReport(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error retrieving reports for intern {0} for professor {1}: {2}",
                    new Object[]{internId, professorId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al recuperar los reportes del practicante.", sqlException);
        }

        return reports;
    }

    public List<Report> getByInternAndProject(int internId, int projectId)
            throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }
        if (projectId <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + projectId);
        }

        List<Report> reports = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_SELECT_BY_INTERN_AND_PROJECT)) {

            statement.setInt(1, internId);
            statement.setInt(2, projectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reports.add(mapResultSetToReport(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error retrieving reports for intern {0} in project {1}: {2}",
                    new Object[]{internId, projectId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al recuperar los reportes del practicante.", sqlException);
        }

        return reports;
    }

    @Override
    public boolean updateStatus(int idReport, ReportStatusUpdate update)
            throws ServiceException, ValidationException {
        if (idReport <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + idReport);
        }
        if (update == null || update.getStatus() == null || update.getStatus().isBlank()) {
            throw new ValidationException("El estado del reporte no puede estar vacío.");
        }

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_STATUS)) {

            statement.setString(1, update.getStatus());
            statement.setString(2, update.getProfessorObservations());
            statement.setDate (3, update.getReviewDate());
            statement.setInt (4, idReport);

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error updating report status {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new ServiceException("Error al actualizar el estado del reporte.", sqlException);
        }

        return isUpdated;
    }

    public boolean updateGrade(int idReport, Double grade, java.time.LocalDate evaluationDate)
            throws ServiceException, ValidationException {
        if (idReport <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + idReport);
        }
        if (grade == null || grade < 0 || grade > 10) {
            throw new ValidationException(
                    "La calificación debe estar entre 0 y 10. Valor recibido: " + grade);
        }

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_GRADE)) {

            statement.setBigDecimal(1, java.math.BigDecimal.valueOf(grade));
            if (evaluationDate != null) {
                statement.setDate(2, java.sql.Date.valueOf(evaluationDate));
            } else {
                statement.setDate(2, null);
            }
            statement.setInt(3, idReport);

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving report grade {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new ServiceException("Error al guardar la calificación del reporte.", sqlException);
        }

        return isUpdated;
    }

    public Double getAveragePracticeGrade(int internId)
            throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }

        Double averageGrade = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_AVERAGE_BY_INTERN)) {

            statement.setInt(1, internId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double value = resultSet.getDouble("average_grade");
                    if (!resultSet.wasNull()) {
                        averageGrade = value;
                    }
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error calculating average grade for intern {0}: {1}",
                    new Object[]{internId, sqlException.getMessage()});
            throw new ServiceException("Error al calcular la calificación de la práctica.",
                    sqlException);
        }

        return averageGrade;
    }

    @Override
    public boolean updateSignedDocumentPath(int idReport, String signedPath)
            throws ServiceException, ValidationException {
        if (idReport <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + idReport);
        }
        if (signedPath == null || signedPath.isBlank()) {
            throw new ValidationException("La ruta del documento firmado no puede estar vacía.");
        }

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_SIGNED_PATH)) {

            statement.setString(1, signedPath);
            statement.setInt (2, idReport);

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error updating signed path for report {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al actualizar la ruta del documento firmado.", sqlException);
        }

        return isUpdated;
    }

    public boolean markLateDelivery(int idReport) throws ServiceException, ValidationException {
        if (idReport <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + idReport);
        }

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_MARK_LATE_DELIVERY)) {

            statement.setInt(1, idReport);

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error marking late submission for report {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new ServiceException("Error al registrar la entrega tardía.", sqlException);
        }

        return isUpdated;
    }

    public boolean updateDocumentPath(int idReport, String documentPath)
            throws ServiceException, ValidationException {
        if (idReport <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + idReport);
        }
        if (documentPath == null || documentPath.isBlank()) {
            throw new ValidationException("La ruta del documento no puede estar vacía.");
        }

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_DOCUMENT_PATH)) {

            statement.setString(1, documentPath);
            statement.setInt (2, idReport);

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error updating report document path {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al actualizar la ruta del documento del reporte.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public int getTotalApprovedHoursByIntern(int idIntern)
            throws ServiceException, ValidationException {
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }

        int totalHours = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SUM_APPROVED_HOURS)) {

            statement.setInt(1, idIntern);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    totalHours = resultSet.getInt("total_horas");
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error calculating approved hours for intern {0}: {1}",
                    new Object[]{idIntern, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al calcular las horas aprobadas del practicante.", sqlException);
        }

        return totalHours;
    }

    @Override
    public boolean existsMonthlyByInternAndPeriod(int idIntern, String month, int year)
            throws ServiceException {
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_EXISTS_MONTHLY)) {

            statement.setInt (1, idIntern);
            statement.setString(2, month);
            statement.setInt (3, year);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total") > 0;
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error verifying monthly report existence: {0}",
                    sqlException.getMessage());
            throw new ServiceException(
                    "Error al verificar reporte mensual existente.", sqlException);
        }

        return false;
    }

    @Override
    public boolean existsPartialByInternAndProject(int idIntern, int idProject)
            throws ServiceException {
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_EXISTS_PARTIAL)) {

            statement.setInt(1, idIntern);
            statement.setInt(2, idProject);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total") > 0;
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error verifying partial report existence: {0}",
                    sqlException.getMessage());
            throw new ServiceException(
                    "Error al verificar reporte parcial existente.", sqlException);
        }

        return false;
    }

    @Override
    public boolean existsFinalByInternAndProject(int idIntern, int idProject)
            throws ServiceException {
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_EXISTS_FINAL)) {

            statement.setInt(1, idIntern);
            statement.setInt(2, idProject);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total") > 0;
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error verifying final report existence: {0}",
                    sqlException.getMessage());
            throw new ServiceException(
                    "Error al verificar reporte final existente.", sqlException);
        }

        return false;
    }

    protected Report mapResultSetToReport(ResultSet resultSet) throws SQLException {
        Report report = new Report();
        report.setIdReport (resultSet.getInt ("id_reporte"));
        report.setIdIntern (resultSet.getInt ("id_practicante"));
        report.setIdProject (resultSet.getInt ("id_proyecto"));
        report.setIdProfessor(resultSet.getInt ("id_profesor"));
        report.setReportType (resultSet.getString("tipo_reporte"));
        report.setPeriod (resultSet.getString("periodo"));
        report.setDocumentPath(resultSet.getString("ruta_documento"));
        report.setSignedDocumentPath(resultSet.getString("ruta_documento_firmado"));
        report.setStatus (resultSet.getString("estado"));
        report.setReportedHours(resultSet.getInt ("horas_reportadas"));
        report.setProfessorObservations(resultSet.getString("observaciones_profesor"));
        report.setSubmissionDate(resultSet.getDate("fecha_entrega"));

        java.sql.Date reviewDate = resultSet.getDate("fecha_revision");
        if (reviewDate != null) {
            report.setReviewDate(reviewDate.toLocalDate());
        }

        java.math.BigDecimal grade = resultSet.getBigDecimal("calificacion");
        if (grade != null) {
            report.setGrade(grade.doubleValue());
        }

        java.sql.Date evaluationDate = resultSet.getDate("fecha_evaluacion");
        if (evaluationDate != null) {
            report.setEvaluationDate(evaluationDate.toLocalDate());
        }

        java.sql.Date deadline = resultSet.getDate("fecha_limite");
        if (deadline != null) {
            report.setDeadline(deadline.toLocalDate());
        }
        report.setLateDelivery(resultSet.getBoolean("entrega_tardia"));

        return report;
    }

    public boolean deleteByInternAndProject(int internId, int projectId)
            throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }
        if (projectId <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + projectId);
        }

        String sql = "DELETE FROM reporte WHERE id_practicante = ? AND id_proyecto = ?";
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, internId);
            statement.setInt(2, projectId);
            rowsAffected = statement.executeUpdate();

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error deleting reports for intern {0} in project {1}: {2}",
                    new Object[]{internId, projectId, sqlException.getMessage()});
            throw new ServiceException(
                    "Error al eliminar reportes del practicante.", sqlException);
        }

        return rowsAffected >= 0;
    }
}
