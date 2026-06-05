import Logic.Exceptions.ServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class OrganizationTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO organizacion_vinculada (nombre_organizacion, correo_organizacion, " +
                    "direccion, sector, estado) VALUES (?, ?, ?, ?, ?)";

    private static final String DEFAULT_NAME = "TechCorp Test";
    private static final String DEFAULT_EMAIL = "contacto@techcorp-test.mx";
    private static final String DEFAULT_ADDRESS = "Av. Central 100, Xalapa";
    private static final String DEFAULT_SECTOR = "Tecnología";

    private String name = DEFAULT_NAME;
    private String email = DEFAULT_EMAIL;
    private String address = DEFAULT_ADDRESS;
    private String sector = DEFAULT_SECTOR;
    private String status = TestConstants.STATUS_ACTIVE_ORG;

    public OrganizationTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public OrganizationTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public OrganizationTestDataBuilder withAddress(String address) {
        this.address = address;
        return this;
    }

    public OrganizationTestDataBuilder withSector(String sector) {
        this.sector = sector;
        return this;
    }

    public OrganizationTestDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public int persist(Connection connection) throws ServiceException {
        int generatedId = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, address);
            statement.setString(4, sector);
            statement.setString(5, status);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    generatedId = keys.getInt(1);
                }
            }
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to persist organization test data", sqlException);
        }
        return generatedId;
    }
}
