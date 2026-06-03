import Logic.DAO.ProfessorDAO;
import Logic.DTOs.Professor;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfessorDAOTest extends BaseDAOTest {

    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -5;
    private static final int NON_EXISTENT_ID = 9999;
    private static final String EXPECTED_ACADEMIC_AREA = "Ingeniería de Software";

    private ProfessorDAO createDao() throws Exception {
        return new ProfessorDAO();
    }

    @Test
    void testFindByIdReturnsSupportProfessor() throws Exception {
        ProfessorDAO dao = createDao();
        Professor retrieved = dao.findById(ID_PROFESSOR);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdReturnsCorrectAcademicArea() throws Exception {
        ProfessorDAO dao = createDao();
        Professor retrieved = dao.findById(ID_PROFESSOR);
        assertEquals(EXPECTED_ACADEMIC_AREA, retrieved.getAcademicArea());
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() throws Exception {
        ProfessorDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() throws Exception {
        ProfessorDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        ProfessorDAO dao = createDao();
        Professor retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllReturnsSupportProfessor() throws Exception {
        ProfessorDAO dao = createDao();
        List<Professor> all = dao.findAll();
        assertEquals(1, all.size());
    }

    @Test
    void testFindProfessorsWithoutCoordinatorRoleReturnsSupportProfessor() throws Exception {
        ProfessorDAO dao = createDao();
        List<Professor> professors = dao.findProfessorsWithoutCoordinatorRole();
        assertEquals(1, professors.size());
    }

    @Test
    void testDeactivateProfessorWithZeroIdThrowsValidationException() throws Exception {
        ProfessorDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.deactivateProfessor(INVALID_ID_ZERO));
    }

    @Test
    void testDeactivateProfessorWithNegativeIdThrowsValidationException() throws Exception {
        ProfessorDAO dao = createDao();
        assertThrows(ValidationException.class,
                () -> dao.deactivateProfessor(INVALID_ID_NEGATIVE));
    }

    @Test
    void testDeactivateProfessorWithNonExistentIdReturnsFalse() throws Exception {
        ProfessorDAO dao = createDao();
        boolean result = dao.deactivateProfessor(NON_EXISTENT_ID);
        assertFalse(result);
    }
}
