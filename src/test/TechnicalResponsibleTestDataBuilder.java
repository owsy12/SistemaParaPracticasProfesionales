import Logic.Exceptions.ServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class TechnicalResponsibleTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO tecnico_responsable (id_organizacion, nombre, apellido_paterno, " +
                    "apellido_materno, correo_responsable, cargo) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String DEFAULT_FIRST_NAME = "Luis";
    private static final String DEFAULT_LAST_NAME = "Pérez";
    private static final String DEFAULT_SECOND_LAST_NAME = "Vega";
    private static final String DEFAULT_EMAIL = "luis.perez@techcorp-test.mx";
    private static final String DEFAULT_POSITION = "Gerente de TI";

    private int idOrganization;
    private String firstName = DEFAULT_FIRST_NAME;
    private String lastName = DEFAULT_LAST_NAME;
    private String secondLastName = DEFAULT_SECOND_LAST_NAME;
    private String email = DEFAULT_EMAIL;
    private String position = DEFAULT_POSITION;

    public TechnicalResponsibleTestDataBuilder withOrganizationId(int idOrganization) {
        this.idOrganization = idOrganization;
        return this;
    }

    public TechnicalResponsibleTestDataBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public TechnicalResponsibleTestDataBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public TechnicalResponsibleTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public TechnicalResponsibleTestDataBuilder withPosition(String position) {
        this.position = position;
        return this;
    }

    public int persist(Connection connection) throws ServiceException {
        int generatedId = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idOrganization);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, secondLastName);
            statement.setString(5, email);
            statement.setString(6, position);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedId = keys.getInt(1);
                }
            }
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to persist technical supervisor test data", sqlException);
        }
        return generatedId;
    }
}
