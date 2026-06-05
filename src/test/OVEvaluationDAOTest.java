import DataAccess.DataBaseConnection;
import Logic.DAO.OVEvaluationDAO;
import Logic.DTOs.OVEvaluation;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OVEvaluationDAOTest extends BaseDAOTest {

    private final OVEvaluationDAO dao = new OVEvaluationDAO();

    private OVEvaluation buildOVEvaluation(int idIntern, int idProject) {
        OVEvaluation evaluation = new OVEvaluation();
        evaluation.setIdIntern(idIntern);
        evaluation.setIdProject(idProject);
        evaluation.setDocumentPath(TestConstants.DEFAULT_DOCUMENT_PATH);
        evaluation.setStatus(TestConstants.STATUS_OV_EVAL_PENDING);
        evaluation.setDeliveryDate(LocalDateTime.now());
        return evaluation;
    }

    @Test
    void testSaveValidOVEvaluationReturnsOneRowAffected() throws Exception {
        OVEvalContext context = persistContext();
        int result = dao.save(buildOVEvaluation(context.idIntern, context.idProject));
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testSaveOVEvaluationWithZeroInternIdThrowsValidationException() throws Exception {
        OVEvalContext context = persistContext();
        OVEvaluation evaluation = buildOVEvaluation(TestConstants.INVALID_ID_ZERO, context.idProject);
        assertThrows(ValidationException.class, () -> dao.save(evaluation));
    }

    @Test
    void testFindByInternAndProjectAfterSaveReturnsNotNull() throws Exception {
        OVEvalContext context = persistContext();
        dao.save(buildOVEvaluation(context.idIntern, context.idProject));
        OVEvaluation retrieved = dao.findByInternAndProject(context.idIntern, context.idProject);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByInternAndProjectWithNonExistentReturnsNull() throws Exception {
        OVEvaluation retrieved = dao.findByInternAndProject(
                TestConstants.NON_EXISTENT_ID, TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByInternAndProjectWithZeroInternIdThrowsValidationException() throws Exception {
        OVEvalContext context = persistContext();
        assertThrows(ValidationException.class,
                () -> dao.findByInternAndProject(TestConstants.INVALID_ID_ZERO, context.idProject));
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws Exception {
        OVEvalContext context = persistContext();
        dao.save(buildOVEvaluation(context.idIntern, context.idProject));
        boolean result = dao.deleteByInternAndProject(context.idIntern, context.idProject);
        assertTrue(result);
    }

    @Test
    void testDeleteByInternAndProjectWithZeroProjectIdThrowsValidationException() throws Exception {
        OVEvalContext context = persistContext();
        assertThrows(ValidationException.class,
                () -> dao.deleteByInternAndProject(context.idIntern, TestConstants.INVALID_ID_ZERO));
    }

    private OVEvalContext persistContext() throws Exception {
        OVEvalContext context = new OVEvalContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
        }
        return context;
    }

    private static final class OVEvalContext {
        int idIntern;
        int idProject;
    }
}
