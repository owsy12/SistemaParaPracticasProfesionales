import DataAccess.DataBaseConnection;
import Logic.DAO.SelfEvaluationDAO;
import Logic.DTOs.SelfEvaluation;
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

class SelfEvaluationDAOTest extends BaseDAOTest {

    private final SelfEvaluationDAO dao = new SelfEvaluationDAO();

    private SelfEvaluation buildSelfEvaluation(int idIntern, int idProject) {
        SelfEvaluation selfEvaluation = new SelfEvaluation();
        selfEvaluation.setIdIntern(idIntern);
        selfEvaluation.setIdProject(idProject);
        selfEvaluation.setPeriod(TestConstants.DEFAULT_PERIOD);
        selfEvaluation.setStatement01(TestConstants.DEFAULT_STATEMENT_SCORE);
        selfEvaluation.setStatement02(TestConstants.DEFAULT_STATEMENT_SCORE);
        selfEvaluation.setStatement03(TestConstants.DEFAULT_STATEMENT_SCORE);
        selfEvaluation.setStatement04(TestConstants.DEFAULT_STATEMENT_SCORE);
        selfEvaluation.setStatement05(TestConstants.DEFAULT_STATEMENT_SCORE);
        selfEvaluation.setStatement06(TestConstants.DEFAULT_STATEMENT_SCORE);
        selfEvaluation.setStatement07(TestConstants.DEFAULT_STATEMENT_SCORE);
        selfEvaluation.setStatement08(TestConstants.DEFAULT_STATEMENT_SCORE);
        selfEvaluation.setStatement09(TestConstants.DEFAULT_STATEMENT_SCORE);
        selfEvaluation.setStatement10(TestConstants.DEFAULT_STATEMENT_SCORE);
        selfEvaluation.setFinalScore(TestConstants.DEFAULT_FINAL_SCORE);
        selfEvaluation.setPlaceAndDate(TestConstants.DEFAULT_PLACE_AND_DATE);
        selfEvaluation.setDocumentPath(TestConstants.DEFAULT_DOCUMENT_PATH);
        selfEvaluation.setStatus(TestConstants.STATUS_SELF_EVAL_PENDING);
        return selfEvaluation;
    }

    @Test
    void testSaveValidSelfEvaluationReturnsOneRowAffected() throws ServiceException, ValidationException {
        SelfEvalContext context = persistContext();
        int result = dao.save(buildSelfEvaluation(context.idIntern, context.idProject));
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testSaveSelfEvaluationWithZeroInternIdThrowsValidationException() throws ServiceException, ValidationException {
        SelfEvalContext context = persistContext();
        SelfEvaluation selfEvaluation = buildSelfEvaluation(TestConstants.INVALID_ID_ZERO, context.idProject);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(selfEvaluation);
            }
        });
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        SelfEvalContext context = persistContext();
        SelfEvaluation selfEvaluation = buildSelfEvaluation(context.idIntern, context.idProject);
        dao.save(selfEvaluation);
        SelfEvaluation retrieved = dao.getById(selfEvaluation.getIdSelfEvaluation());
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdAfterSaveReturnsEqualObject() throws ServiceException, ValidationException {
        SelfEvalContext context = persistContext();
        SelfEvaluation selfEvaluation = buildSelfEvaluation(context.idIntern, context.idProject);
        dao.save(selfEvaluation);
        SelfEvaluation retrieved = dao.getById(selfEvaluation.getIdSelfEvaluation());
        assertEquals(selfEvaluation, retrieved);
    }

    @Test
    void testGetByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.getById(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testGetByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        SelfEvaluation retrieved = dao.getById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        SelfEvalContext context = persistContext();
        dao.save(buildSelfEvaluation(context.idIntern, context.idProject));
        List<SelfEvaluation> all = dao.getAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testGetAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<SelfEvaluation> all = dao.getAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindByIdInternAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        SelfEvalContext context = persistContext();
        dao.save(buildSelfEvaluation(context.idIntern, context.idProject));
        SelfEvaluation retrieved = dao.findByIdIntern(context.idIntern);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findByIdIntern(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testUpdateStatusReturnsTrue() throws ServiceException, ValidationException {
        SelfEvalContext context = persistContext();
        SelfEvaluation selfEvaluation = buildSelfEvaluation(context.idIntern, context.idProject);
        dao.save(selfEvaluation);
        boolean result = dao.updateStatus(
                selfEvaluation.getIdSelfEvaluation(), TestConstants.STATUS_SELF_EVAL_DELIVERED);
        assertTrue(result);
    }

    @Test
    void testUpdateStatusWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.updateStatus(TestConstants.INVALID_ID_ZERO, TestConstants.STATUS_SELF_EVAL_DELIVERED);
            }
        });
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws ServiceException, ValidationException {
        SelfEvalContext context = persistContext();
        dao.save(buildSelfEvaluation(context.idIntern, context.idProject));
        boolean result = dao.deleteByInternAndProject(context.idIntern, context.idProject);
        assertTrue(result);
    }

    private SelfEvalContext persistContext() throws ServiceException, ValidationException {
        SelfEvalContext context = new SelfEvalContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return context;
    }

    private static final class SelfEvalContext {
        int idIntern;
        int idProject;
    }
}
