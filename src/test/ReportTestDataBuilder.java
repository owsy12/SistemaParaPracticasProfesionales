import Logic.Exceptions.ServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class ReportTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO reporte (id_practicante, id_proyecto, id_profesor, tipo_reporte, " +
                    "periodo, ruta_documento, estado, horas_reportadas) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private int idIntern;
    private int idProject;
    private int idProfessor;
    private String reportType = TestConstants.REPORT_TYPE_PARTIAL;
    private String period = TestConstants.DEFAULT_PERIOD;
    private String documentPath = TestConstants.DEFAULT_DOCUMENT_PATH;
    private String status = TestConstants.STATUS_REPORT_PENDING;
    private int reportedHours = TestConstants.DEFAULT_REPORTED_HOURS;

    public ReportTestDataBuilder withInternId(int idIntern) {
        this.idIntern = idIntern;
        return this;
    }

    public ReportTestDataBuilder withProjectId(int idProject) {
        this.idProject = idProject;
        return this;
    }

    public ReportTestDataBuilder withProfessorId(int idProfessor) {
        this.idProfessor = idProfessor;
        return this;
    }

    public ReportTestDataBuilder withReportType(String reportType) {
        this.reportType = reportType;
        return this;
    }

    public ReportTestDataBuilder withPeriod(String period) {
        this.period = period;
        return this;
    }

    public ReportTestDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public ReportTestDataBuilder withReportedHours(int reportedHours) {
        this.reportedHours = reportedHours;
        return this;
    }

    public int persist(Connection connection) throws ServiceException {
        int generatedId = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idIntern);
            statement.setInt(2, idProject);
            statement.setInt(3, idProfessor);
            statement.setString(4, reportType);
            statement.setString(5, period);
            statement.setString(6, documentPath);
            statement.setString(7, status);
            statement.setInt(8, reportedHours);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedId = keys.getInt(1);
                }
            }
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to persist report test data", sqlException);
        }
        return generatedId;
    }
}
