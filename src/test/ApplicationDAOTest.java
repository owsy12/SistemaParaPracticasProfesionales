import Logic.DAO.ApplicationDAO;
import Logic.DTOs.Application;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDAOTest extends BaseDAOTest {

    private static final String STATUS_REJECTED = "Rechazada";
    private static final int INVALID_INTERN_ID_ZERO = 0;
    private static final int NON_EXISTENT_ID = 9999;

    private final ApplicationDAO dao = new ApplicationDAO();

    private Application buildValidApplication(String status) {
        Application application = new Application();
        application.setIdIntern(ID_INTERN);
        application.setStatus(status);
        application.setApplicationDate(LocalDate.now());
        return application;
    }

    @Test
    void testCreateValidApplicationReturnsGeneratedId() throws Exception {
        int generatedId = dao.create(buildValidApplication(STATUS_PENDING));
        assertTrue(generatedId > 0);
    }

    @Test
    void testCreateApplicationWithZeroInternIdThrowsValidationException() {
        Application application = buildValidApplication(STATUS_PENDING);
        application.setIdIntern(INVALID_INTERN_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.create(application));
    }

    @Test
    void testFindByIdReturnsSupportApplication() throws Exception {
        Application retrieved = dao.findById(ID_APPLICATION);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdReturnsCorrectInternId() throws Exception {
        Application retrieved = dao.findById(ID_APPLICATION);
        assertEquals(ID_INTERN, retrieved.getIdIntern());
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        Application retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByInternReturnsAcceptedApplication() throws Exception {
        Application retrieved = dao.findByIntern(ID_INTERN);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByInternReturnsCorrectStatus() throws Exception {
        Application retrieved = dao.findByIntern(ID_INTERN);
        assertEquals(STATUS_ACCEPTED, retrieved.getStatus());
    }

    @Test
    void testFindByInternWithNonExistentInternReturnsNull() throws Exception {
        Application retrieved = dao.findByIntern(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllReturnsAtLeastOneElement() throws Exception {
        List<Application> all = dao.findAll();
        assertEquals(1, all.size());
    }

    @Test
    void testFindByStatusAcceptedReturnsSupportApplication() throws Exception {
        List<Application> accepted = dao.findByStatus(STATUS_ACCEPTED);
        assertEquals(1, accepted.size());
    }

    @Test
    void testFindByStatusRejectedReturnsEmptyList() throws Exception {
        List<Application> rejected = dao.findByStatus(STATUS_REJECTED);
        assertTrue(rejected.isEmpty());
    }

    @Test
    void testUpdateStatusReturnsTrue() throws Exception {
        boolean result = dao.updateStatus(ID_APPLICATION, STATUS_REJECTED);
        assertTrue(result);
    }

    @Test
    void testUpdateStatusPersistsNewStatus() throws Exception {
        dao.updateStatus(ID_APPLICATION, STATUS_REJECTED);
        Application retrieved = dao.findById(ID_APPLICATION);
        assertEquals(STATUS_REJECTED, retrieved.getStatus());
    }

    @Test
    void testUpdateStatusWithNonExistentIdReturnsFalse() throws Exception {
        boolean result = dao.updateStatus(NON_EXISTENT_ID, STATUS_REJECTED);
        assertFalse(result);
    }

    @Test
    void testFindActiveApplicationByInternWhenNonePendingReturnsNull() throws Exception {
        Application retrieved = dao.findActiveApplicationByIntern(ID_INTERN);
        assertNull(retrieved);
    }

    @Test
    void testCancelAcceptedByInternReturnsTrue() throws Exception {
        boolean result = dao.cancelAcceptedByIntern(ID_INTERN);
        assertTrue(result);
    }

    @Test
    void testCancelAcceptedByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.cancelAcceptedByIntern(INVALID_INTERN_ID_ZERO));
    }

    @Test
    void testCancelAcceptedByInternWithNoAcceptedReturnsFalse() throws Exception {
        dao.cancelAcceptedByIntern(ID_INTERN);
        boolean result = dao.cancelAcceptedByIntern(ID_INTERN);
        assertFalse(result);
    }
}
