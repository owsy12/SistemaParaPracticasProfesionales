import DataAccess.DataBaseConnection;
import Logic.DAO.CoordinatorDAO;
import Logic.DTOs.Coordinator;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoordinatorDAOTest extends BaseDAOTest {

    private static final String COORD_REGISTRATION_NUMBER = "C70000001";
    private static final String COORD_EMAIL = "coord.test@uv.mx";
    private static final String COORD_FIRST_NAME = "Pedro";
    private static final String COORD_LAST_NAME = "Silva";

    private CoordinatorDAO buildDao() throws Exception {
        return new CoordinatorDAO();
    }

    private Coordinator buildCoordinator() {
        Coordinator coordinator = new Coordinator();
        coordinator.setRegistrationNumber(COORD_REGISTRATION_NUMBER);
        coordinator.setFirstName(COORD_FIRST_NAME);
        coordinator.setLastName(COORD_LAST_NAME);
        coordinator.setSecondLastName(COORD_LAST_NAME);
        coordinator.setPassword(TestConstants.DEFAULT_PASSWORD_HASH);
        coordinator.setEmail(COORD_EMAIL);
        coordinator.setStatus(TestConstants.STATUS_ACTIVE_USER);
        coordinator.setRole(TestConstants.ROLE_COORDINATOR);
        return coordinator;
    }

    @Test
    void testSaveValidCoordinatorReturnsTrue() throws Exception {
        CoordinatorDAO dao = buildDao();
        boolean result = dao.save(buildCoordinator());
        assertTrue(result);
    }

    @Test
    void testSaveSecondActiveCoordinatorThrowsValidationException() throws Exception {
        CoordinatorDAO dao = buildDao();
        persistCoordinatorViaBuilders();
        Coordinator secondCoordinator = buildCoordinator();
        secondCoordinator.setRegistrationNumber("C70000002");
        secondCoordinator.setEmail("coord2.test@uv.mx");
        assertThrows(ValidationException.class, () -> dao.save(secondCoordinator));
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws Exception {
        int idUser = persistCoordinatorViaBuilders();
        CoordinatorDAO dao = buildDao();
        Coordinator retrieved = dao.findById(idUser);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() throws Exception {
        CoordinatorDAO dao = buildDao();
        assertThrows(ValidationException.class, () -> dao.findById(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() throws Exception {
        CoordinatorDAO dao = buildDao();
        assertThrows(ValidationException.class,
                () -> dao.findById(TestConstants.INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        CoordinatorDAO dao = buildDao();
        Coordinator retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllCoordinatorsWithNoDataReturnsEmptyList() throws Exception {
        CoordinatorDAO dao = buildDao();
        List<Coordinator> all = dao.findAllCoordinators();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllCoordinatorsAfterPersistReturnsOneElement() throws Exception {
        persistCoordinatorViaBuilders();
        CoordinatorDAO dao = buildDao();
        List<Coordinator> all = dao.findAllCoordinators();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testFindActiveCoordinatorsReturnsOnlyActive() throws Exception {
        persistCoordinatorViaBuilders();
        CoordinatorDAO dao = buildDao();
        List<Coordinator> active = dao.findActiveCoordinators();
        assertEquals(TestConstants.SINGLE_RESULT, active.size());
    }

    @Test
    void testDeleteCoordinatorWithZeroIdThrowsValidationException() throws Exception {
        CoordinatorDAO dao = buildDao();
        assertThrows(ValidationException.class, () -> dao.delete(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testDeleteCoordinatorReturnsTrue() throws Exception {
        int idUser = persistCoordinatorViaBuilders();
        CoordinatorDAO dao = buildDao();
        boolean result = dao.delete(idUser);
        assertTrue(result);
    }

    private int persistCoordinatorViaBuilders() throws Exception {
        int idUser;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idUser = new UserTestDataBuilder()
                    .withRegistrationNumber(COORD_REGISTRATION_NUMBER)
                    .withEmail(COORD_EMAIL)
                    .withFirstName(COORD_FIRST_NAME)
                    .withLastName(COORD_LAST_NAME)
                    .withRole(TestConstants.ROLE_COORDINATOR)
                    .persist(connection);
            new CoordinatorTestDataBuilder().withUserId(idUser).persist(connection);
        }
        return idUser;
    }
}
