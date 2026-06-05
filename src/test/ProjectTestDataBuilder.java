import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public final class ProjectTestDataBuilder {

    private static final String INSERT_SQL =
            "INSERT INTO proyecto (id_organizacion, id_tecnico, id_profesor, nrc, nombre, " +
                    "descripcion, fecha_inicio, fecha_fin, cupo_maximo, cupo_disponible, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String DEFAULT_NAME = "Sistema de Inventario";
    private static final String DEFAULT_DESCRIPTION = "Sistema web de inventario.";
    private static final LocalDate DEFAULT_START = LocalDate.of(2025, 1, 15);
    private static final LocalDate DEFAULT_END = LocalDate.of(2025, 7, 15);

    private int idOrganization;
    private int idTechnical;
    private int idProfessor;
    private String nrc = TestConstants.DEFAULT_NRC;
    private String name = DEFAULT_NAME;
    private String description = DEFAULT_DESCRIPTION;
    private LocalDate startDate = DEFAULT_START;
    private LocalDate endDate = DEFAULT_END;
    private int maxSlots = TestConstants.DEFAULT_PROJECT_MAX_SLOTS;
    private int availableSlots = TestConstants.DEFAULT_PROJECT_AVAILABLE_SLOTS;
    private String status = TestConstants.STATUS_AVAILABLE;

    public ProjectTestDataBuilder withOrganizationId(int idOrganization) {
        this.idOrganization = idOrganization;
        return this;
    }

    public ProjectTestDataBuilder withTechnicalId(int idTechnical) {
        this.idTechnical = idTechnical;
        return this;
    }

    public ProjectTestDataBuilder withProfessorId(int idProfessor) {
        this.idProfessor = idProfessor;
        return this;
    }

    public ProjectTestDataBuilder withNrc(String nrc) {
        this.nrc = nrc;
        return this;
    }

    public ProjectTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ProjectTestDataBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public ProjectTestDataBuilder withAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
        return this;
    }

    public int persist(Connection connection) throws SQLException {
        int generatedId = 0;
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idOrganization);
            statement.setInt(2, idTechnical);
            statement.setInt(3, idProfessor);
            statement.setString(4, nrc);
            statement.setString(5, name);
            statement.setString(6, description);
            statement.setDate(7, java.sql.Date.valueOf(startDate));
            statement.setDate(8, java.sql.Date.valueOf(endDate));
            statement.setInt(9, maxSlots);
            statement.setInt(10, availableSlots);
            statement.setString(11, status);
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
