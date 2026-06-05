import DataAccess.DataBaseConnection;
import Logic.DAO.InternDAO;
import Logic.DTOs.Intern;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternDAOTest extends BaseDAOTest {

    private static final String INTERN_REGISTRATION_NUMBER = "S30000001";
    private static final String INTERN_EMAIL = "intern.test@uv.mx";
    private static final String INTERN_FIRST_NAME = "Karen";
    private static final String INTERN_LAST_NAME = "Reyes";
    private static final int UPDATED_CREDITS = 280;

    private InternDAO buildDao() throws Exception {
        return new InternDAO();
    }

    private Intern buildIntern() {
        Intern intern = new Intern();
        intern.setRegistrationNumber(INTERN_REGISTRATION_NUMBER);
        intern.setFirstName(INTERN_FIRST_NAME);
        intern.setLastName(INTERN_LAST_NAME);
        intern.setSecondLastName(INTERN_LAST_NAME);
        intern.setPassword(TestConstants.DEFAULT_PASSWORD_HASH);
        intern.setEmail(INTERN_EMAIL);
        intern.setStatus(TestConstants.STATUS_ACTIVE_USER);
        intern.setRole(TestConstants.ROLE_INTERN);
        intern.setCredits(TestConstants.DEFAULT_INTERN_CREDITS);
        return intern;
    }

    @Test
    void testSaveValidInternReturnsTrue() throws Exception {
        InternDAO dao = buildDao();
        boolean result = dao.saveIntern(buildIntern());
        assertTrue(result);
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws Exception {
        int idUser = persistInternViaBuilders();
        InternDAO dao = buildDao();
        Intern retrieved = dao.findById(idUser);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() throws Exception {
        InternDAO dao = buildDao();
        assertThrows(ValidationException.class, () -> dao.findById(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        InternDAO dao = buildDao();
        Intern retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testDeactivateInternWithZeroIdThrowsValidationException() throws Exception {
        InternDAO dao = buildDao();
        assertThrows(ValidationException.class,
                () -> dao.deactivateIntern(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testDeactivateInternReturnsTrue() throws Exception {
        int idUser = persistInternViaBuilders();
        InternDAO dao = buildDao();
        boolean result = dao.deactivateIntern(idUser);
        assertTrue(result);
    }

    @Test
    void testUpdateCreditsReturnsTrue() throws Exception {
        int idUser = persistInternViaBuilders();
        InternDAO dao = buildDao();
        boolean result = dao.updateCredits(idUser, UPDATED_CREDITS);
        assertTrue(result);
    }

    @Test
    void testUpdateCreditsWithZeroIdThrowsValidationException() throws Exception {
        InternDAO dao = buildDao();
        assertThrows(ValidationException.class,
                () -> dao.updateCredits(TestConstants.INVALID_ID_ZERO, UPDATED_CREDITS));
    }

    @Test
    void testFindAllActiveInternsAfterPersistReturnsOneElement() throws Exception {
        persistInternViaBuilders();
        InternDAO dao = buildDao();
        List<Intern> active = dao.findAllActiveinterns();
        assertEquals(TestConstants.SINGLE_RESULT, active.size());
    }

    @Test
    void testFindByProjectWithZeroIdThrowsValidationException() throws Exception {
        InternDAO dao = buildDao();
        assertThrows(ValidationException.class,
                () -> dao.findByProject(TestConstants.INVALID_ID_ZERO));
    }

    private int persistInternViaBuilders() throws Exception {
        int idUser;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idUser = new UserTestDataBuilder()
                    .withRegistrationNumber(INTERN_REGISTRATION_NUMBER)
                    .withEmail(INTERN_EMAIL)
                    .withFirstName(INTERN_FIRST_NAME)
                    .withLastName(INTERN_LAST_NAME)
                    .withRole(TestConstants.ROLE_INTERN)
                    .persist(connection);
            new InternTestDataBuilder()
                    .withUserId(idUser)
                    .withCredits(TestConstants.DEFAULT_INTERN_CREDITS)
                    .persist(connection);
        }
        return idUser;
    }
}
