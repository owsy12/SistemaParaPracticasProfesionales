import DataAccess.DataBaseConnection;
import Logic.DAO.AdministratorDAO;
import Logic.DTOs.Administrator;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdministratorDAOTest extends BaseDAOTest {

    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -3;
    private static final int NON_EXISTENT_ID = 9999;
    private static final int NEW_ADMINISTRATOR_ID = 10;

    private final AdministratorDAO dao = new AdministratorDAO();

    private void insertAdministratorUser(int idUser, String matricula, String email) throws Exception {
        try (Connection connection = DataBaseConnection.connectDatabase();
             Statement statement = connection.createStatement()) {
            statement.execute(
                "INSERT INTO usuario (id_usuario, matricula, nombre, apellido_paterno, " +
                "apellido_materno, contrasenia, correo) VALUES (" + idUser + ", '" + matricula +
                "', 'Admin', 'Root', 'User', '$2b$10$hashAdmin', '" + email + "')"
            );
        }
    }

    private Administrator buildAdministrator(int idUser) {
        Administrator administrator = new Administrator();
        administrator.setId(idUser);
        return administrator;
    }

    @Test
    void testSaveValidAdministratorReturnsTrue() throws Exception {
        insertAdministratorUser(NEW_ADMINISTRATOR_ID, "A10000010", "admin10@uv.mx");
        boolean result = dao.saveAdmin(buildAdministrator(NEW_ADMINISTRATOR_ID));
        assertTrue(result);
    }

    @Test
    void testSaveAdministratorWithIdZeroThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.saveAdmin(buildAdministrator(INVALID_ID_ZERO)));
    }

    @Test
    void testSaveAdministratorWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.saveAdmin(buildAdministrator(INVALID_ID_NEGATIVE)));
    }

    @Test
    void testSaveDuplicateAdministratorThrowsDuplicateEntryException() throws Exception {
        insertAdministratorUser(NEW_ADMINISTRATOR_ID, "A10000010", "admin10@uv.mx");
        dao.saveAdmin(buildAdministrator(NEW_ADMINISTRATOR_ID));
        assertThrows(DuplicateEntryException.class,
                () -> dao.saveAdmin(buildAdministrator(NEW_ADMINISTRATOR_ID)));
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws Exception {
        insertAdministratorUser(NEW_ADMINISTRATOR_ID, "A10000010", "admin10@uv.mx");
        dao.saveAdmin(buildAdministrator(NEW_ADMINISTRATOR_ID));
        Administrator retrieved = dao.findById(NEW_ADMINISTRATOR_ID);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdAfterSaveReturnsCorrectId() throws Exception {
        insertAdministratorUser(NEW_ADMINISTRATOR_ID, "A10000010", "admin10@uv.mx");
        dao.saveAdmin(buildAdministrator(NEW_ADMINISTRATOR_ID));
        Administrator retrieved = dao.findById(NEW_ADMINISTRATOR_ID);
        assertEquals(NEW_ADMINISTRATOR_ID, retrieved.getId());
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        Administrator retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindAllWhenEmptyReturnsEmptyList() throws Exception {
        List<Administrator> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterSaveReturnsOneElement() throws Exception {
        insertAdministratorUser(NEW_ADMINISTRATOR_ID, "A10000010", "admin10@uv.mx");
        dao.saveAdmin(buildAdministrator(NEW_ADMINISTRATOR_ID));
        List<Administrator> all = dao.findAll();
        assertEquals(1, all.size());
    }
}
