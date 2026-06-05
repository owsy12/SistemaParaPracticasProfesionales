import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class AdministratorTestDataBuilder {

    private static final String INSERT_SQL = "INSERT INTO administrador (id_usuario) VALUES (?)";

    private int idUser;

    public AdministratorTestDataBuilder withUserId(int idUser) {
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
