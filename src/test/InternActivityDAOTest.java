import DataAccess.DataBaseConnection;
import Logic.DAO.InternActivityDAO;
import Logic.DTOs.InternActivity;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternActivityDAOTest extends BaseDAOTest {

    private static final int UPDATED_HOURS = 12;

    private final InternActivityDAO internActivityDAO = new InternActivityDAO();

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
    void testSaveValidInternActivityReturnsPositiveId() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        int generatedId = internActivityDAO.save(buildInternActivity(context.idActivity, context.idIntern));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveInternActivityWithZeroActivityIdThrowsValidationException() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        InternActivity activity = buildInternActivity(TestConstants.INVALID_ID_ZERO, context.idIntern);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                internActivityDAO.save(activity);
            }
        });
    }

    @Test
    void testSaveDuplicateInternActivityThrowsDuplicateEntryException() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        internActivityDAO.save(buildInternActivity(context.idActivity, context.idIntern));
        assertThrows(DuplicateEntryException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                internActivityDAO.save(buildInternActivity(context.idActivity, context.idIntern));
            }
        });
    }

    @Test
    void testFindByActivityAndInternAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        internActivityDAO.save(buildInternActivity(context.idActivity, context.idIntern));
        InternActivity retrieved = internActivityDAO.findByActivityAndIntern(context.idActivity, context.idIntern);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByActivityAndInternWhenNoRecordReturnsNull() throws ServiceException, ValidationException {
        InternActivity retrieved = internActivityDAO.findByActivityAndIntern(
                TestConstants.NON_EXISTENT_ID, TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByActivityAndInternWithZeroIdThrowsValidationException() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                internActivityDAO.findByActivityAndIntern(TestConstants.INVALID_ID_ZERO, context.idIntern);
            }
        });
    }

    @Test
    void testFindByInternAndProjectAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        internActivityDAO.save(buildInternActivity(context.idActivity, context.idIntern));
        List<InternActivity> activities = internActivityDAO.findByInternAndProject(context.idIntern, context.idProject);
        assertEquals(TestConstants.SINGLE_RESULT, activities.size());
    }

    @Test
    void testFindByInternAndProjectWithNoActivitiesReturnsEmptyList() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        List<InternActivity> activities = internActivityDAO.findByInternAndProject(context.idIntern, context.idProject);
        assertTrue(activities.isEmpty());
    }

    @Test
    void testGetTotalHoursByInternAfterSaveReturnsDedicatedHours() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        internActivityDAO.save(buildInternActivity(context.idActivity, context.idIntern));
        int total = internActivityDAO.getTotalHoursByIntern(context.idIntern);
        assertEquals(TestConstants.DEFAULT_DEDICATED_HOURS, total);
    }

    @Test
    void testGetTotalHoursByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                internActivityDAO.getTotalHoursByIntern(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testUpdateInternActivityReturnsTrue() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        InternActivity activity = buildInternActivity(context.idActivity, context.idIntern);
        internActivityDAO.save(activity);
        activity.setStatus(TestConstants.STATUS_INTERN_ACTIVITY_COMPLETED);
        activity.setDedicatedHours(UPDATED_HOURS);
        boolean result = internActivityDAO.update(activity);
        assertTrue(result);
    }

    @Test
    void testUpdateInternActivityWithZeroIdThrowsValidationException() {
        InternActivity activity = buildInternActivity(
                TestConstants.NON_EXISTENT_ID, TestConstants.NON_EXISTENT_ID);
        activity.setIdInternActivity(TestConstants.INVALID_ID_ZERO);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                internActivityDAO.update(activity);
            }
        });
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        internActivityDAO.save(buildInternActivity(context.idActivity, context.idIntern));
        boolean result = internActivityDAO.deleteByInternAndProject(context.idIntern, context.idProject);
        assertTrue(result);
    }

    @Test
    void testDeleteByInternAndProjectWithZeroProjectIdThrowsValidationException() throws ServiceException, ValidationException {
        InternActivityContext context = persistContext();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                internActivityDAO.deleteByInternAndProject(context.idIntern, TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    private InternActivityContext persistContext() throws ServiceException, ValidationException {
        InternActivityContext context = new InternActivityContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
            context.idActivity = new ActivityTestDataBuilder()
                    .withProjectId(scene.getProjectId())
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return context;
    }

    private static final class InternActivityContext {
        int idIntern;
        int idProject;
        int idActivity;
    }
}
