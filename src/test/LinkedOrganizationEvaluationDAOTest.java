import DataAccess.DataBaseConnection;
import Logic.DAO.LinkedOrganizationEvaluationDAO;
import Logic.DTOs.LinkedOrganizationEvaluation;
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

class LinkedOrganizationEvaluationDAOTest extends BaseDAOTest {

    private final LinkedOrganizationEvaluationDAO linkedOrganizationEvaluationDAO = new LinkedOrganizationEvaluationDAO();

    private LinkedOrganizationEvaluation buildLinkedOrganizationEvaluation(int idIntern, int idProject) {
        LinkedOrganizationEvaluation evaluation = new LinkedOrganizationEvaluation();
        evaluation.setIdIntern(idIntern);
        evaluation.setIdProject(idProject);
        evaluation.setDocumentPath(TestConstants.DEFAULT_DOCUMENT_PATH);
        evaluation.setStatus(TestConstants.STATUS_OV_EVAL_PENDING);
        evaluation.setDeliveryDate(LocalDateTime.now());
        return evaluation;
    }

    @Test
    void testSaveValidLinkedOrganizationEvaluationReturnsOneRowAffected() throws ServiceException, ValidationException {
        EvaluationContext context = persistContext();
        int result = linkedOrganizationEvaluationDAO.save(buildLinkedOrganizationEvaluation(context.idIntern, context.idProject));
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testSaveLinkedOrganizationEvaluationWithZeroInternIdThrowsValidationException() throws ServiceException, ValidationException {
        EvaluationContext context = persistContext();
        LinkedOrganizationEvaluation evaluation = buildLinkedOrganizationEvaluation(TestConstants.INVALID_ID_ZERO, context.idProject);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                linkedOrganizationEvaluationDAO.save(evaluation);
            }
        });
    }

    @Test
    void testFindByInternAndProjectAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        EvaluationContext context = persistContext();
        linkedOrganizationEvaluationDAO.save(buildLinkedOrganizationEvaluation(context.idIntern, context.idProject));
        LinkedOrganizationEvaluation retrieved = linkedOrganizationEvaluationDAO.findByInternAndProject(context.idIntern, context.idProject);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByInternAndProjectWithNonExistentReturnsNull() throws ServiceException, ValidationException {
        LinkedOrganizationEvaluation retrieved = linkedOrganizationEvaluationDAO.findByInternAndProject(
                TestConstants.NON_EXISTENT_ID, TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByInternAndProjectWithZeroInternIdThrowsValidationException() throws ServiceException, ValidationException {
        EvaluationContext context = persistContext();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                linkedOrganizationEvaluationDAO.findByInternAndProject(TestConstants.INVALID_ID_ZERO, context.idProject);
            }
        });
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws ServiceException, ValidationException {
        EvaluationContext context = persistContext();
        linkedOrganizationEvaluationDAO.save(buildLinkedOrganizationEvaluation(context.idIntern, context.idProject));
        boolean result = linkedOrganizationEvaluationDAO.deleteByInternAndProject(context.idIntern, context.idProject);
        assertTrue(result);
    }

    @Test
    void testDeleteByInternAndProjectWithZeroProjectIdThrowsValidationException() throws ServiceException, ValidationException {
        EvaluationContext context = persistContext();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                linkedOrganizationEvaluationDAO.deleteByInternAndProject(context.idIntern, TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    private EvaluationContext persistContext() throws ServiceException, ValidationException {
        EvaluationContext context = new EvaluationContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return context;
    }

    private static final class EvaluationContext {
        int idIntern;
        int idProject;
    }
}
