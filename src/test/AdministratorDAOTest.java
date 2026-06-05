import DataAccess.DataBaseConnection;
import Logic.DAO.AdministratorDAO;
import Logic.DTOs.Administrator;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdministratorDAOTest extends BaseDAOTest {

    private static final String ADMIN_REGISTRATION_NUMBER = "A60000001";
    private static final String ADMIN_EMAIL = "admin.test@uv.mx";
    private static final String ADMIN_FIRST_NAME = "Rosa";
    private static final String ADMIN_LAST_NAME = "Mendoza";

    private final AdministratorDAO dao = new AdministratorDAO();

    private Administrator buildAdministrator(int idUser) {
        Administrator administrator = new Administrator();
        administrator.setId(idUser);
        administrator.setRegistrationNumber(ADMIN_REGISTRATION_NUMBER);
        administrator.setFirstName(ADMIN_FIRST_NAME);
        administrator.setLastName(ADMIN_LAST_NAME);
        administrator.setSecondLastName(ADMIN_LAST_NAME);
        administrator.setPassword(TestConstants.DEFAULT_PASSWORD_HASH);
        administrator.setEmail(ADMIN_EMAIL);
        administrator.setStatus(TestConstants.STATUS_ACTIVE_USER);
        return administrator;
    }

    @Test
    void testSaveValidAdministratorReturnsTrue() throws Exception {
        int idUser = persistUserOnly();
        boolean result = dao.saveAdmin(buildAdministrator(idUser));
        assertTrue(result);
    }

    @Test
    void testSaveAdministratorWithZeroIdThrowsValidationException() {
        Administrator administrator = buildAdministrator(TestConstants.INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.saveAdmin(administrator));
    }

    private int persistUserOnly() throws Exception {
        int idUser;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idUser = new UserTestDataBuilder()
                    .withRegistrationNumber(ADMIN_REGISTRATION_NUMBER)
                    .withEmail(ADMIN_EMAIL)
                    .withFirstName(ADMIN_FIRST_NAME)
                    .withLastName(ADMIN_LAST_NAME)
                    .withRole(TestConstants.ROLE_ADMINISTRATOR)
                    .persist(connection);
        }
        return idUser;
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws Exception {
        int idUser = persistAdministratorViaBuilders();
        Administrator retrieved = dao.findById(idUser);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdAfterSaveReturnsCorrectRegistrationNumber() throws Exception {
        int idUser = persistAdministratorViaBuilders();
        Administrator retrieved = dao.findById(idUser);
        assertEquals(ADMIN_REGISTRATION_NUMBER, retrieved.getRegistrationNumber());
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findById(TestConstants.INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        Administrator retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws Exception {
        List<Administrator> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterSaveReturnsOneElement() throws Exception {
        persistAdministratorViaBuilders();
        List<Administrator> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    private int persistAdministratorViaBuilders() throws Exception {
        int idUser;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idUser = new UserTestDataBuilder()
                    .withRegistrationNumber(ADMIN_REGISTRATION_NUMBER)
                    .withEmail(ADMIN_EMAIL)
                    .withFirstName(ADMIN_FIRST_NAME)
                    .withLastName(ADMIN_LAST_NAME)
                    .withRole(TestConstants.ROLE_ADMINISTRATOR)
                    .persist(connection);
            new AdministratorTestDataBuilder().withUserId(idUser).persist(connection);
        }
        return idUser;
    }
}
