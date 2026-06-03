import Logic.DAO.OVEvaluationDAO;
import Logic.DTOs.OVEvaluation;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OVEvaluationDAOTest extends BaseDAOTest {

    private static final String EVALUATION_DOCUMENT_PATH = "/docs/evaluacion_ov.pdf";
    private static final String EVALUATION_STATUS_DELIVERED = "Entregado";
    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -2;
    private static final int NON_EXISTENT_ID = 9999;

    private final OVEvaluationDAO dao = new OVEvaluationDAO();

    private OVEvaluation buildValidEvaluation() {
        OVEvaluation evaluation = new OVEvaluation();
        evaluation.setIdIntern(ID_INTERN);
        evaluation.setIdProject(ID_PROJECT);
        evaluation.setDocumentPath(EVALUATION_DOCUMENT_PATH);
        evaluation.setStatus(EVALUATION_STATUS_DELIVERED);
        evaluation.setDeliveryDate(LocalDateTime.now());
        return evaluation;
    }

    @Test
    void testSaveValidEvaluationReturnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidEvaluation());
        assertEquals(1, result);
    }

    @Test
    void testSaveValidEvaluationAssignsGeneratedId() throws Exception {
        OVEvaluation evaluation = buildValidEvaluation();
        dao.save(evaluation);
        assertTrue(evaluation.getIdOVEvaluation() > 0);
    }

    @Test
    void testSaveEvaluationWithZeroInternIdThrowsValidationException() {
        OVEvaluation evaluation = buildValidEvaluation();
        evaluation.setIdIntern(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(evaluation));
    }

    @Test
    void testSaveEvaluationWithZeroProjectIdThrowsValidationException() {
        OVEvaluation evaluation = buildValidEvaluation();
        evaluation.setIdProject(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(evaluation));
    }

    @Test
    void testSaveDuplicateInternAndProjectThrowsDuplicateEntryException() throws Exception {
        dao.save(buildValidEvaluation());
        assertThrows(DuplicateEntryException.class, () -> dao.save(buildValidEvaluation()));
    }

    @Test
    void testFindByInternAndProjectAfterSaveReturnsNotNull() throws Exception {
        dao.save(buildValidEvaluation());
        OVEvaluation retrieved = dao.findByInternAndProject(ID_INTERN, ID_PROJECT);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByInternAndProjectAfterSaveReturnsCorrectInternId() throws Exception {
        dao.save(buildValidEvaluation());
        OVEvaluation retrieved = dao.findByInternAndProject(ID_INTERN, ID_PROJECT);
        assertEquals(ID_INTERN, retrieved.getIdIntern());
    }

    @Test
    void testFindByInternAndProjectAfterSaveReturnsCorrectStatus() throws Exception {
        dao.save(buildValidEvaluation());
        OVEvaluation retrieved = dao.findByInternAndProject(ID_INTERN, ID_PROJECT);
        assertEquals(EVALUATION_STATUS_DELIVERED, retrieved.getStatus());
    }

    @Test
    void testFindByInternAndProjectWhenNoEvaluationReturnsNull() throws Exception {
        OVEvaluation retrieved = dao.findByInternAndProject(ID_INTERN, ID_PROJECT);
        assertNull(retrieved);
    }

    @Test
    void testFindByInternAndProjectWithZeroInternIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findByInternAndProject(INVALID_ID_ZERO, ID_PROJECT));
    }

    @Test
    void testFindByInternAndProjectWithNegativeProjectIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findByInternAndProject(ID_INTERN, INVALID_ID_NEGATIVE));
    }

    @Test
    void testDeleteByInternAndProjectAfterSaveReturnsTrue() throws Exception {
        dao.save(buildValidEvaluation());
        boolean result = dao.deleteByInternAndProject(ID_INTERN, ID_PROJECT);
        assertTrue(result);
    }

    @Test
    void testDeleteByInternAndProjectRemovesEvaluation() throws Exception {
        dao.save(buildValidEvaluation());
        dao.deleteByInternAndProject(ID_INTERN, ID_PROJECT);
        OVEvaluation retrieved = dao.findByInternAndProject(ID_INTERN, ID_PROJECT);
        assertNull(retrieved);
    }

    @Test
    void testDeleteByInternAndProjectWithNonExistentReturnsTrue() throws Exception {
        boolean result = dao.deleteByInternAndProject(ID_INTERN, NON_EXISTENT_ID);
        assertTrue(result);
    }
}
