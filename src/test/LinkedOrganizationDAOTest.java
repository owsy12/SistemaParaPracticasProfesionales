import DataAccess.DataBaseConnection;
import Logic.DAO.LinkedOrganizationDAO;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkedOrganizationDAOTest extends BaseDAOTest {

    private static final String NEW_ORG_NAME = "InnovaSoft Test";
    private static final String NEW_ORG_EMAIL = "contacto@innovasoft-test.mx";
    private static final String NEW_ORG_ADDRESS = "Calle Reforma 123";
    private static final String NEW_ORG_SECTOR = "Software";
    private static final String UPDATED_ORG_NAME = "InnovaSoft Test Avanzado";
    private static final String SECONDARY_ORG_NAME = "DataLabs Test";
    private static final String SECONDARY_ORG_EMAIL = "contacto@datalabs-test.mx";

    private final LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();

    private LinkedOrganization buildOrganization(String name, String email) {
        LinkedOrganization organization = new LinkedOrganization();
        organization.setName(name);
        organization.setEmail(email);
        organization.setAddress(NEW_ORG_ADDRESS);
        organization.setSector(NEW_ORG_SECTOR);
        return organization;
    }

    @Test
    void testSaveValidOrganizationReturnsTrue() throws ServiceException, ValidationException {
        boolean result = linkedOrganizationDAO.saveLinkedOrganization(buildOrganization(NEW_ORG_NAME, NEW_ORG_EMAIL));
        assertTrue(result);
    }

    @Test
    void testSaveDuplicateNameThrowsDuplicateEntryException() throws ServiceException, ValidationException {
        linkedOrganizationDAO.saveLinkedOrganization(buildOrganization(NEW_ORG_NAME, NEW_ORG_EMAIL));
        assertThrows(DuplicateEntryException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                linkedOrganizationDAO.saveLinkedOrganization(buildOrganization(NEW_ORG_NAME, SECONDARY_ORG_EMAIL));
            }
        });
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        int idOrganization = persistOrganizationViaBuilder();
        LinkedOrganization retrieved = linkedOrganizationDAO.findById(idOrganization);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                linkedOrganizationDAO.findById(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                linkedOrganizationDAO.findById(TestConstants.INVALID_ID_NEGATIVE);
            }
        });
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        LinkedOrganization retrieved = linkedOrganizationDAO.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<LinkedOrganization> linkedOrganizationList = linkedOrganizationDAO.findAll();
        assertTrue(linkedOrganizationList.isEmpty());
    }

    @Test
    void testFindAllAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        persistOrganizationViaBuilder();
        List<LinkedOrganization> linkedOrganizationList = linkedOrganizationDAO.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, linkedOrganizationList.size());
    }

    @Test
    void testFindAllActiveExcludesInactiveOrganizations() throws ServiceException, ValidationException {
        int activeId = persistOrganizationViaBuilder();
        int inactiveId = persistInactiveOrganizationViaBuilder();
        List<LinkedOrganization> activeList = linkedOrganizationDAO.findAllActive();
        assertEquals(TestConstants.SINGLE_RESULT, activeList.size());
        assertEquals(activeId, activeList.get(0).getIdLinkedOrganization());
        assertFalse(activeId == inactiveId);
    }

    @Test
    void testUpdateOrganizationReturnsTrue() throws ServiceException, ValidationException {
        int idOrganization = persistOrganizationViaBuilder();
        LinkedOrganization organization = linkedOrganizationDAO.findById(idOrganization);
        organization.setName(UPDATED_ORG_NAME);
        boolean result = linkedOrganizationDAO.update(organization);
        assertTrue(result);
    }

    @Test
    void testUpdateOrganizationPersistsNewName() throws ServiceException, ValidationException {
        int idOrganization = persistOrganizationViaBuilder();
        LinkedOrganization organization = linkedOrganizationDAO.findById(idOrganization);
        organization.setName(UPDATED_ORG_NAME);
        linkedOrganizationDAO.update(organization);
        LinkedOrganization retrieved = linkedOrganizationDAO.findById(idOrganization);
        assertEquals(UPDATED_ORG_NAME, retrieved.getName());
    }

    @Test
    void testUpdateOrganizationWithZeroIdThrowsValidationException() {
        LinkedOrganization organization = buildOrganization(NEW_ORG_NAME, NEW_ORG_EMAIL);
        organization.setIdLinkedOrganization(TestConstants.INVALID_ID_ZERO);
        organization.setStatus(TestConstants.STATUS_ACTIVE_ORG);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                linkedOrganizationDAO.update(organization);
            }
        });
    }

    @Test
    void testDeactivateOrganizationReturnsTrue() throws ServiceException, ValidationException {
        int idOrganization = persistOrganizationViaBuilder();
        boolean result = linkedOrganizationDAO.deactivateLinkedOrganization(idOrganization);
        assertTrue(result);
    }

    @Test
    void testDeactivateOrganizationPersistsInactiveStatus() throws ServiceException, ValidationException {
        int idOrganization = persistOrganizationViaBuilder();
        linkedOrganizationDAO.deactivateLinkedOrganization(idOrganization);
        LinkedOrganization retrieved = linkedOrganizationDAO.findById(idOrganization);
        assertEquals(TestConstants.STATUS_INACTIVE_ORG, retrieved.getStatus());
    }

    @Test
    void testDeactivateOrganizationWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                linkedOrganizationDAO.deactivateLinkedOrganization(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testHasAssociatedProjectsReturnsTrueWhenProjectsExist() throws ServiceException, ValidationException {
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            boolean hasProjects = linkedOrganizationDAO.hasAssociatedProjects(scene.getOrganizationId());
            assertTrue(hasProjects);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
    }

    @Test
    void testHasAssociatedProjectsReturnsFalseWhenNoProjects() throws ServiceException, ValidationException {
        int idOrganization = persistOrganizationViaBuilder();
        boolean hasProjects = linkedOrganizationDAO.hasAssociatedProjects(idOrganization);
        assertFalse(hasProjects);
    }

    @Test
    void testDeleteOrganizationWithAssociatedProjectsThrowsValidationException() throws ServiceException, ValidationException {
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            assertThrows(ValidationException.class, new Executable() {
                @Override
                public void execute() throws Throwable {
                    linkedOrganizationDAO.deleteLinkedOrganization(scene.getOrganizationId());
                }
            });
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
    }

    @Test
    void testDeleteOrganizationWithoutProjectsReturnsTrue() throws ServiceException, ValidationException {
        int idOrganization = persistOrganizationViaBuilder();
        boolean result = linkedOrganizationDAO.deleteLinkedOrganization(idOrganization);
        assertTrue(result);
    }

    @Test
    void testDeleteOrganizationWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                linkedOrganizationDAO.deleteLinkedOrganization(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    private int persistOrganizationViaBuilder() throws ServiceException, ValidationException {
        int idOrganization;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idOrganization = new OrganizationTestDataBuilder()
                    .withName(NEW_ORG_NAME)
                    .withEmail(NEW_ORG_EMAIL)
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idOrganization;
    }

    private int persistInactiveOrganizationViaBuilder() throws ServiceException, ValidationException {
        int idOrganization;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idOrganization = new OrganizationTestDataBuilder()
                    .withName(SECONDARY_ORG_NAME)
                    .withEmail(SECONDARY_ORG_EMAIL)
                    .withStatus(TestConstants.STATUS_INACTIVE_ORG)
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idOrganization;
    }
}
