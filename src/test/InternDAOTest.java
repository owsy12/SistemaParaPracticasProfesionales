import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InternDAOTest extends BaseDAOTest {

    private static final int UPDATED_CREDITS = 240;
    private static final int NEGATIVE_CREDITS = -10;
    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -1;
    private static final int NON_EXISTENT_ID = 9999;
    private static final int EXPECTED_INITIAL_CREDITS = 200;

    private InternDAO createDao() throws Exception {
        return new InternDAO();
    }

    @Test
    void testFindByIdReturnsSupportIntern() throws Exception {
        InternDAO dao = createDao();
        Intern retrieved = dao.findById(ID_INTERN);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdReturnsCorrectCredits() throws Exception {
        InternDAO dao = createDao();
        Intern retrieved = dao.findById(ID_INTERN);
        assertEquals(EXPECTED_INITIAL_CREDITS, retrieved.getCredits());
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() throws Exception {
        InternDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() throws Exception {
        InternDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        InternDAO dao = createDao();
        Intern retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllCoordinatorsReturnsSupportIntern() throws Exception {
        InternDAO dao = createDao();
        List<Intern> all = dao.findAllCoordinators();
        assertEquals(1, all.size());
    }

    @Test
    void testFindAllActiveInternsReturnsSupportIntern() throws Exception {
        InternDAO dao = createDao();
        List<Intern> active = dao.findAllActiveinterns();
        assertEquals(1, active.size());
    }

    @Test
    void testFindByProjectWhenNoAssignmentReturnsEmptyList() throws Exception {
        InternDAO dao = createDao();
        List<Intern> interns = dao.findByProject(ID_PROJECT);
        assertTrue(interns.isEmpty());
    }

    @Test
    void testFindByProjectWithZeroIdThrowsValidationException() throws Exception {
        InternDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.findByProject(INVALID_ID_ZERO));
    }

    @Test
    void testDeactivateInternWithZeroIdThrowsValidationException() throws Exception {
        InternDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.deactivateIntern(INVALID_ID_ZERO));
    }

    @Test
    void testDeactivateInternReturnsTrue() throws Exception {
        InternDAO dao = createDao();
        boolean result = dao.deactivateIntern(ID_INTERN);
        assertTrue(result);
    }

    @Test
    void testDeactivateInternWithNonExistentIdReturnsFalse() throws Exception {
        InternDAO dao = createDao();
        boolean result = dao.deactivateIntern(NON_EXISTENT_ID);
        assertFalse(result);
    }

    @Test
    void testUpdateCreditsReturnsTrue() throws Exception {
        InternDAO dao = createDao();
        boolean result = dao.updateCredits(ID_INTERN, UPDATED_CREDITS);
        assertTrue(result);
    }

    @Test
    void testUpdateCreditsPersistsNewValue() throws Exception {
        InternDAO dao = createDao();
        dao.updateCredits(ID_INTERN, UPDATED_CREDITS);
        Intern retrieved = dao.findById(ID_INTERN);
        assertEquals(UPDATED_CREDITS, retrieved.getCredits());
    }

    @Test
    void testUpdateCreditsWithZeroIdThrowsValidationException() throws Exception {
        InternDAO dao = createDao();
        assertThrows(ValidationException.class,
                () -> dao.updateCredits(INVALID_ID_ZERO, UPDATED_CREDITS));
    }

    @Test
    void testUpdateCreditsWithNegativeCreditsThrowsValidationException() throws Exception {
        InternDAO dao = createDao();
        assertThrows(ValidationException.class,
                () -> dao.updateCredits(ID_INTERN, NEGATIVE_CREDITS));
    }

    @Test
    void testUpdateCreditsWithNonExistentInternReturnsFalse() throws Exception {
        InternDAO dao = createDao();
        boolean result = dao.updateCredits(NON_EXISTENT_ID, UPDATED_CREDITS);
        assertFalse(result);
    }
}
