import DataAccess.DataBaseConnection;
import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.TechnicalResponsible;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
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

    private TechnicalResponsible buildSupervisor(int idOrganization) {
        TechnicalResponsible supervisor = new TechnicalResponsible();
        supervisor.setIdOrganization(idOrganization);
        supervisor.setName(TECH_FIRST_NAME);
        supervisor.setLastName(TECH_LAST_NAME);
        supervisor.setSecondLastName(TECH_LAST_NAME);
        supervisor.setEmail(TECH_EMAIL);
        supervisor.setPosition(TECH_POSITION);
        return supervisor;
    }

    @Test
    void testSaveValidSupervisorReturnsTrue() throws ServiceException, ValidationException {
        int idOrganization = persistOrganization();
        boolean result = dao.saveTechnicalResponsible(buildSupervisor(idOrganization));
        assertTrue(result);
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        int idTechnical = persistSupervisorViaBuilders();
        TechnicalResponsible retrieved = dao.findById(idTechnical);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findById(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        TechnicalResponsible retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<TechnicalResponsible> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterPersistReturnsOneElement() throws ServiceException, ValidationException {
        persistSupervisorViaBuilders();
        List<TechnicalResponsible> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testFindByOrganizationReturnsOneElement() throws ServiceException, ValidationException {
        int idOrganization = persistOrganization();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            new TechnicalResponsibleTestDataBuilder()
                    .withOrganizationId(idOrganization)
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        List<TechnicalResponsible> byOrg = dao.findByOrganization(idOrganization);
        assertEquals(TestConstants.SINGLE_RESULT, byOrg.size());
    }

    @Test
    void testFindByOrganizationWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findByOrganization(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testUpdateSupervisorReturnsTrue() throws ServiceException, ValidationException {
        int idTechnical = persistSupervisorViaBuilders();
        TechnicalResponsible supervisor = dao.findById(idTechnical);
        supervisor.setPosition(UPDATED_TECH_POSITION);
        boolean result = dao.update(supervisor);
        assertTrue(result);
    }

    @Test
    void testDeleteSupervisorWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.delete(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testDeleteSupervisorReturnsTrue() throws ServiceException, ValidationException {
        int idTechnical = persistSupervisorViaBuilders();
        boolean result = dao.delete(idTechnical);
        assertTrue(result);
    }

    private int persistOrganization() throws ServiceException, ValidationException {
        int idOrganization;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idOrganization = new OrganizationTestDataBuilder().persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idOrganization;
    }

    private int persistSupervisorViaBuilders() throws ServiceException, ValidationException {
        int idTechnical;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            int idOrganization = new OrganizationTestDataBuilder().persist(connection);
            idTechnical = new TechnicalResponsibleTestDataBuilder()
                    .withOrganizationId(idOrganization)
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idTechnical;
    }
}
