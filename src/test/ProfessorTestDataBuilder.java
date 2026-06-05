import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class ProfessorTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO profesor (id_usuario, academica) VALUES (?, ?)";

    private static final String DEFAULT_ACADEMIC_AREA = "Ingeniería de Software";

    private int idUser;
    private String academicArea = DEFAULT_ACADEMIC_AREA;

    public ProfessorTestDataBuilder withUserId(int idUser) {
        this.idUser = idUser;
        return this;
    }

    public ProfessorTestDataBuilder withAcademicArea(String academicArea) {
        this.academicArea = academicArea;
        return this;
    }

    public void persist(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setInt(1, idUser);
            statement.setString(2, academicArea);
            statement.executeUpdate();
        }
    }
}
