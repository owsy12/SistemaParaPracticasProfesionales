import DataAccess.DataBaseConnection;
import Logic.DAO.ActivityDAO;
import Logic.DTOs.Activity;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityDAOTest extends BaseDAOTest {

    private static final String ACTIVITY_NAME = "Levantamiento de requerimientos";
    private static final String ACTIVITY_NAME_SECOND = "Diseño de base de datos";
    private static final String UPDATED_ACTIVITY_NAME = "Levantamiento detallado";
    private static final String ACTIVITY_DESCRIPTION = "Entrevistas iniciales.";
    private static final LocalDate ACTIVITY_START = LocalDate.of(2025, 2, 1);
    private static final LocalDate ACTIVITY_END = LocalDate.of(2025, 2, 28);

    private final ActivityDAO dao = new ActivityDAO();

    private Activity buildActivity(int idProject, String name) {
        Activity activity = new Activity();
        activity.setIdProject(idProject);
        activity.setName(name);
        activity.setDescription(ACTIVITY_DESCRIPTION);
        activity.setStatus(TestConstants.STATUS_ACTIVITY_ACTIVE);
        activity.setCreationDate(ACTIVITY_START);
        activity.setStartDate(ACTIVITY_START);
        activity.setEndDate(ACTIVITY_END);
        return activity;
    }

    @Test
    void testSaveValidActivityReturnsPositiveId() throws ServiceException, ValidationException {
        int idProject = persistProject();
        int generatedId = dao.save(buildActivity(idProject, ACTIVITY_NAME));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveValidActivityAssignsIdToDto() throws ServiceException, ValidationException {
        int idProject = persistProject();
        Activity activity = buildActivity(idProject, ACTIVITY_NAME);
        dao.save(activity);
        assertTrue(activity.getIdActivity() > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveActivityWithZeroProjectIdThrowsValidationException() {
        Activity activity = buildActivity(TestConstants.INVALID_ID_ZERO, ACTIVITY_NAME);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(activity);
            }
        });
    }

    @Test
    void testSaveActivityWithBlankNameThrowsValidationException() throws ServiceException, ValidationException {
        int idProject = persistProject();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(buildActivity(idProject, TestConstants.BLANK_TEXT));
            }
        });
    }

    @Test
    void testSaveActivityWithNullNameThrowsValidationException() throws ServiceException, ValidationException {
        int idProject = persistProject();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(buildActivity(idProject, null));
            }
        });
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        int idProject = persistProject();
        Activity activity = buildActivity(idProject, ACTIVITY_NAME);
        dao.save(activity);
        Activity retrieved = dao.findById(activity.getIdActivity());
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdAfterSaveReturnsEqualObject() throws ServiceException, ValidationException {
        int idProject = persistProject();
        Activity activity = buildActivity(idProject, ACTIVITY_NAME);
        dao.save(activity);
        Activity retrieved = dao.findById(activity.getIdActivity());
        assertEquals(activity, retrieved);
    }

    @Test
    void testFindByIdAfterSaveReturnsCorrectName() throws ServiceException, ValidationException {
        int idProject = persistProject();
        Activity activity = buildActivity(idProject, ACTIVITY_NAME);
        dao.save(activity);
        Activity retrieved = dao.findById(activity.getIdActivity());
        assertEquals(ACTIVITY_NAME, retrieved.getName());
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        Activity retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findById(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findById(TestConstants.INVALID_ID_NEGATIVE);
            }
        });
    }

    @Test
    void testFindByProjectAfterSavingTwoActivitiesReturnsTwo() throws ServiceException, ValidationException {
        int idProject = persistProject();
        dao.save(buildActivity(idProject, ACTIVITY_NAME));
        dao.save(buildActivity(idProject, ACTIVITY_NAME_SECOND));
        List<Activity> activities = dao.findByProject(idProject);
        assertEquals(TestConstants.TWO_RESULTS, activities.size());
    }

    @Test
    void testFindByProjectWhenNoActivitiesReturnsEmptyList() throws ServiceException, ValidationException {
        int idProject = persistProject();
        List<Activity> activities = dao.findByProject(idProject);
        assertTrue(activities.isEmpty());
    }

    @Test
    void testFindByProjectWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findByProject(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testUpdateActivityReturnsTrue() throws ServiceException, ValidationException {
        int idProject = persistProject();
        Activity activity = buildActivity(idProject, ACTIVITY_NAME);
        dao.save(activity);
        activity.setName(UPDATED_ACTIVITY_NAME);
        boolean result = dao.update(activity);
        assertTrue(result);
    }

    @Test
    void testUpdateActivityWithBlankNameThrowsValidationException() throws ServiceException, ValidationException {
        int idProject = persistProject();
        Activity activity = buildActivity(idProject, ACTIVITY_NAME);
        dao.save(activity);
        activity.setName(TestConstants.BLANK_TEXT);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.update(activity);
            }
        });
    }

    @Test
    void testDeactivateActivityReturnsTrue() throws ServiceException, ValidationException {
        int idProject = persistProject();
        Activity activity = buildActivity(idProject, ACTIVITY_NAME);
        dao.save(activity);
        boolean result = dao.deactivate(activity.getIdActivity());
        assertTrue(result);
    }

    @Test
    void testDeactivateActivityPersistsInactiveStatus() throws ServiceException, ValidationException {
        int idProject = persistProject();
        Activity activity = buildActivity(idProject, ACTIVITY_NAME);
        dao.save(activity);
        dao.deactivate(activity.getIdActivity());
        Activity retrieved = dao.findById(activity.getIdActivity());
        assertEquals(TestConstants.STATUS_ACTIVITY_INACTIVE, retrieved.getStatus());
    }

    @Test
    void testDeactivateNonExistentActivityReturnsFalse() throws ServiceException, ValidationException {
        boolean result = dao.deactivate(TestConstants.NON_EXISTENT_ID);
        assertFalse(result);
    }

    @Test
    void testDeleteActivityReturnsTrue() throws ServiceException, ValidationException {
        int idProject = persistProject();
        Activity activity = buildActivity(idProject, ACTIVITY_NAME);
        dao.save(activity);
        boolean result = dao.delete(activity.getIdActivity());
        assertTrue(result);
    }

    @Test
    void testDeleteWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.delete(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    private int persistProject() throws ServiceException, ValidationException {
        int idProject;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            idProject = scene.getProjectId();
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idProject;
    }
}
