import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class CoordinatorTestDataBuilder {

    private static final String INSERT_SQL = "INSERT INTO coordinador (id_usuario) VALUES (?)";

    private int idUser;

    public CoordinatorTestDataBuilder withUserId(int idUser) {
        this.idUser = idUser;
        return this;
    }

    public void persist(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setInt(1, idUser);
            statement.executeUpdate();
        }
    }
}
