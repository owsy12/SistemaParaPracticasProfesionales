import Logic.DAO.TechnicalResponsibleDAO;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalResponsibleDAOTest extends BaseDAOTest {

    private static final String NEW_NAME = "Mario";
    private static final String NEW_LAST_NAME = "Hernández";
    private static final String NEW_SECOND_LAST_NAME = "Ríos";
    private static final String NEW_EMAIL = "mario.hernandez@techcorp.mx";
    private static final String NEW_POSITION = "Líder de proyecto";
    private static final String UPDATED_NAME = "Mario Alberto";
    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ORGANIZATION_ID_ZERO = 0;
    private static final int NON_EXISTENT_ID = 9999;

    private final TechnicalResponsibleDAO dao = new TechnicalResponsibleDAO();

    private TechnicalSupervisor buildValidSupervisor() {
        TechnicalSupervisor supervisor = new TechnicalSupervisor();
        supervisor.setIdOrganization(ID_ORGANIZATION);
        supervisor.setName(NEW_NAME);
        supervisor.setLastName(NEW_LAST_NAME);
        supervisor.setSecondLastName(NEW_SECOND_LAST_NAME);
        supervisor.seteMail(NEW_EMAIL);
        supervisor.setPosition(NEW_POSITION);
        return supervisor;
    }

    @Test
    void testSaveValidSupervisorReturnsTrue() throws Exception {
        boolean result = dao.saveTechnicalResponsible(buildValidSupervisor());
        assertTrue(result);
    }

    @Test
    void testSaveDuplicateEmailThrowsDuplicateEntryException() throws Exception {
        dao.saveTechnicalResponsible(buildValidSupervisor());
        assertThrows(DuplicateEntryException.class,
                () -> dao.saveTechnicalResponsible(buildValidSupervisor()));
    }

    @Test
    void testFindByIdReturnsSupportSupervisor() throws Exception {
        TechnicalSupervisor retrieved = dao.findById(ID_TECHNICAL);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdReturnsCorrectOrganizationId() throws Exception {
        TechnicalSupervisor retrieved = dao.findById(ID_TECHNICAL);
        assertEquals(ID_ORGANIZATION, retrieved.getIdOrganization());
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        TechnicalSupervisor retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllReturnsAtLeastOneElement() throws Exception {
        List<TechnicalSupervisor> all = dao.findAll();
        assertEquals(1, all.size());
    }

    @Test
    void testFindAllAfterSaveReturnsTwoElements() throws Exception {
        dao.saveTechnicalResponsible(buildValidSupervisor());
        List<TechnicalSupervisor> all = dao.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testFindByOrganizationReturnsSupportSupervisor() throws Exception {
        List<TechnicalSupervisor> supervisors = dao.findByOrganization(ID_ORGANIZATION);
        assertEquals(1, supervisors.size());
    }

    @Test
    void testFindByOrganizationWhenNoneReturnsEmptyList() throws Exception {
        List<TechnicalSupervisor> supervisors = dao.findByOrganization(NON_EXISTENT_ID);
        assertTrue(supervisors.isEmpty());
    }

    @Test
    void testFindByOrganizationWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findByOrganization(INVALID_ORGANIZATION_ID_ZERO));
    }

    @Test
    void testUpdateSupervisorReturnsTrue() throws Exception {
        TechnicalSupervisor supervisor = dao.findById(ID_TECHNICAL);
        supervisor.setName(UPDATED_NAME);
        boolean result = dao.update(supervisor);
        assertTrue(result);
    }

    @Test
    void testUpdateSupervisorPersistsNewName() throws Exception {
        TechnicalSupervisor supervisor = dao.findById(ID_TECHNICAL);
        supervisor.setName(UPDATED_NAME);
        dao.update(supervisor);
        TechnicalSupervisor retrieved = dao.findById(ID_TECHNICAL);
        assertEquals(UPDATED_NAME, retrieved.getName());
    }

    @Test
    void testUpdateSupervisorWithZeroIdThrowsValidationException() {
        TechnicalSupervisor supervisor = buildValidSupervisor();
        supervisor.setIdTechnicalSupervisor(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.update(supervisor));
    }

    @Test
    void testDeleteWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.delete(INVALID_ID_ZERO));
    }

    @Test
    void testDeleteNonExistentSupervisorReturnsFalse() throws Exception {
        boolean result = dao.delete(NON_EXISTENT_ID);
        assertFalse(result);
    }

    @Test
    void testDeleteWithOrganizationValidationFailsWhenOrgHasProjects() {
        assertThrows(ValidationException.class,
                () -> dao.deleteWithOrganizationValidation(ID_TECHNICAL));
    }
}
