import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class ProjectApplicationTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO solicitud_proyecto (id_solicitud, id_proyecto, orden_preferencia) " +
                    "VALUES (?, ?, ?)";

    private int idApplication;
    private int idProject;
    private int preferenceOrder = TestConstants.DEFAULT_PREFERENCE_ORDER;

    public ProjectApplicationTestDataBuilder withApplicationId(int idApplication) {
        this.idApplication = idApplication;
        return this;
    }

    public ProjectApplicationTestDataBuilder withProjectId(int idProject) {
        this.idProject = idProject;
        return this;
    }

    public ProjectApplicationTestDataBuilder withPreferenceOrder(int preferenceOrder) {
        this.preferenceOrder = preferenceOrder;
        return this;
    }

    public int persist(Connection connection) throws SQLException {
        int generatedId = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idApplication);
            statement.setInt(2, idProject);
            statement.setInt(3, preferenceOrder);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedId = keys.getInt(1);
                }
            }
        }
        return generatedId;
    }
}
