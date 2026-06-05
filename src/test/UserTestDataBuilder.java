import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class UserTestDataBuilder {

    private static final String INSERT_USER_SQL =
            "INSERT INTO usuario (matricula, nombre, apellido_paterno, apellido_materno, " +
                    "contrasenia, correo) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String INSERT_ROLE_SQL =
            "INSERT INTO usuario_rol (id_usuario, rol, estado) VALUES (?, ?, ?)";

    private static final String DEFAULT_REGISTRATION_NUMBER = "S00000001";
    private static final String DEFAULT_FIRST_NAME = "Test";
    private static final String DEFAULT_LAST_NAME = "User";
    private static final String DEFAULT_SECOND_LAST_NAME = "Example";
    private static final String DEFAULT_EMAIL = "test.user@uv.mx";

    private String registrationNumber = DEFAULT_REGISTRATION_NUMBER;
    private String firstName = DEFAULT_FIRST_NAME;
    private String lastName = DEFAULT_LAST_NAME;
    private String secondLastName = DEFAULT_SECOND_LAST_NAME;
    private String email = DEFAULT_EMAIL;
    private String passwordHash = TestConstants.DEFAULT_PASSWORD_HASH;
    private String role;
    private String roleStatus = TestConstants.STATUS_ACTIVE_USER;

    public UserTestDataBuilder withRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        return this;
    }

    public UserTestDataBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserTestDataBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestDataBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public UserTestDataBuilder withRole(String role) {
        this.role = role;
        return this;
    }

    public UserTestDataBuilder withRoleStatus(String roleStatus) {
        this.roleStatus = roleStatus;
        return this;
    }

    public int persist(Connection connection) throws SQLException {
        int generatedId = insertUser(connection);
        if (role != null) {
            insertRole(connection, generatedId);
        }
        return generatedId;
    }

    private int insertUser(Connection connection) throws SQLException {
        int generatedId = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, registrationNumber);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, secondLastName);
            statement.setString(5, passwordHash);
            statement.setString(6, email);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedId = keys.getInt(1);
                }
            }
        }
        return generatedId;
    }

    private void insertRole(Connection connection, int idUser) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ROLE_SQL)) {
            statement.setInt(1, idUser);
            statement.setString(2, role);
            statement.setString(3, roleStatus);
            statement.executeUpdate();
        }
    }
}
