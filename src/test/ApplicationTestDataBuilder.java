import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class ApplicationTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO solicitud (id_practicante, estado) VALUES (?, ?)";

    private int idIntern;
    private String status = TestConstants.STATUS_PENDING;

    public ApplicationTestDataBuilder withInternId(int idIntern) {
        this.idIntern = idIntern;
        return this;
    }

    public ApplicationTestDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public int persist(Connection connection) throws SQLException {
        int generatedId = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idIntern);
            statement.setString(2, status);
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
