import DataAccess.DataBaseConnection;
import Logic.DAO.UserDAO;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDAOTest extends BaseDAOTest {

    private static final String NEW_USER_REGISTRATION_NUMBER = "S40000001";
    private static final String NEW_USER_EMAIL = "nuevo.usuario@uv.mx";
    private static final String NEW_USER_FIRST_NAME = "Mario";
    private static final String NEW_USER_LAST_NAME = "Hernández";
    private static final String UPDATED_FIRST_NAME = "Mario Antonio";
    private static final String NON_EXISTENT_REGISTRATION_NUMBER = "Z99999999";

    private User buildUser(String registrationNumber, String email) {
        User user = new User();
        user.setRegistrationNumber(registrationNumber);
        user.setFirstName(NEW_USER_FIRST_NAME);
        user.setLastName(NEW_USER_LAST_NAME);
        user.setSecondLastName(NEW_USER_LAST_NAME);
        user.setPassword(TestConstants.DEFAULT_PASSWORD_HASH);
        user.setEmail(email);
        user.setStatus(TestConstants.STATUS_ACTIVE_USER);
        user.setRole(TestConstants.ROLE_INTERN);
        return user;
    }

    private UserDAO buildDao() throws ServiceException {
        return new UserDAO();
    }

    @Test
    void testSaveValidUserAssignsGeneratedId() throws Exception {
        UserDAO dao = buildDao();
        int generatedId = dao.saveUser(buildUser(NEW_USER_REGISTRATION_NUMBER, NEW_USER_EMAIL));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testFindByIdAfterSaveReturnsCorrectRegistrationNumber() throws Exception {
        UserDAO dao = buildDao();
        int generatedId = dao.saveUser(buildUser(NEW_USER_REGISTRATION_NUMBER, NEW_USER_EMAIL));
        User retrieved = dao.findById(generatedId);
        assertEquals(NEW_USER_REGISTRATION_NUMBER, retrieved.getRegistrationNumber());
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() throws Exception {
        UserDAO dao = buildDao();
        assertThrows(ValidationException.class, () -> dao.findById(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() throws Exception {
        UserDAO dao = buildDao();
        assertThrows(ValidationException.class,
                () -> dao.findById(TestConstants.INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        UserDAO dao = buildDao();
        User retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws Exception {
        UserDAO dao = buildDao();
        List<User> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterSaveReturnsOneElement() throws Exception {
        UserDAO dao = buildDao();
        dao.saveUser(buildUser(NEW_USER_REGISTRATION_NUMBER, NEW_USER_EMAIL));
        List<User> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testFindByIdentifierByRegistrationNumberReturnsUser() throws Exception {
        UserDAO dao = buildDao();
        dao.saveUser(buildUser(NEW_USER_REGISTRATION_NUMBER, NEW_USER_EMAIL));
        User retrieved = dao.findByIdentifier(NEW_USER_REGISTRATION_NUMBER);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdentifierByEmailReturnsUser() throws Exception {
        UserDAO dao = buildDao();
        dao.saveUser(buildUser(NEW_USER_REGISTRATION_NUMBER, NEW_USER_EMAIL));
        User retrieved = dao.findByIdentifier(NEW_USER_EMAIL);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdentifierWithUnknownReturnsNull() throws Exception {
        UserDAO dao = buildDao();
        User retrieved = dao.findByIdentifier(NON_EXISTENT_REGISTRATION_NUMBER);
        assertNull(retrieved);
    }

    @Test
    void testFindByEmailReturnsCorrectRegistrationNumber() throws Exception {
        UserDAO dao = buildDao();
        dao.saveUser(buildUser(NEW_USER_REGISTRATION_NUMBER, NEW_USER_EMAIL));
        User retrieved = dao.findByEmail(NEW_USER_EMAIL);
        assertEquals(NEW_USER_REGISTRATION_NUMBER, retrieved.getRegistrationNumber());
    }

    @Test
    void testUpdateUserReturnsTrue() throws Exception {
        UserDAO dao = buildDao();
        int generatedId = dao.saveUser(buildUser(NEW_USER_REGISTRATION_NUMBER, NEW_USER_EMAIL));
        User user = dao.findById(generatedId);
        user.setFirstName(UPDATED_FIRST_NAME);
        user.setStatus(TestConstants.STATUS_ACTIVE_USER);
        boolean result = dao.update(user);
        assertTrue(result);
    }

    @Test
    void testUpdateUserPersistsNewFirstName() throws Exception {
        UserDAO dao = buildDao();
        int generatedId = dao.saveUser(buildUser(NEW_USER_REGISTRATION_NUMBER, NEW_USER_EMAIL));
        User user = dao.findById(generatedId);
        user.setFirstName(UPDATED_FIRST_NAME);
        user.setStatus(TestConstants.STATUS_ACTIVE_USER);
        dao.update(user);
        User retrieved = dao.findById(generatedId);
        assertEquals(UPDATED_FIRST_NAME, retrieved.getFirstName());
    }

    @Test
    void testDeleteUserReturnsTrue() throws Exception {
        UserDAO dao = buildDao();
        int generatedId = persistStandaloneUser();
        boolean result = dao.delete(generatedId);
        assertTrue(result);
    }

    @Test
    void testDeleteUserRemovesRecord() throws Exception {
        UserDAO dao = buildDao();
        int generatedId = persistStandaloneUser();
        dao.delete(generatedId);
        User retrieved = dao.findById(generatedId);
        assertNull(retrieved);
    }

    @Test
    void testDeleteUserWithZeroIdThrowsValidationException() throws Exception {
        UserDAO dao = buildDao();
        assertThrows(ValidationException.class, () -> dao.delete(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testDeleteNonExistentUserReturnsFalse() throws Exception {
        UserDAO dao = buildDao();
        boolean result = dao.delete(TestConstants.NON_EXISTENT_ID);
        assertFalse(result);
    }

    private int persistStandaloneUser() throws Exception {
        int generatedId;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            generatedId = new UserTestDataBuilder()
                    .withRegistrationNumber(NEW_USER_REGISTRATION_NUMBER)
                    .withEmail(NEW_USER_EMAIL)
                    .persist(connection);
        }
        return generatedId;
    }
}
