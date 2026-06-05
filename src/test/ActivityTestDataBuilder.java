import Logic.Exceptions.ServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public final class ActivityTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO actividad (id_proyecto, nombre, descripcion, fecha_inicio, fecha_fin, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String DEFAULT_NAME = "Levantamiento de requerimientos";
    private static final String DEFAULT_DESCRIPTION = "Entrevistas iniciales con stakeholders.";
    private static final LocalDate DEFAULT_START = LocalDate.of(2025, 2, 1);
    private static final LocalDate DEFAULT_END = LocalDate.of(2025, 2, 28);

    private int idProject;
    private String name = DEFAULT_NAME;
    private String description = DEFAULT_DESCRIPTION;
    private LocalDate startDate = DEFAULT_START;
    private LocalDate endDate = DEFAULT_END;
    private String status = TestConstants.STATUS_ACTIVITY_ACTIVE;

    public ActivityTestDataBuilder withProjectId(int idProject) {
        this.idProject = idProject;
        return this;
    }

    public ActivityTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ActivityTestDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public int persist(Connection connection) throws ServiceException {
        int generatedId = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idProject);
            statement.setString(2, name);
            statement.setString(3, description);
            statement.setDate(4, java.sql.Date.valueOf(startDate));
            statement.setDate(5, java.sql.Date.valueOf(endDate));
            statement.setString(6, status);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedId = keys.getInt(1);
                }
            }
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to persist activity test data", sqlException);
        }
        return generatedId;
    }
}
