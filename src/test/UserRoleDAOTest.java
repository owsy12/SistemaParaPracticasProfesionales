import DataAccess.DataBaseConnection;
import Logic.DAO.UserRoleDAO;
import Logic.DTOs.User;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleDAOTest extends BaseDAOTest {

    private static final int NEW_USER_ID = 50;
    private static final String STATUS_INACTIVE = "Inactivo";
    private static final String ROLE_ADMINISTRATOR = "Administrador";
    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -3;

    private final UserRoleDAO dao = new UserRoleDAO();

    private void insertSupportUser() throws Exception {
        try (Connection connection = DataBaseConnection.connectDatabase();
             Statement statement = connection.createStatement()) {
            statement.execute(
                "INSERT INTO usuario (id_usuario, matricula, nombre, apellido_paterno, " +
                "apellido_materno, contrasenia, correo) VALUES (" + NEW_USER_ID +
                ", 'X50000050', 'Iván', 'López', 'Soto', '$2b$10$hashIvan', 'ivan.lopez@uv.mx')"
            );
        }
    }

    private User buildUser(int id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    @Test
    void testSaveUserRoleWithZeroIdThrowsValidationException() {
        User user = buildUser(INVALID_ID_ZERO, ROLE_ADMINISTRATOR);
        assertThrows(ValidationException.class, () -> dao.saveUserRole(user));
    }

    @Test
    void testSaveUserRoleValidReturnsTrue() throws Exception {
        insertSupportUser();
        boolean result = dao.saveUserRole(buildUser(NEW_USER_ID, ROLE_ADMINISTRATOR));
        assertTrue(result);
    }

    @Test
    void testSaveDuplicateUserRoleThrowsDuplicateEntryException() throws Exception {
        assertThrows(DuplicateEntryException.class,
                () -> dao.saveUserRole(buildUser(ID_INTERN, ROLE_INTERN)));
    }

    @Test
    void testFindRolesByUserIdAfterSupportInsertReturnsOne() throws Exception {
        List<String> roles = dao.findRolesByUserId(ID_INTERN);
        assertEquals(1, roles.size());
    }

    @Test
    void testFindRolesByUserIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findRolesByUserId(INVALID_ID_ZERO));
    }

    @Test
    void testFindRolesByUserIdWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findRolesByUserId(INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindUsersByRoleReturnsSupportIntern() throws Exception {
        List<Map<String, Object>> users = dao.findUsersByRole(ROLE_INTERN);
        assertEquals(1, users.size());
    }

    @Test
    void testFindUsersByRoleWithUnknownRoleReturnsEmptyList() throws Exception {
        List<Map<String, Object>> users = dao.findUsersByRole(ROLE_ADMINISTRATOR);
        assertTrue(users.isEmpty());
    }

    @Test
    void testDeleteUserRoleReturnsTrue() throws Exception {
        boolean result = dao.deleteUserRole(ID_INTERN, ROLE_INTERN);
        assertTrue(result);
    }

    @Test
    void testDeleteUserRoleWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.deleteUserRole(INVALID_ID_ZERO, ROLE_INTERN));
    }

    @Test
    void testDeleteUserRoleWithNonExistentReturnsFalse() throws Exception {
        boolean result = dao.deleteUserRole(ID_INTERN, ROLE_ADMINISTRATOR);
        assertFalse(result);
    }

    @Test
    void testUpdateUserRoleStatusReturnsTrue() throws Exception {
        User user = buildUser(ID_INTERN, ROLE_INTERN);
        user.setStatus(STATUS_INACTIVE);
        boolean result = dao.updateUserRolStatus(user);
        assertTrue(result);
    }

    @Test
    void testUpdateUserRoleStatusWithZeroIdThrowsValidationException() {
        User user = buildUser(INVALID_ID_ZERO, ROLE_INTERN);
        user.setStatus(STATUS_INACTIVE);
        assertThrows(ValidationException.class, () -> dao.updateUserRolStatus(user));
    }

    @Test
    void testGetActiveRolsByUserIdReturnsOne() throws Exception {
        List<String> active = dao.getActiveRolsByUserId(ID_INTERN);
        assertEquals(1, active.size());
    }

    @Test
    void testGetActiveRolsByUserIdReturnsEmptyForInactivatedRole() throws Exception {
        User user = buildUser(ID_INTERN, ROLE_INTERN);
        user.setStatus(STATUS_INACTIVE);
        dao.updateUserRolStatus(user);
        List<String> active = dao.getActiveRolsByUserId(ID_INTERN);
        assertTrue(active.isEmpty());
    }
}
