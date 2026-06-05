import DataAccess.DataBaseConnection;
import Logic.DAO.LinkedOrganizationDAO;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
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

    private final LinkedOrganizationDAO dao = new LinkedOrganizationDAO();

    private LinkedOrganization buildOrganization(String name, String email) {
        LinkedOrganization organization = new LinkedOrganization();
        organization.setName(name);
        organization.setEmail(email);
        organization.setAddress(NEW_ORG_ADDRESS);
        organization.setSector(NEW_ORG_SECTOR);
        return organization;
    }

    @Test
    void testSaveValidOrganizationReturnsTrue() throws Exception {
        boolean result = dao.saveLinkedOrganization(buildOrganization(NEW_ORG_NAME, NEW_ORG_EMAIL));
        assertTrue(result);
    }

    @Test
    void testSaveDuplicateNameThrowsDuplicateEntryException() throws Exception {
        dao.saveLinkedOrganization(buildOrganization(NEW_ORG_NAME, NEW_ORG_EMAIL));
        assertThrows(DuplicateEntryException.class, () ->
                dao.saveLinkedOrganization(buildOrganization(NEW_ORG_NAME, SECONDARY_ORG_EMAIL)));
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws Exception {
        int idOrganization = persistOrganizationViaBuilder();
        LinkedOrganization retrieved = dao.findById(idOrganization);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(TestConstants.INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        LinkedOrganization retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws Exception {
        List<LinkedOrganization> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterSaveReturnsOneElement() throws Exception {
        persistOrganizationViaBuilder();
        List<LinkedOrganization> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testFindAllActiveExcludesInactiveOrganizations() throws Exception {
        int activeId = persistOrganizationViaBuilder();
        int inactiveId = persistInactiveOrganizationViaBuilder();
        List<LinkedOrganization> activeList = dao.findAllActive();
        assertEquals(TestConstants.SINGLE_RESULT, activeList.size());
        assertEquals(activeId, activeList.get(0).getIdLinkedOrganization());
        assertFalse(activeId == inactiveId);
    }

    @Test
    void testUpdateOrganizationReturnsTrue() throws Exception {
        int idOrganization = persistOrganizationViaBuilder();
        LinkedOrganization organization = dao.findById(idOrganization);
        organization.setName(UPDATED_ORG_NAME);
        boolean result = dao.update(organization);
        assertTrue(result);
    }

    @Test
    void testUpdateOrganizationPersistsNewName() throws Exception {
        int idOrganization = persistOrganizationViaBuilder();
        LinkedOrganization organization = dao.findById(idOrganization);
        organization.setName(UPDATED_ORG_NAME);
        dao.update(organization);
        LinkedOrganization retrieved = dao.findById(idOrganization);
        assertEquals(UPDATED_ORG_NAME, retrieved.getName());
    }

    @Test
    void testUpdateOrganizationWithZeroIdThrowsValidationException() {
        LinkedOrganization organization = buildOrganization(NEW_ORG_NAME, NEW_ORG_EMAIL);
        organization.setIdLinkedOrganization(TestConstants.INVALID_ID_ZERO);
        organization.setStatus(TestConstants.STATUS_ACTIVE_ORG);
        assertThrows(ValidationException.class, () -> dao.update(organization));
    }

    @Test
    void testDeactivateOrganizationReturnsTrue() throws Exception {
        int idOrganization = persistOrganizationViaBuilder();
        boolean result = dao.deactivateLinkedOrganization(idOrganization);
        assertTrue(result);
    }

    @Test
    void testDeactivateOrganizationPersistsInactiveStatus() throws Exception {
        int idOrganization = persistOrganizationViaBuilder();
        dao.deactivateLinkedOrganization(idOrganization);
        LinkedOrganization retrieved = dao.findById(idOrganization);
        assertEquals(TestConstants.STATUS_INACTIVE_ORG, retrieved.getStatus());
    }

    @Test
    void testDeactivateOrganizationWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.deactivateLinkedOrganization(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testHasAssociatedProjectsReturnsTrueWhenProjectsExist() throws Exception {
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            boolean hasProjects = dao.hasAssociatedProjects(scene.getOrganizationId());
            assertTrue(hasProjects);
        }
    }

    @Test
    void testHasAssociatedProjectsReturnsFalseWhenNoProjects() throws Exception {
        int idOrganization = persistOrganizationViaBuilder();
        boolean hasProjects = dao.hasAssociatedProjects(idOrganization);
        assertFalse(hasProjects);
    }

    @Test
    void testDeleteOrganizationWithAssociatedProjectsThrowsValidationException() throws Exception {
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            assertThrows(ValidationException.class,
                    () -> dao.deleteLinkedOrganization(scene.getOrganizationId()));
        }
    }

    @Test
    void testDeleteOrganizationWithoutProjectsReturnsTrue() throws Exception {
        int idOrganization = persistOrganizationViaBuilder();
        boolean result = dao.deleteLinkedOrganization(idOrganization);
        assertTrue(result);
    }

    @Test
    void testDeleteOrganizationWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.deleteLinkedOrganization(TestConstants.INVALID_ID_ZERO));
    }

    private int persistOrganizationViaBuilder() throws Exception {
        int idOrganization;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idOrganization = new OrganizationTestDataBuilder()
                    .withName(NEW_ORG_NAME)
                    .withEmail(NEW_ORG_EMAIL)
                    .persist(connection);
        }
        return idOrganization;
    }

    private int persistInactiveOrganizationViaBuilder() throws Exception {
        int idOrganization;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idOrganization = new OrganizationTestDataBuilder()
                    .withName(SECONDARY_ORG_NAME)
                    .withEmail(SECONDARY_ORG_EMAIL)
                    .withStatus(TestConstants.STATUS_INACTIVE_ORG)
                    .persist(connection);
        }
        return idOrganization;
    }
}
