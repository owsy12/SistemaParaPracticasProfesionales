import DataAccess.DataBaseConnection;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TechnicalResponsibleDAOTest extends BaseDAOTest {

    private static final String TECH_FIRST_NAME = "Sandra";
    private static final String TECH_LAST_NAME = "Núñez";
    private static final String TECH_EMAIL = "sandra.nunez@techcorp-test.mx";
    private static final String TECH_POSITION = "Líder de proyecto";
    private static final String UPDATED_TECH_POSITION = "Directora de proyecto";

    private final TechnicalResponsibleDAO dao = new TechnicalResponsibleDAO();

    private TechnicalSupervisor buildSupervisor(int idOrganization) {
        TechnicalSupervisor supervisor = new TechnicalSupervisor();
        supervisor.setIdOrganization(idOrganization);
        supervisor.setName(TECH_FIRST_NAME);
        supervisor.setLastName(TECH_LAST_NAME);
        supervisor.setSecondLastName(TECH_LAST_NAME);
        supervisor.seteMail(TECH_EMAIL);
        supervisor.setPosition(TECH_POSITION);
        return supervisor;
    }

    @Test
    void testSaveValidSupervisorReturnsTrue() throws Exception {
        int idOrganization = persistOrganization();
        boolean result = dao.saveTechnicalResponsible(buildSupervisor(idOrganization));
        assertTrue(result);
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws Exception {
        int idTechnical = persistSupervisorViaBuilders();
        TechnicalSupervisor retrieved = dao.findById(idTechnical);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        TechnicalSupervisor retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws Exception {
        List<TechnicalSupervisor> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterPersistReturnsOneElement() throws Exception {
        persistSupervisorViaBuilders();
        List<TechnicalSupervisor> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testFindByOrganizationReturnsOneElement() throws Exception {
        int idOrganization = persistOrganization();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            new TechnicalSupervisorTestDataBuilder()
                    .withOrganizationId(idOrganization)
                    .persist(connection);
        }
        List<TechnicalSupervisor> byOrg = dao.findByOrganization(idOrganization);
        assertEquals(TestConstants.SINGLE_RESULT, byOrg.size());
    }

    @Test
    void testFindByOrganizationWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findByOrganization(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testUpdateSupervisorReturnsTrue() throws Exception {
        int idTechnical = persistSupervisorViaBuilders();
        TechnicalSupervisor supervisor = dao.findById(idTechnical);
        supervisor.setPosition(UPDATED_TECH_POSITION);
        boolean result = dao.update(supervisor);
        assertTrue(result);
    }

    @Test
    void testDeleteSupervisorWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.delete(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testDeleteSupervisorReturnsTrue() throws Exception {
        int idTechnical = persistSupervisorViaBuilders();
        boolean result = dao.delete(idTechnical);
        assertTrue(result);
    }

    private int persistOrganization() throws Exception {
        int idOrganization;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idOrganization = new OrganizationTestDataBuilder().persist(connection);
        }
        return idOrganization;
    }

    private int persistSupervisorViaBuilders() throws Exception {
        int idTechnical;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            int idOrganization = new OrganizationTestDataBuilder().persist(connection);
            idTechnical = new TechnicalSupervisorTestDataBuilder()
                    .withOrganizationId(idOrganization)
                    .persist(connection);
        }
        return idTechnical;
    }
}
