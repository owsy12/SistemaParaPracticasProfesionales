import DataAccess.DataBaseConnection;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.User;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRoleDAOTest extends BaseDAOTest {

    private static final String NEW_USER_REGISTRATION_NUMBER = "X50000001";
    private static final String NEW_USER_EMAIL = "rol.test@uv.mx";

    private final UserRoleDAO userRoleDAO = new UserRoleDAO();

    private User buildUserWithIdAndRole(int idUser, String role) {
        User user = new User();
        user.setIdUser(idUser);
        user.setRole(role);
        user.setStatus(TestConstants.STATUS_ACTIVE_USER);
        return user;
    }

    @Test
    void testSaveUserRoleWithZeroIdThrowsValidationException() {
        User user = buildUserWithIdAndRole(TestConstants.INVALID_ID_ZERO, TestConstants.ROLE_ADMINISTRATOR);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                userRoleDAO.saveUserRole(user);
            }
        });
    }

    @Test
    void testSaveUserRoleValidReturnsTrue() throws ServiceException, ValidationException {
        int idUser = persistStandaloneUser();
        boolean result = userRoleDAO.saveUserRole(buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR));
        assertTrue(result);
    }

    @Test
    void testSaveDuplicateUserRoleThrowsDuplicateEntryException() throws ServiceException, ValidationException {
        int idUser = persistStandaloneUser();
        userRoleDAO.saveUserRole(buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR));
        assertThrows(DuplicateEntryException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                userRoleDAO.saveUserRole(buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR));
            }
        });
    }

    @Test
    void testFindRolesByUserIdAfterSaveReturnsOneRole() throws ServiceException, ValidationException {
        int idUser = persistStandaloneUser();
        userRoleDAO.saveUserRole(buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR));
        List<String> roles = userRoleDAO.findRolesByUserId(idUser);
        assertEquals(TestConstants.SINGLE_RESULT, roles.size());
    }

    @Test
    void testFindRolesByUserIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                userRoleDAO.findRolesByUserId(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testFindRolesByUserIdWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                userRoleDAO.findRolesByUserId(TestConstants.INVALID_ID_NEGATIVE);
            }
        });
    }

    @Test
    void testFindUsersByRoleReturnsOneUser() throws ServiceException, ValidationException {
        int idUser = persistStandaloneUser();
        userRoleDAO.saveUserRole(buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR));
        List<Map<String, Object>> users = userRoleDAO.findUsersByRole(TestConstants.ROLE_ADMINISTRATOR);
        assertEquals(TestConstants.SINGLE_RESULT, users.size());
    }

    @Test
    void testFindUsersByRoleWithUnusedRoleReturnsEmptyList() throws ServiceException, ValidationException {
        List<Map<String, Object>> users = userRoleDAO.findUsersByRole(TestConstants.ROLE_ADMINISTRATOR);
        assertTrue(users.isEmpty());
    }

    @Test
    void testDeleteUserRoleReturnsTrue() throws ServiceException, ValidationException {
        int idUser = persistStandaloneUser();
        userRoleDAO.saveUserRole(buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR));
        boolean result = userRoleDAO.deleteUserRole(idUser, TestConstants.ROLE_ADMINISTRATOR);
        assertTrue(result);
    }

    @Test
    void testDeleteUserRoleWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                userRoleDAO.deleteUserRole(TestConstants.INVALID_ID_ZERO, TestConstants.ROLE_INTERN);
            }
        });
    }

    @Test
    void testDeleteNonExistentUserRoleReturnsFalse() throws ServiceException, ValidationException {
        int idUser = persistStandaloneUser();
        boolean result = userRoleDAO.deleteUserRole(idUser, TestConstants.ROLE_ADMINISTRATOR);
        assertFalse(result);
    }

    @Test
    void testUpdateUserRoleStatusReturnsTrue() throws ServiceException, ValidationException {
        int idUser = persistStandaloneUser();
        userRoleDAO.saveUserRole(buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR));
        User user = buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR);
        user.setStatus(TestConstants.STATUS_INACTIVE_USER);
        boolean result = userRoleDAO.updateUserRolStatus(user);
        assertTrue(result);
    }

    @Test
    void testUpdateUserRoleStatusWithZeroIdThrowsValidationException() {
        User user = buildUserWithIdAndRole(TestConstants.INVALID_ID_ZERO, TestConstants.ROLE_INTERN);
        user.setStatus(TestConstants.STATUS_INACTIVE_USER);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                userRoleDAO.updateUserRolStatus(user);
            }
        });
    }

    @Test
    void testGetActiveRolesByUserIdReturnsOneActiveRole() throws ServiceException, ValidationException {
        int idUser = persistStandaloneUser();
        userRoleDAO.saveUserRole(buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR));
        List<String> active = userRoleDAO.getActiveRolsByUserId(idUser);
        assertEquals(TestConstants.SINGLE_RESULT, active.size());
    }

    @Test
    void testGetActiveRolesByUserIdReturnsEmptyForInactiveRole() throws ServiceException, ValidationException {
        int idUser = persistStandaloneUser();
        userRoleDAO.saveUserRole(buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR));
        User user = buildUserWithIdAndRole(idUser, TestConstants.ROLE_ADMINISTRATOR);
        user.setStatus(TestConstants.STATUS_INACTIVE_USER);
        userRoleDAO.updateUserRolStatus(user);
        List<String> active = userRoleDAO.getActiveRolsByUserId(idUser);
        assertTrue(active.isEmpty());
    }

    private int persistStandaloneUser() throws ServiceException, ValidationException {
        int idUser;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idUser = new UserTestDataBuilder()
                    .withRegistrationNumber(NEW_USER_REGISTRATION_NUMBER)
                    .withEmail(NEW_USER_EMAIL)
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idUser;
    }
}
