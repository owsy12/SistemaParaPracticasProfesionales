import Logic.DAO.ActivityDAO;
import Logic.DTOs.Activity;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityDAOTest extends BaseDAOTest {

    private static final String ACTIVITY_NAME = "Levantamiento de requerimientos";
    private static final String ACTIVITY_NAME_SECOND = "Diseño de base de datos";
    private static final String ACTIVITY_DESCRIPTION = "Entrevistas iniciales con stakeholders.";
    private static final String ACTIVITY_STATUS_ACTIVE = "Activa";
    private static final String ACTIVITY_STATUS_INACTIVE = "Inactiva";
    private static final String UPDATED_ACTIVITY_NAME = "Levantamiento detallado";
    private static final String BLANK_NAME = "  ";
    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -1;
    private static final int NON_EXISTENT_ID = 9999;
    private static final LocalDate ACTIVITY_START = LocalDate.of(2025, 2, 1);
    private static final LocalDate ACTIVITY_END = LocalDate.of(2025, 2, 28);
    private static final int EXPECTED_TWO_ACTIVITIES = 2;

    private final ActivityDAO dao = new ActivityDAO();

    private Activity buildValidActivity(String name) {
        Activity activity = new Activity();
        activity.setIdProject(ID_PROJECT);
        activity.setName(name);
        activity.setDescription(ACTIVITY_DESCRIPTION);
        activity.setStatus(ACTIVITY_STATUS_ACTIVE);
        activity.setCreationDate(ACTIVITY_START);
        activity.setStartDate(ACTIVITY_START);
        activity.setEndDate(ACTIVITY_END);
        return activity;
    }

    @Test
    void testSaveValidActivityReturnsPositiveId() throws Exception {
        int generatedId = dao.save(buildValidActivity(ACTIVITY_NAME));
        assertTrue(generatedId > 0);
    }

    @Test
    void testSaveValidActivityAssignsIdToDto() throws Exception {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        dao.save(activity);
        assertTrue(activity.getIdActivity() > 0);
    }

    @Test
    void testSaveActivityWithZeroProjectIdThrowsValidationException() {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        activity.setIdProject(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(activity));
    }

    @Test
    void testSaveActivityWithBlankNameThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.save(buildValidActivity(BLANK_NAME)));
    }

    @Test
    void testSaveActivityWithNullNameThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.save(buildValidActivity(null)));
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws Exception {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        dao.save(activity);
        Activity retrieved = dao.findById(activity.getIdActivity());
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdAfterSaveReturnsCorrectName() throws Exception {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        dao.save(activity);
        Activity retrieved = dao.findById(activity.getIdActivity());
        assertEquals(ACTIVITY_NAME, retrieved.getName());
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        Activity retrieved = dao.findById(NON_EXISTENT_ID);
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
    void testFindByProjectAfterSavingTwoActivitiesReturnsTwo() throws Exception {
        dao.save(buildValidActivity(ACTIVITY_NAME));
        dao.save(buildValidActivity(ACTIVITY_NAME_SECOND));
        List<Activity> activities = dao.findByProject(ID_PROJECT);
        assertEquals(EXPECTED_TWO_ACTIVITIES, activities.size());
    }

    @Test
    void testFindByProjectWhenNoActivitiesReturnsEmptyList() throws Exception {
        List<Activity> activities = dao.findByProject(ID_PROJECT);
        assertTrue(activities.isEmpty());
    }

    @Test
    void testFindByProjectWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findByProject(INVALID_ID_ZERO));
    }

    @Test
    void testUpdateActivityReturnsTrue() throws Exception {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        dao.save(activity);
        activity.setName(UPDATED_ACTIVITY_NAME);
        boolean result = dao.update(activity);
        assertTrue(result);
    }

    @Test
    void testUpdateActivityPersistsNewName() throws Exception {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        dao.save(activity);
        activity.setName(UPDATED_ACTIVITY_NAME);
        dao.update(activity);
        Activity retrieved = dao.findById(activity.getIdActivity());
        assertEquals(UPDATED_ACTIVITY_NAME, retrieved.getName());
    }

    @Test
    void testUpdateActivityWithBlankNameThrowsValidationException() throws Exception {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        dao.save(activity);
        activity.setName(BLANK_NAME);
        assertThrows(ValidationException.class, () -> dao.update(activity));
    }

    @Test
    void testDeactivateActivityReturnsTrue() throws Exception {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        dao.save(activity);
        boolean result = dao.deactivate(activity.getIdActivity());
        assertTrue(result);
    }

    @Test
    void testDeactivateActivityPersistsInactiveStatus() throws Exception {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        dao.save(activity);
        dao.deactivate(activity.getIdActivity());
        Activity retrieved = dao.findById(activity.getIdActivity());
        assertEquals(ACTIVITY_STATUS_INACTIVE, retrieved.getStatus());
    }

    @Test
    void testDeactivateNonExistentActivityReturnsFalse() throws Exception {
        boolean result = dao.deactivate(NON_EXISTENT_ID);
        assertFalse(result);
    }

    @Test
    void testDeleteActivityReturnsTrue() throws Exception {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        dao.save(activity);
        boolean result = dao.delete(activity.getIdActivity());
        assertTrue(result);
    }

    @Test
    void testDeleteActivityRemovesRecord() throws Exception {
        Activity activity = buildValidActivity(ACTIVITY_NAME);
        dao.save(activity);
        dao.delete(activity.getIdActivity());
        Activity retrieved = dao.findById(activity.getIdActivity());
        assertNull(retrieved);
    }

    @Test
    void testDeleteWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.delete(INVALID_ID_ZERO));
    }
}
