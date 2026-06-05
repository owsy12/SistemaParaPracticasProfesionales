import DataAccess.DataBaseConnection;
import Logic.DAO.ApplicationDAO;
import Logic.DTOs.Application;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationDAOTest extends BaseDAOTest {

    private final ApplicationDAO dao = new ApplicationDAO();

    private Application buildApplication(int idIntern, String status) {
        Application application = new Application();
        application.setIdIntern(idIntern);
        application.setStatus(status);
        return application;
    }

    @Test
    void testCreateValidApplicationReturnsPositiveId() throws Exception {
        int idIntern = persistInternUser();
        int generatedId = dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testCreateApplicationWithZeroInternIdThrowsValidationException() {
        Application application = buildApplication(TestConstants.INVALID_ID_ZERO, TestConstants.STATUS_PENDING);
        assertThrows(ValidationException.class, () -> dao.create(application));
    }

    @Test
    void testFindByIdAfterCreateReturnsNotNull() throws Exception {
        int idIntern = persistInternUser();
        int idApplication = dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        Application retrieved = dao.findById(idApplication);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        Application retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByInternAfterCreateReturnsLatestApplication() throws Exception {
        int idIntern = persistInternUser();
        dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        Application retrieved = dao.findByIntern(idIntern);
        assertNotNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws Exception {
        List<Application> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterCreateReturnsOneElement() throws Exception {
        int idIntern = persistInternUser();
        dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        List<Application> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testFindByStatusReturnsOneElement() throws Exception {
        int idIntern = persistInternUser();
        dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        List<Application> pending = dao.findByStatus(TestConstants.STATUS_PENDING);
        assertEquals(TestConstants.SINGLE_RESULT, pending.size());
    }

    @Test
    void testFindByStatusWithUnknownStatusReturnsEmptyList() throws Exception {
        int idIntern = persistInternUser();
        dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        List<Application> rejected = dao.findByStatus(TestConstants.STATUS_REJECTED);
        assertTrue(rejected.isEmpty());
    }

    @Test
    void testUpdateStatusReturnsTrue() throws Exception {
        int idIntern = persistInternUser();
        int idApplication = dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        boolean result = dao.updateStatus(idApplication, TestConstants.STATUS_ACCEPTED);
        assertTrue(result);
    }

    @Test
    void testFindActiveApplicationByInternReturnsApplication() throws Exception {
        int idIntern = persistInternUser();
        dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        Application retrieved = dao.findActiveApplicationByIntern(idIntern);
        assertNotNull(retrieved);
    }

    @Test
    void testCancelAcceptedByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.cancelAcceptedByIntern(TestConstants.INVALID_ID_ZERO));
    }

    private int persistInternUser() throws Exception {
        int idIntern;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idIntern = new UserTestDataBuilder()
                    .withRegistrationNumber("S55000001")
                    .withEmail("app.intern@uv.mx")
                    .withRole(TestConstants.ROLE_INTERN)
                    .persist(connection);
            new InternTestDataBuilder().withUserId(idIntern).persist(connection);
        }
        return idIntern;
    }
}
