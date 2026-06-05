import Logic.Exceptions.ServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class EducationalExperienceTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO experiencia_educativa (nrc, nombre, id_profesor) VALUES (?, ?, ?)";

    private static final String DEFAULT_NAME = "Prácticas Profesionales";

    private String nrc = TestConstants.DEFAULT_NRC;
    private String name = DEFAULT_NAME;
    private int idProfessor;

    public EducationalExperienceTestDataBuilder withNrc(String nrc) {
        this.nrc = nrc;
        return this;
    }

    public EducationalExperienceTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public EducationalExperienceTestDataBuilder withProfessorId(int idProfessor) {
        this.idProfessor = idProfessor;
        return this;
    }

    public void persist(Connection connection) throws ServiceException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, nrc);
            statement.setString(2, name);
            statement.setInt(3, idProfessor);
            statement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to persist educational experience test data", sqlException);
        }
    }
}
