import Logic.DAO.ProjectApplicationDAO;
import Logic.DTOs.ProjectApplication;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectApplicationDAOTest extends BaseDAOTest {

    private static final int PREFERENCE_ORDER_PRIMARY = 1;
    private static final int PREFERENCE_ORDER_SECONDARY = 2;
    private static final int INVALID_ID_ZERO = 0;
    private static final int NON_EXISTENT_ID = 9999;
    private static final int SUPPORT_PROJECT_APPLICATION_ID = 1;

    private final ProjectApplicationDAO dao = new ProjectApplicationDAO();

    private ProjectApplication buildValidProjectApplication(int preferenceOrder) {
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setIdApplication(ID_APPLICATION);
        projectApplication.setIdProyect(ID_PROJECT);
        projectApplication.setPreferenceOrder(preferenceOrder);
        return projectApplication;
    }

    @Test
    void testCreateValidProjectApplicationReturnsTrue() throws Exception {
        boolean result = dao.create(buildValidProjectApplication(PREFERENCE_ORDER_SECONDARY));
        assertTrue(result);
    }

    @Test
    void testCreateWithZeroApplicationIdThrowsValidationException() {
        ProjectApplication projectApplication = buildValidProjectApplication(PREFERENCE_ORDER_PRIMARY);
        projectApplication.setIdApplication(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.create(projectApplication));
    }

    @Test
    void testCreateWithZeroProjectIdThrowsValidationException() {
        ProjectApplication projectApplication = buildValidProjectApplication(PREFERENCE_ORDER_PRIMARY);
        projectApplication.setIdProyect(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.create(projectApplication));
    }

    @Test
    void testFindByIdReturnsSupportProjectApplication() throws Exception {
        ProjectApplication retrieved = dao.findById(SUPPORT_PROJECT_APPLICATION_ID);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdReturnsCorrectApplicationId() throws Exception {
        ProjectApplication retrieved = dao.findById(SUPPORT_PROJECT_APPLICATION_ID);
        assertEquals(ID_APPLICATION, retrieved.getIdApplication());
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        ProjectApplication retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByApplicationReturnsSupportApplication() throws Exception {
        List<ProjectApplication> options = dao.findByApplication(ID_APPLICATION);
        assertEquals(1, options.size());
    }

    @Test
    void testFindByApplicationWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findByApplication(INVALID_ID_ZERO));
    }

    @Test
    void testFindAllReturnsAtLeastOneElement() throws Exception {
        List<ProjectApplication> all = dao.findAll();
        assertEquals(1, all.size());
    }

    @Test
    void testDeleteExistingReturnsTrue() throws Exception {
        boolean result = dao.delete(SUPPORT_PROJECT_APPLICATION_ID);
        assertTrue(result);
    }

    @Test
    void testDeleteNonExistentReturnsFalse() throws Exception {
        boolean result = dao.delete(NON_EXISTENT_ID);
        assertFalse(result);
    }

    @Test
    void testDeleteWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.delete(INVALID_ID_ZERO));
    }

    @Test
    void testFindProjectIdsByInternReturnsListWithSupportProject() throws Exception {
        List<Integer> projectIds = dao.findProjectIdsByIntern(ID_INTERN);
        assertEquals(1, projectIds.size());
    }

    @Test
    void testFindProjectIdsByInternReturnsCorrectProjectId() throws Exception {
        List<Integer> projectIds = dao.findProjectIdsByIntern(ID_INTERN);
        assertEquals(ID_PROJECT, projectIds.get(0).intValue());
    }

    @Test
    void testFindProjectIdsByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findProjectIdsByIntern(INVALID_ID_ZERO));
    }
}
