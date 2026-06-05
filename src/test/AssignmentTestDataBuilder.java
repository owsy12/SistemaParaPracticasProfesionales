import Logic.Exceptions.ServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class AssignmentTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO asignacion (id_practicante, id_proyecto, id_solicitud, estado) " +
                    "VALUES (?, ?, ?, ?)";

    private int idIntern;
    private int idProject;
    private int idApplication;
    private String status = TestConstants.STATUS_ASSIGNMENT_ACTIVE;

    public AssignmentTestDataBuilder withInternId(int idIntern) {
        this.idIntern = idIntern;
        return this;
    }

    public AssignmentTestDataBuilder withProjectId(int idProject) {
        this.idProject = idProject;
        return this;
    }

    public AssignmentTestDataBuilder withApplicationId(int idApplication) {
        this.idApplication = idApplication;
        return this;
    }

    public AssignmentTestDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public int persist(Connection connection) throws ServiceException {
        int generatedId = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idIntern);
            statement.setInt(2, idProject);
            statement.setInt(3, idApplication);
            statement.setString(4, status);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedId = keys.getInt(1);
                }
            }
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to persist assignment test data", sqlException);
        }
        return generatedId;
    }
}
