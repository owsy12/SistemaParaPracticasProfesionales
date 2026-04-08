package Logic;

import Logic.DAO.AssignmentDAO;
import Logic.DTOs.Assignment;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentDAOTest extends BaseDAOTest {

    private final AssignmentDAO dao = new AssignmentDAO();

    private Assignment buildValidAssignment() {
        Assignment assignment = new Assignment();
        assignment.setIdIntern     (ID_PRACTICANTE);
        assignment.setIdProyect    (ID_PROYECTO);
        assignment.setIdApplication(ID_SOLICITUD);
        assignment.setAssignmentDate(new Date());
        return assignment;
    }

    // ---------------------------------------------------------------

    @Test
    void save_withValidData_returnsOneRowAffected() throws Exception {
        Assignment assignment = buildValidAssignment();

        int result = dao.save(assignment);

        assertEquals(1, result);
    }

    @Test
    void save_withValidData_assignsGeneratedId() throws Exception {
        Assignment assignment = buildValidAssignment();

        dao.save(assignment);

        assertTrue(assignment.getIdAssignment() > 0);
    }

    @Test
    void save_thenGetById_returnsCorrectIntern() throws Exception {
        Assignment assignment = buildValidAssignment();
        dao.save(assignment);

        Assignment retrieved = dao.getById(assignment.getIdAssignment());

        assertNotNull(retrieved);
        assertEquals(ID_PRACTICANTE, retrieved.getIdIntern());
    }

    @Test
    void save_thenGetById_returnsCorrectProject() throws Exception {
        Assignment assignment = buildValidAssignment();
        dao.save(assignment);

        Assignment retrieved = dao.getById(assignment.getIdAssignment());

        assertEquals(ID_PROYECTO, retrieved.getIdProyect());
    }

    @Test
    void save_thenGetById_returnsCorrectApplication() throws Exception {
        Assignment assignment = buildValidAssignment();
        dao.save(assignment);

        Assignment retrieved = dao.getById(assignment.getIdAssignment());

        assertEquals(ID_SOLICITUD, retrieved.getIdApplication());
    }

    @Test
    void save_thenGetByIdIntern_returnsAssignment() throws Exception {
        Assignment assignment = buildValidAssignment();
        dao.save(assignment);

        Assignment retrieved = dao.getByIdIntern(ID_PRACTICANTE);

        assertNotNull(retrieved);
        assertEquals(ID_PRACTICANTE, retrieved.getIdIntern());
    }

    @Test
    void getByIdIntern_whenNoAssignment_returnsNull() throws Exception {
        // No se ha insertado ninguna asignación
        Assignment retrieved = dao.getByIdIntern(ID_PRACTICANTE);

        assertNull(retrieved);
    }

    @Test
    void save_thenGetByIdProject_returnsListWithOneElement() throws Exception {
        dao.save(buildValidAssignment());

        List<Assignment> byProject = dao.getByIdProject(ID_PROYECTO);

        assertEquals(1, byProject.size());
        assertEquals(ID_PROYECTO, byProject.get(0).getIdProyect());
    }

    @Test
    void save_thenGetAll_containsSavedAssignment() throws Exception {
        dao.save(buildValidAssignment());

        List<Assignment> all = dao.getAll();

        assertEquals(1, all.size());
    }

    @Test
    void save_samePracticanteTwice_throwsSQLException() throws Exception {
        dao.save(buildValidAssignment());

        // UNIQUE KEY uq_asig_practicante no permite dos asignaciones
        assertThrows(Exception.class, () -> dao.save(buildValidAssignment()),
                "Debe lanzar excepción: un practicante solo puede tener una asignación");
    }
}
