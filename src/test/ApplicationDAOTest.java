import DataAccess.DataBaseConnection;
import Logic.DAO.ApplicationDAO;
import Logic.DTOs.Application;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
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
    void testCreateValidApplicationReturnsPositiveId() throws ServiceException, ValidationException {
        int idIntern = persistInternUser();
        int generatedId = dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testCreateApplicationWithZeroInternIdThrowsValidationException() {
        Application application = buildApplication(TestConstants.INVALID_ID_ZERO, TestConstants.STATUS_PENDING);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.create(application);
            }
        });
    }

    @Test
    void testFindByIdAfterCreateReturnsNotNull() throws ServiceException, ValidationException {
        int idIntern = persistInternUser();
        int idApplication = dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        Application retrieved = dao.findById(idApplication);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        Application retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByInternAfterCreateReturnsLatestApplication() throws ServiceException, ValidationException {
        int idIntern = persistInternUser();
        dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        Application retrieved = dao.findByIntern(idIntern);
        assertNotNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<Application> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterCreateReturnsOneElement() throws ServiceException, ValidationException {
        int idIntern = persistInternUser();
        dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        List<Application> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testFindByStatusReturnsOneElement() throws ServiceException, ValidationException {
        int idIntern = persistInternUser();
        dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        List<Application> pending = dao.findByStatus(TestConstants.STATUS_PENDING);
        assertEquals(TestConstants.SINGLE_RESULT, pending.size());
    }

    @Test
    void testFindByStatusWithUnknownStatusReturnsEmptyList() throws ServiceException, ValidationException {
        int idIntern = persistInternUser();
        dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        List<Application> rejected = dao.findByStatus(TestConstants.STATUS_REJECTED);
        assertTrue(rejected.isEmpty());
    }

    @Test
    void testUpdateStatusReturnsTrue() throws ServiceException, ValidationException {
        int idIntern = persistInternUser();
        int idApplication = dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        boolean result = dao.updateStatus(idApplication, TestConstants.STATUS_ACCEPTED);
        assertTrue(result);
    }

    @Test
    void testFindActiveApplicationByInternReturnsApplication() throws ServiceException, ValidationException {
        int idIntern = persistInternUser();
        dao.create(buildApplication(idIntern, TestConstants.STATUS_PENDING));
        Application retrieved = dao.findActiveApplicationByIntern(idIntern);
        assertNotNull(retrieved);
    }

    @Test
    void testCancelAcceptedByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.cancelAcceptedByIntern(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    private int persistInternUser() throws ServiceException, ValidationException {
        int idIntern;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idIntern = new UserTestDataBuilder()
                    .withRegistrationNumber("S55000001")
                    .withEmail("app.intern@uv.mx")
                    .withRole(TestConstants.ROLE_INTERN)
                    .persist(connection);
            new InternTestDataBuilder().withUserId(idIntern).persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idIntern;
    }
}
