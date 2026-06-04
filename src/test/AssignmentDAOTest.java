import Logic.DAO.AssignmentDAO;
import Logic.DTOs.Assignment;
import Logic.Exceptions.ServiceException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentDAOTest extends BaseDAOTest {

    private final AssignmentDAO dao = new AssignmentDAO();

    private Assignment buildValidAssignment() {
        Assignment assignment = new Assignment();
        assignment.setIdIntern(ID_INTERN);
        assignment.setIdProject(ID_PROJECT);
        assignment.setIdApplication(ID_APPLICATION);
        assignment.setAssignmentDate(LocalDate.now());
        return assignment;
    }

    @Test
    void testSaveValidAssignmentReturnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidAssignment());
        assertEquals(1, result);
    }

    @Test
    void testSaveValidAssignmentAssignsGeneratedId() throws Exception {
        Assignment assignment = buildValidAssignment();
        dao.save(assignment);
        assertTrue(assignment.getIdAssignment() > 0);
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws Exception {
        Assignment assignment = buildValidAssignment();
        dao.save(assignment);
        Assignment retrieved = dao.getById(assignment.getIdAssignment());
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectInternId() throws Exception {
        Assignment assignment = buildValidAssignment();
        dao.save(assignment);
        Assignment retrieved = dao.getById(assignment.getIdAssignment());
        assertEquals(ID_INTERN, retrieved.getIdIntern());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectProjectId() throws Exception {
        Assignment assignment = buildValidAssignment();
        dao.save(assignment);
        Assignment retrieved = dao.getById(assignment.getIdAssignment());
        assertEquals(ID_PROJECT, retrieved.getIdProject());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectApplicationId() throws Exception {
        Assignment assignment = buildValidAssignment();
        dao.save(assignment);
        Assignment retrieved = dao.getById(assignment.getIdAssignment());
        assertEquals(ID_APPLICATION, retrieved.getIdApplication());
    }

    @Test
    void testGetByIdProjectAfterSaveReturnsListWithOneElement() throws Exception {
        dao.save(buildValidAssignment());
        List<Assignment> byProject = dao.getByIdProject(ID_PROJECT);
        assertEquals(1, byProject.size());
    }

    @Test
    void testGetByIdProjectAfterSaveReturnsCorrectProjectId() throws Exception {
        dao.save(buildValidAssignment());
        List<Assignment> byProject = dao.getByIdProject(ID_PROJECT);
        assertEquals(ID_PROJECT, byProject.get(0).getIdProject());
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws Exception {
        dao.save(buildValidAssignment());
        List<Assignment> all = dao.getAll();
        assertEquals(1, all.size());
    }

    @Test
    void testSaveDuplicateInternThrowsServiceException() throws Exception {
        dao.save(buildValidAssignment());
        assertThrows(ServiceException.class, () -> dao.save(buildValidAssignment()));
    }
}
