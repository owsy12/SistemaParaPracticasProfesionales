import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public final class PracticeTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO practica (nrc, id_practicante, fecha_inicio, fecha_fin, estado) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final LocalDate DEFAULT_START = LocalDate.of(2025, 1, 15);
    private static final LocalDate DEFAULT_END = LocalDate.of(2025, 7, 15);

    private String nrc = TestConstants.DEFAULT_NRC;
    private int idIntern;
    private LocalDate startDate = DEFAULT_START;
    private LocalDate endDate = DEFAULT_END;
    private String status = TestConstants.STATUS_PRACTICE_ACTIVE;

    public PracticeTestDataBuilder withNrc(String nrc) {
        this.nrc = nrc;
        return this;
    }

    public PracticeTestDataBuilder withInternId(int idIntern) {
        this.idIntern = idIntern;
        return this;
    }

    public PracticeTestDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public int persist(Connection connection) throws SQLException {
        int generatedId = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nrc);
            statement.setInt(2, idIntern);
            statement.setDate(3, java.sql.Date.valueOf(startDate));
            statement.setDate(4, java.sql.Date.valueOf(endDate));
            statement.setString(5, status);
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
