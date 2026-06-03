package test.Logic;

import Logic.DAO.SelfEvaluationDAO;
import Logic.DTOs.SelfEvaluation;
import Logic.Exceptions.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelfEvaluationDAOTest extends BaseDAOTest {

    private static final String EVALUATION_PERIOD = "2025-01";
    private static final String EVALUATION_PERIOD_ALTERNATIVE = "2025-02";
    private static final String EVALUATION_PLACE_AND_DATE = "Xalapa, Ver., 15 de enero de 2025";
    private static final String EVALUATION_DOCUMENT_PATH = "/docs/autoevaluacion_2025_01.pdf";
    private static final int STATEMENT_VALUE_FOUR = 4;
    private static final int STATEMENT_VALUE_FIVE = 5;
    private static final int STATEMENT_VALUE_THREE = 3;
    private static final int FINAL_SCORE = 42;
    private static final int NON_EXISTENT_EVALUATION_ID = 9999;

    private final SelfEvaluationDAO dao = new SelfEvaluationDAO();

    private SelfEvaluation buildValidSelfEvaluation() {
        SelfEvaluation selfEvaluation = new SelfEvaluation();
        selfEvaluation.setIdIntern(ID_INTERN);
        selfEvaluation.setIdProyect(ID_PROJECT);
        selfEvaluation.setPeriod(EVALUATION_PERIOD);
        selfEvaluation.setStatement01(STATEMENT_VALUE_FOUR);
        selfEvaluation.setStatement02(STATEMENT_VALUE_FIVE);
        selfEvaluation.setStatement03(STATEMENT_VALUE_THREE);
        selfEvaluation.setStatement04(STATEMENT_VALUE_FOUR);
        selfEvaluation.setStatement05(STATEMENT_VALUE_FIVE);
        selfEvaluation.setStatement06(STATEMENT_VALUE_FOUR);
        selfEvaluation.setStatement07(STATEMENT_VALUE_THREE);
        selfEvaluation.setStatement08(STATEMENT_VALUE_FIVE);
        selfEvaluation.setStatement09(STATEMENT_VALUE_FOUR);
        selfEvaluation.setStatement10(STATEMENT_VALUE_FIVE);
        selfEvaluation.setFinalScore(FINAL_SCORE);
        selfEvaluation.setPlaceAndDate(EVALUATION_PLACE_AND_DATE);
        selfEvaluation.setDocumentPath(EVALUATION_DOCUMENT_PATH);
        selfEvaluation.setStatus(STATUS_PENDING);
        return selfEvaluation;
    }

    @Test
    void testSaveValidSelfEvaluationReturnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidSelfEvaluation());
        assertEquals(1, result);
    }

    @Test
    void testSaveValidSelfEvaluationAssignsGeneratedId() throws Exception {
        SelfEvaluation selfEvaluation = buildValidSelfEvaluation();
        dao.save(selfEvaluation);
        assertTrue(selfEvaluation.getIdSelfEvalation() > 0);
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws Exception {
        SelfEvaluation selfEvaluation = buildValidSelfEvaluation();
        dao.save(selfEvaluation);
        SelfEvaluation retrieved = dao.getById(selfEvaluation.getIdSelfEvalation());
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectPeriod() throws Exception {
        SelfEvaluation selfEvaluation = buildValidSelfEvaluation();
        dao.save(selfEvaluation);
        SelfEvaluation retrieved = dao.getById(selfEvaluation.getIdSelfEvalation());
        assertEquals(EVALUATION_PERIOD, retrieved.getPeriod());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectStatement01() throws Exception {
        SelfEvaluation selfEvaluation = buildValidSelfEvaluation();
        dao.save(selfEvaluation);
        SelfEvaluation retrieved = dao.getById(selfEvaluation.getIdSelfEvalation());
        assertEquals(STATEMENT_VALUE_FOUR, retrieved.getStatement01());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectInternId() throws Exception {
        SelfEvaluation selfEvaluation = buildValidSelfEvaluation();
        dao.save(selfEvaluation);
        SelfEvaluation retrieved = dao.getById(selfEvaluation.getIdSelfEvalation());
        assertEquals(ID_INTERN, retrieved.getIdIntern());
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws Exception {
        dao.save(buildValidSelfEvaluation());
        List<SelfEvaluation> all = dao.getAll();
        assertEquals(1, all.size());
    }

    @Test
    void testGetByIdWithNonExistentIdReturnsNull() throws Exception {
        SelfEvaluation retrieved = dao.getById(NON_EXISTENT_EVALUATION_ID);
        assertNull(retrieved);
    }

    @Test
    void testSaveDuplicatePeriodSameInternThrowsServiceException() throws Exception {
        dao.save(buildValidSelfEvaluation());
        assertThrows(ServiceException.class, () -> dao.save(buildValidSelfEvaluation()));
    }

    @Test
    void testSaveWithDifferentPeriodReturnsOneRowAffected() throws Exception {
        dao.save(buildValidSelfEvaluation());
        SelfEvaluation second = buildValidSelfEvaluation();
        second.setPeriod(EVALUATION_PERIOD_ALTERNATIVE);
        int result = dao.save(second);
        assertEquals(1, result);
    }
}
