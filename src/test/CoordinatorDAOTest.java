package test.Logic;

import Logic.DAO.CoordinatorDAO;
import Logic.DTOs.Coordinator;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoordinatorDAOTest extends BaseDAOTest {

    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -1;
    private static final int NON_EXISTENT_ID = 9999;
    private static final String NEW_MATRICULA = "C90909090";
    private static final String NEW_FIRST_NAME = "Norma";
    private static final String NEW_LAST_NAME = "Vidal";
    private static final String NEW_SECOND_LAST_NAME = "Cruz";
    private static final String NEW_PASSWORD = "$2b$10$hashNorma";
    private static final String NEW_EMAIL = "norma.vidal@uv.mx";

    private CoordinatorDAO createDao() throws Exception {
        return new CoordinatorDAO();
    }

    private Coordinator buildCoordinatorDto() {
        Coordinator coordinator = new Coordinator();
        coordinator.setMatricula(NEW_MATRICULA);
        coordinator.setFirstName(NEW_FIRST_NAME);
        coordinator.setLastName(NEW_LAST_NAME);
        coordinator.setSecondLastName(NEW_SECOND_LAST_NAME);
        coordinator.setPassword(NEW_PASSWORD);
        coordinator.setEmail(NEW_EMAIL);
        coordinator.setRole(ROLE_COORDINATOR);
        return coordinator;
    }

    @Test
    void testSaveCoordinatorWhenAnotherIsActiveThrowsValidationException() throws Exception {
        CoordinatorDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.save(buildCoordinatorDto()));
    }

    @Test
    void testFindByIdReturnsSupportCoordinator() throws Exception {
        CoordinatorDAO dao = createDao();
        Coordinator retrieved = dao.findById(ID_COORDINATOR);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdReturnsCorrectId() throws Exception {
        CoordinatorDAO dao = createDao();
        Coordinator retrieved = dao.findById(ID_COORDINATOR);
        assertEquals(ID_COORDINATOR, retrieved.getId());
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() throws Exception {
        CoordinatorDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() throws Exception {
        CoordinatorDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        CoordinatorDAO dao = createDao();
        Coordinator retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllCoordinatorsReturnsSupportCoordinator() throws Exception {
        CoordinatorDAO dao = createDao();
        List<Coordinator> all = dao.findAllCoordinators();
        assertEquals(1, all.size());
    }

    @Test
    void testFindCoordinatorsWithoutProfessorRoleReturnsSupportCoordinator() throws Exception {
        CoordinatorDAO dao = createDao();
        List<Coordinator> coordinators = dao.findCoordinatorsWithoutProfessorRole();
        assertEquals(1, coordinators.size());
    }

    @Test
    void testFindActiveCoordinatorsReturnsSupportCoordinator() throws Exception {
        CoordinatorDAO dao = createDao();
        List<Coordinator> coordinators = dao.findActiveCoordinators();
        assertEquals(1, coordinators.size());
    }

    @Test
    void testDeleteWithZeroIdThrowsValidationException() throws Exception {
        CoordinatorDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.delete(INVALID_ID_ZERO));
    }

    @Test
    void testDeleteWithNegativeIdThrowsValidationException() throws Exception {
        CoordinatorDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.delete(INVALID_ID_NEGATIVE));
    }

    @Test
    void testDeleteSupportCoordinatorReturnsTrue() throws Exception {
        CoordinatorDAO dao = createDao();
        boolean result = dao.delete(ID_COORDINATOR);
        assertTrue(result);
    }
}
