package test.Logic;

import Logic.DAO.PracticeDAO;
import Logic.DTOs.Practice;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PracticeDAOTest extends BaseDAOTest {

    private static final LocalDate PRACTICE_START = LocalDate.of(2025, 2, 1);
    private static final LocalDate PRACTICE_REACTIVATE = LocalDate.of(2025, 3, 1);
    private static final String STATUS_CANCELLED = "Cancelada";
    private static final int INVALID_ID_ZERO = 0;
    private static final int NON_EXISTENT_ID = 9999;
    private static final String BLANK_NRC = "  ";

    private final PracticeDAO dao = new PracticeDAO();

    private Practice buildValidPractice() {
        Practice practice = new Practice();
        practice.setNrc(NRC_EDUCATIONAL_EXPERIENCE);
        practice.setIdIntern(ID_INTERN);
        practice.setStartDate(PRACTICE_START);
        return practice;
    }

    @Test
    void testSaveValidPracticeReturnsTrue() throws Exception {
        boolean result = dao.save(buildValidPractice());
        assertTrue(result);
    }

    @Test
    void testSaveValidPracticeAssignsGeneratedId() throws Exception {
        Practice practice = buildValidPractice();
        dao.save(practice);
        assertTrue(practice.getIdPractice() > 0);
    }

    @Test
    void testSaveWithBlankNrcThrowsValidationException() {
        Practice practice = buildValidPractice();
        practice.setNrc(BLANK_NRC);
        assertThrows(ValidationException.class, () -> dao.save(practice));
    }

    @Test
    void testSaveWithZeroInternIdThrowsValidationException() {
        Practice practice = buildValidPractice();
        practice.setIdIntern(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(practice));
    }

    @Test
    void testSaveWithNullStartDateThrowsValidationException() {
        Practice practice = buildValidPractice();
        practice.setStartDate(null);
        assertThrows(ValidationException.class, () -> dao.save(practice));
    }

    @Test
    void testSaveDuplicateNrcSameInternThrowsDuplicateEntryException() throws Exception {
        dao.save(buildValidPractice());
        assertThrows(DuplicateEntryException.class, () -> dao.save(buildValidPractice()));
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws Exception {
        Practice practice = buildValidPractice();
        dao.save(practice);
        Practice retrieved = dao.findById(practice.getIdPractice());
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdAfterSaveReturnsCorrectNrc() throws Exception {
        Practice practice = buildValidPractice();
        dao.save(practice);
        Practice retrieved = dao.findById(practice.getIdPractice());
        assertEquals(NRC_EDUCATIONAL_EXPERIENCE, retrieved.getNrc());
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        Practice retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByNrcReturnsListWithSavedPractice() throws Exception {
        dao.save(buildValidPractice());
        List<Practice> list = dao.findByNrc(NRC_EDUCATIONAL_EXPERIENCE);
        assertEquals(1, list.size());
    }

    @Test
    void testFindByNrcWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findByNrc(BLANK_NRC));
    }

    @Test
    void testFindByInternAfterSaveReturnsList() throws Exception {
        dao.save(buildValidPractice());
        List<Practice> list = dao.findByIntern(ID_INTERN);
        assertEquals(1, list.size());
    }

    @Test
    void testFindByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findByIntern(INVALID_ID_ZERO));
    }

    @Test
    void testFindActiveByInternAfterSaveReturnsNotNull() throws Exception {
        dao.save(buildValidPractice());
        Practice active = dao.findActiveByIntern(ID_INTERN);
        assertNotNull(active);
    }

    @Test
    void testFindActiveByInternWhenNoActiveReturnsNull() throws Exception {
        Practice active = dao.findActiveByIntern(ID_INTERN);
        assertNull(active);
    }

    @Test
    void testHasConcludedPracticeReturnsFalseForActivePractice() throws Exception {
        dao.save(buildValidPractice());
        boolean concluded = dao.hasConcludedPractice(ID_INTERN);
        assertFalse(concluded);
    }

    @Test
    void testDeleteExistingPracticeReturnsTrue() throws Exception {
        Practice practice = buildValidPractice();
        dao.save(practice);
        boolean result = dao.delete(practice.getIdPractice());
        assertTrue(result);
    }

    @Test
    void testDeleteWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.delete(INVALID_ID_ZERO));
    }

    @Test
    void testCancelActiveByInternAndProjectReturnsTrue() throws Exception {
        dao.save(buildValidPractice());
        boolean result = dao.cancelActiveByInternAndProject(ID_INTERN, ID_PROJECT);
        assertTrue(result);
    }

    @Test
    void testCancelActiveByInternAndProjectPersistsCancelledStatus() throws Exception {
        Practice practice = buildValidPractice();
        dao.save(practice);
        dao.cancelActiveByInternAndProject(ID_INTERN, ID_PROJECT);
        Practice retrieved = dao.findById(practice.getIdPractice());
        assertEquals(STATUS_CANCELLED, retrieved.getStatus());
    }

    @Test
    void testReactivateOrCreateReactivatesCancelledPractice() throws Exception {
        dao.save(buildValidPractice());
        dao.cancelActiveByInternAndProject(ID_INTERN, ID_PROJECT);
        boolean result = dao.reactivateOrCreate(ID_INTERN, NRC_EDUCATIONAL_EXPERIENCE, PRACTICE_REACTIVATE);
        assertTrue(result);
    }
}
