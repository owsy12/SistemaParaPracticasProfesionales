import DataAccess.DataBaseConnection;
import Logic.DAO.InternActivityDAO;
import Logic.DTOs.InternActivity;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternActivityDAOTest extends BaseDAOTest {

    private static final int UPDATED_HOURS = 12;

    private final InternActivityDAO dao = new InternActivityDAO();

    private InternActivity buildInternActivity(int idActivity, int idIntern) {
        InternActivity activity = new InternActivity();
        activity.setIdActivity(idActivity);
        activity.setIdIntern(idIntern);
        activity.setDedicatedHours(TestConstants.DEFAULT_DEDICATED_HOURS);
        activity.setStatus(TestConstants.STATUS_INTERN_ACTIVITY_IN_PROGRESS);
        activity.setCompletionDate(LocalDate.now());
        activity.setObservations(TestConstants.DEFAULT_OBSERVATIONS);
        return activity;
    }

    @Test
    void testSaveValidInternActivityReturnsPositiveId() throws Exception {
        InternActivityContext context = persistContext();
        int generatedId = dao.save(buildInternActivity(context.idActivity, context.idIntern));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveInternActivityWithZeroActivityIdThrowsValidationException() throws Exception {
        InternActivityContext context = persistContext();
        InternActivity activity = buildInternActivity(TestConstants.INVALID_ID_ZERO, context.idIntern);
        assertThrows(ValidationException.class, () -> dao.save(activity));
    }

    @Test
    void testSaveDuplicateInternActivityThrowsDuplicateEntryException() throws Exception {
        InternActivityContext context = persistContext();
        dao.save(buildInternActivity(context.idActivity, context.idIntern));
        assertThrows(DuplicateEntryException.class,
                () -> dao.save(buildInternActivity(context.idActivity, context.idIntern)));
    }

    @Test
    void testFindByActivityAndInternAfterSaveReturnsNotNull() throws Exception {
        InternActivityContext context = persistContext();
        dao.save(buildInternActivity(context.idActivity, context.idIntern));
        InternActivity retrieved = dao.findByActivityAndIntern(context.idActivity, context.idIntern);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByActivityAndInternWhenNoRecordReturnsNull() throws Exception {
        InternActivity retrieved = dao.findByActivityAndIntern(
                TestConstants.NON_EXISTENT_ID, TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByActivityAndInternWithZeroIdThrowsValidationException() throws Exception {
        InternActivityContext context = persistContext();
        assertThrows(ValidationException.class,
                () -> dao.findByActivityAndIntern(TestConstants.INVALID_ID_ZERO, context.idIntern));
    }

    @Test
    void testFindByInternAndProjectAfterSaveReturnsOneElement() throws Exception {
        InternActivityContext context = persistContext();
        dao.save(buildInternActivity(context.idActivity, context.idIntern));
        List<InternActivity> activities = dao.findByInternAndProject(context.idIntern, context.idProject);
        assertEquals(TestConstants.SINGLE_RESULT, activities.size());
    }

    @Test
    void testFindByInternAndProjectWithNoActivitiesReturnsEmptyList() throws Exception {
        InternActivityContext context = persistContext();
        List<InternActivity> activities = dao.findByInternAndProject(context.idIntern, context.idProject);
        assertTrue(activities.isEmpty());
    }

    @Test
    void testGetTotalHoursByInternAfterSaveReturnsDedicatedHours() throws Exception {
        InternActivityContext context = persistContext();
        dao.save(buildInternActivity(context.idActivity, context.idIntern));
        int total = dao.getTotalHoursByIntern(context.idIntern);
        assertEquals(TestConstants.DEFAULT_DEDICATED_HOURS, total);
    }

    @Test
    void testGetTotalHoursByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.getTotalHoursByIntern(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testUpdateInternActivityReturnsTrue() throws Exception {
        InternActivityContext context = persistContext();
        InternActivity activity = buildInternActivity(context.idActivity, context.idIntern);
        dao.save(activity);
        activity.setStatus(TestConstants.STATUS_INTERN_ACTIVITY_COMPLETED);
        activity.setDedicatedHours(UPDATED_HOURS);
        boolean result = dao.update(activity);
        assertTrue(result);
    }

    @Test
    void testUpdateInternActivityWithZeroIdThrowsValidationException() {
        InternActivity activity = buildInternActivity(
                TestConstants.NON_EXISTENT_ID, TestConstants.NON_EXISTENT_ID);
        activity.setIdInternActivity(TestConstants.INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.update(activity));
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws Exception {
        InternActivityContext context = persistContext();
        dao.save(buildInternActivity(context.idActivity, context.idIntern));
        boolean result = dao.deleteByInternAndProject(context.idIntern, context.idProject);
        assertTrue(result);
    }

    @Test
    void testDeleteByInternAndProjectWithZeroProjectIdThrowsValidationException() throws Exception {
        InternActivityContext context = persistContext();
        assertThrows(ValidationException.class,
                () -> dao.deleteByInternAndProject(context.idIntern, TestConstants.INVALID_ID_ZERO));
    }

    private InternActivityContext persistContext() throws Exception {
        InternActivityContext context = new InternActivityContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
            context.idActivity = new ActivityTestDataBuilder()
                    .withProjectId(scene.getProjectId())
                    .persist(connection);
        }
        return context;
    }

    private static final class InternActivityContext {
        int idIntern;
        int idProject;
        int idActivity;
    }
}
