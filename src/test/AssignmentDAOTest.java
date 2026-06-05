import DataAccess.DataBaseConnection;
import Logic.DAO.AssignmentDAO;
import Logic.DTOs.Assignment;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssignmentDAOTest extends BaseDAOTest {

    private final AssignmentDAO dao = new AssignmentDAO();

    private Assignment buildAssignment(AssignmentContext context) {
        Assignment assignment = new Assignment();
        assignment.setIdIntern(context.idIntern);
        assignment.setIdProject(context.idProject);
        assignment.setIdApplication(context.idApplication);
        assignment.setAssignmentDate(LocalDate.now());
        assignment.setStatus(TestConstants.STATUS_ASSIGNMENT_ACTIVE);
        assignment.setAssignmentReason(TestConstants.DEFAULT_ASSIGNMENT_REASON);
        return assignment;
    }

    @Test
    void testSaveValidAssignmentReturnsOneRowAffected() throws ServiceException, ValidationException {
        AssignmentContext context = persistAssignmentDependencies();
        int result = dao.save(buildAssignment(context));
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testSaveValidAssignmentAssignsGeneratedId() throws ServiceException, ValidationException {
        AssignmentContext context = persistAssignmentDependencies();
        Assignment assignment = buildAssignment(context);
        dao.save(assignment);
        assertTrue(assignment.getIdAssignment() > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveDuplicateInternThrowsServiceException() throws ServiceException, ValidationException {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        assertThrows(ServiceException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(buildAssignment(context));
            }
        });
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        AssignmentContext context = persistAssignmentDependencies();
        Assignment assignment = buildAssignment(context);
        dao.save(assignment);
        Assignment retrieved = dao.getById(assignment.getIdAssignment());
        assertNotNull(retrieved);
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
        Assignment retrieved = dao.getById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testGetAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<Assignment> all = dao.getAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        List<Assignment> all = dao.getAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testGetByIdInternReturnsOneElement() throws ServiceException, ValidationException {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        List<Assignment> byIntern = dao.getByIdIntern(context.idIntern);
        assertEquals(TestConstants.SINGLE_RESULT, byIntern.size());
    }

    @Test
    void testGetByIdInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.getByIdIntern(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testGetByIdProjectReturnsOneElement() throws ServiceException, ValidationException {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        List<Assignment> byProject = dao.getByIdProject(context.idProject);
        assertEquals(TestConstants.SINGLE_RESULT, byProject.size());
    }

    @Test
    void testGetActiveByIdInternReturnsNotNull() throws ServiceException, ValidationException {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        Assignment active = dao.getActiveByIdIntern(context.idIntern);
        assertNotNull(active);
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws ServiceException, ValidationException {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        boolean result = dao.deleteByInternAndProject(context.idIntern, context.idProject);
        assertTrue(result);
    }

    private AssignmentContext persistAssignmentDependencies() throws ServiceException, ValidationException {
        AssignmentContext context = new AssignmentContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
            context.idApplication = new ApplicationTestDataBuilder()
                    .withInternId(scene.getInternId())
                    .withStatus(TestConstants.STATUS_ACCEPTED)
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return context;
    }

    private static final class AssignmentContext {
        int idIntern;
        int idProject;
        int idApplication;
    }
}
