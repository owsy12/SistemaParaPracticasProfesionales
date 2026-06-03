import DataAccess.DataBaseConnection;
import Logic.DAO.UserDAO;
import Logic.DTOs.User;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest extends BaseDAOTest {

    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -7;
    private static final int NON_EXISTENT_ID = 9999;
    private static final String EXPECTED_INTERN_MATRICULA = "S21013142";
    private static final String EXPECTED_INTERN_EMAIL = "ana.garcia@uv.mx";
    private static final String NON_EXISTENT_MATRICULA = "Z99999999";
    private static final String UPDATED_FIRST_NAME = "Ana María";
    private static final String STATUS_UPDATED = "Activo";
    private static final int STANDALONE_USER_ID = 80;
    private static final String STANDALONE_MATRICULA = "Y80808080";
    private static final String STANDALONE_EMAIL = "standalone@uv.mx";

    private UserDAO createDao() throws Exception {
        return new UserDAO();
    }

    private void insertStandaloneUser() throws Exception {
        try (Connection connection = DataBaseConnection.connectDatabase();
             Statement statement = connection.createStatement()) {
            statement.execute(
                "INSERT INTO usuario (id_usuario, matricula, nombre, apellido_paterno, " +
                "apellido_materno, contrasenia, correo) VALUES (" + STANDALONE_USER_ID +
                ", '" + STANDALONE_MATRICULA + "', 'Solo', 'Sin', 'Deps', '$2b$10$hashSolo'," +
                " '" + STANDALONE_EMAIL + "')"
            );
        }
    }

    @Test
    void testFindByIdReturnsSupportIntern() throws Exception {
        UserDAO dao = createDao();
        User retrieved = dao.findById(ID_INTERN);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdReturnsCorrectMatricula() throws Exception {
        UserDAO dao = createDao();
        User retrieved = dao.findById(ID_INTERN);
        assertEquals(EXPECTED_INTERN_MATRICULA, retrieved.getMatricula());
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() throws Exception {
        UserDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() throws Exception {
        UserDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        UserDAO dao = createDao();
        User retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllReturnsThreeSupportUsers() throws Exception {
        UserDAO dao = createDao();
        List<User> all = dao.findAll();
        assertEquals(3, all.size());
    }

    @Test
    void testFindByIdentifierWithMatriculaReturnsUser() throws Exception {
        UserDAO dao = createDao();
        User retrieved = dao.findByIdentifier(EXPECTED_INTERN_MATRICULA);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdentifierWithEmailReturnsUser() throws Exception {
        UserDAO dao = createDao();
        User retrieved = dao.findByIdentifier(EXPECTED_INTERN_EMAIL);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdentifierWithUnknownReturnsNull() throws Exception {
        UserDAO dao = createDao();
        User retrieved = dao.findByIdentifier(NON_EXISTENT_MATRICULA);
        assertNull(retrieved);
    }

    @Test
    void testFindByEmailReturnsCorrectMatricula() throws Exception {
        UserDAO dao = createDao();
        User retrieved = dao.findByEmail(EXPECTED_INTERN_EMAIL);
        assertEquals(EXPECTED_INTERN_MATRICULA, retrieved.getMatricula());
    }

    @Test
    void testUpdateUserReturnsTrue() throws Exception {
        UserDAO dao = createDao();
        User user = dao.findById(ID_INTERN);
        user.setFirstName(UPDATED_FIRST_NAME);
        user.setStatus(STATUS_UPDATED);
        boolean result = dao.update(user);
        assertTrue(result);
    }

    @Test
    void testUpdateUserPersistsNewFirstName() throws Exception {
        UserDAO dao = createDao();
        User user = dao.findById(ID_INTERN);
        user.setFirstName(UPDATED_FIRST_NAME);
        user.setStatus(STATUS_UPDATED);
        dao.update(user);
        User retrieved = dao.findById(ID_INTERN);
        assertEquals(UPDATED_FIRST_NAME, retrieved.getFirstName());
    }

    @Test
    void testDeleteWithZeroIdThrowsValidationException() throws Exception {
        UserDAO dao = createDao();
        assertThrows(ValidationException.class, () -> dao.delete(INVALID_ID_ZERO));
    }

    @Test
    void testDeleteStandaloneUserReturnsTrue() throws Exception {
        insertStandaloneUser();
        UserDAO dao = createDao();
        boolean result = dao.delete(STANDALONE_USER_ID);
        assertTrue(result);
    }

    @Test
    void testDeleteNonExistentUserReturnsFalse() throws Exception {
        UserDAO dao = createDao();
        boolean result = dao.delete(NON_EXISTENT_ID);
        assertFalse(result);
    }
}
