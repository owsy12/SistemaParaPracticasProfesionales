import DataAccess.DataBaseConnection;
import Logic.DAO.AssignmentDAO;
import Logic.DTOs.Assignment;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
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
    void testSaveValidAssignmentReturnsOneRowAffected() throws Exception {
        AssignmentContext context = persistAssignmentDependencies();
        int result = dao.save(buildAssignment(context));
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testSaveValidAssignmentAssignsGeneratedId() throws Exception {
        AssignmentContext context = persistAssignmentDependencies();
        Assignment assignment = buildAssignment(context);
        dao.save(assignment);
        assertTrue(assignment.getIdAssignment() > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveDuplicateInternThrowsServiceException() throws Exception {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        assertThrows(ServiceException.class, () -> dao.save(buildAssignment(context)));
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws Exception {
        AssignmentContext context = persistAssignmentDependencies();
        Assignment assignment = buildAssignment(context);
        dao.save(assignment);
        Assignment retrieved = dao.getById(assignment.getIdAssignment());
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.getById(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testGetByIdWithNonExistentIdReturnsNull() throws Exception {
        Assignment retrieved = dao.getById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testGetAllWithNoDataReturnsEmptyList() throws Exception {
        List<Assignment> all = dao.getAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws Exception {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        List<Assignment> all = dao.getAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testGetByIdInternReturnsOneElement() throws Exception {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        List<Assignment> byIntern = dao.getByIdIntern(context.idIntern);
        assertEquals(TestConstants.SINGLE_RESULT, byIntern.size());
    }

    @Test
    void testGetByIdInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.getByIdIntern(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testGetByIdProjectReturnsOneElement() throws Exception {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        List<Assignment> byProject = dao.getByIdProject(context.idProject);
        assertEquals(TestConstants.SINGLE_RESULT, byProject.size());
    }

    @Test
    void testGetActiveByIdInternReturnsNotNull() throws Exception {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        Assignment active = dao.getActiveByIdIntern(context.idIntern);
        assertNotNull(active);
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws Exception {
        AssignmentContext context = persistAssignmentDependencies();
        dao.save(buildAssignment(context));
        boolean result = dao.deleteByInternAndProject(context.idIntern, context.idProject);
        assertTrue(result);
    }

    private AssignmentContext persistAssignmentDependencies() throws Exception {
        AssignmentContext context = new AssignmentContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
            context.idApplication = new ApplicationTestDataBuilder()
                    .withInternId(scene.getInternId())
                    .withStatus(TestConstants.STATUS_ACCEPTED)
                    .persist(connection);
        }
        return context;
    }

    private static final class AssignmentContext {
        int idIntern;
        int idProject;
        int idApplication;
    }
}
