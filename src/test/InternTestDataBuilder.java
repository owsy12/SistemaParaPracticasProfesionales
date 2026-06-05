import Logic.Exceptions.ServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class InternTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO practicante (id_usuario, creditos) VALUES (?, ?)";

    private int idUser;
    private int credits = TestConstants.DEFAULT_INTERN_CREDITS;

    public InternTestDataBuilder withUserId(int idUser) {
        this.idUser = idUser;
        return this;
    }

    public InternTestDataBuilder withCredits(int credits) {
        this.credits = credits;
        return this;
    }

    public void persist(Connection connection) throws ServiceException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setInt(1, idUser);
            statement.setInt(2, credits);
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to persist intern test data", sqlException);
        }
    }
}
