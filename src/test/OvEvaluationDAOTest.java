import DataAccess.DataBaseConnection;
import Logic.DAO.OvEvaluationDAO;
import Logic.DTOs.OvEvaluation;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OvEvaluationDAOTest extends BaseDAOTest {

    private final OvEvaluationDAO dao = new OvEvaluationDAO();

    private OvEvaluation buildOvEvaluation(int idIntern, int idProject) {
        OvEvaluation evaluation = new OvEvaluation();
        evaluation.setIdIntern(idIntern);
        evaluation.setIdProject(idProject);
        evaluation.setDocumentPath(TestConstants.DEFAULT_DOCUMENT_PATH);
        evaluation.setStatus(TestConstants.STATUS_OV_EVAL_PENDING);
        evaluation.setDeliveryDate(LocalDateTime.now());
        return evaluation;
    }

    @Test
    void testSaveValidOvEvaluationReturnsOneRowAffected() throws ServiceException, ValidationException {
        OVEvalContext context = persistContext();
        int result = dao.save(buildOvEvaluation(context.idIntern, context.idProject));
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testSaveOvEvaluationWithZeroInternIdThrowsValidationException() throws ServiceException, ValidationException {
        OVEvalContext context = persistContext();
        OvEvaluation evaluation = buildOvEvaluation(TestConstants.INVALID_ID_ZERO, context.idProject);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(evaluation);
            }
        });
    }

    @Test
    void testFindByInternAndProjectAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        OVEvalContext context = persistContext();
        dao.save(buildOvEvaluation(context.idIntern, context.idProject));
        OvEvaluation retrieved = dao.findByInternAndProject(context.idIntern, context.idProject);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByInternAndProjectWithNonExistentReturnsNull() throws ServiceException, ValidationException {
        OvEvaluation retrieved = dao.findByInternAndProject(
                TestConstants.NON_EXISTENT_ID, TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByInternAndProjectWithZeroInternIdThrowsValidationException() throws ServiceException, ValidationException {
        OVEvalContext context = persistContext();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findByInternAndProject(TestConstants.INVALID_ID_ZERO, context.idProject);
            }
        });
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws ServiceException, ValidationException {
        OVEvalContext context = persistContext();
        dao.save(buildOvEvaluation(context.idIntern, context.idProject));
        boolean result = dao.deleteByInternAndProject(context.idIntern, context.idProject);
        assertTrue(result);
    }

    @Test
    void testDeleteByInternAndProjectWithZeroProjectIdThrowsValidationException() throws ServiceException, ValidationException {
        OVEvalContext context = persistContext();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.deleteByInternAndProject(context.idIntern, TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    private OVEvalContext persistContext() throws ServiceException, ValidationException {
        OVEvalContext context = new OVEvalContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return context;
    }

    private static final class OVEvalContext {
        int idIntern;
        int idProject;
    }
}
