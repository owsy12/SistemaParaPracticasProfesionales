package test.Logic;

import DataAccess.DataBaseConnection;
import Logic.DAO.InternActivityDAO;
import Logic.DTOs.InternActivity;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InternActivityDAOTest extends BaseDAOTest {

    private static final int SUPPORT_ACTIVITY_ID = 1;
    private static final int DEDICATED_HOURS = 8;
    private static final int UPDATED_HOURS = 12;
    private static final String STATUS_IN_PROGRESS = "En Progreso";
    private static final String STATUS_COMPLETED = "Completada";
    private static final String OBSERVATIONS = "Avance dentro de lo planeado.";
    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -2;
    private static final int NON_EXISTENT_ID = 9999;

    private final InternActivityDAO dao = new InternActivityDAO();

    private void insertSupportActivity() throws Exception {
        try (Connection connection = DataBaseConnection.connectDatabase();
             Statement statement = connection.createStatement()) {
            statement.execute(
                "INSERT INTO actividad (id_actividad, id_proyecto, nombre, descripcion, " +
                "semana_inicio_plan, semana_fin_plan) VALUES (" + SUPPORT_ACTIVITY_ID + ", " +
                ID_PROJECT + ", 'Levantamiento', 'Entrevistas', 1, 4)"
            );
        }
    }

    private InternActivity buildValidInternActivity() {
        InternActivity activity = new InternActivity();
        activity.setIdActivity(SUPPORT_ACTIVITY_ID);
        activity.setIdIntern(ID_INTERN);
        activity.setDedicatedHours(DEDICATED_HOURS);
        activity.setStatus(STATUS_IN_PROGRESS);
        activity.setCompletionDate(LocalDate.now());
        activity.setObservations(OBSERVATIONS);
        return activity;
    }

    @Test
    void testSaveValidInternActivityReturnsGeneratedId() throws Exception {
        insertSupportActivity();
        int generatedId = dao.save(buildValidInternActivity());
        assertTrue(generatedId > 0);
    }

    @Test
    void testSaveInternActivityWithZeroActivityIdThrowsValidationException() {
        InternActivity activity = buildValidInternActivity();
        activity.setIdActivity(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(activity));
    }

    @Test
    void testSaveInternActivityWithZeroInternIdThrowsValidationException() {
        InternActivity activity = buildValidInternActivity();
        activity.setIdIntern(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(activity));
    }

    @Test
    void testSaveDuplicateInternActivityThrowsDuplicateEntryException() throws Exception {
        insertSupportActivity();
        dao.save(buildValidInternActivity());
        assertThrows(DuplicateEntryException.class, () -> dao.save(buildValidInternActivity()));
    }

    @Test
    void testFindByActivityAndInternAfterSaveReturnsNotNull() throws Exception {
        insertSupportActivity();
        dao.save(buildValidInternActivity());
        InternActivity retrieved = dao.findByActivityAndIntern(SUPPORT_ACTIVITY_ID, ID_INTERN);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByActivityAndInternAfterSaveReturnsCorrectHours() throws Exception {
        insertSupportActivity();
        dao.save(buildValidInternActivity());
        InternActivity retrieved = dao.findByActivityAndIntern(SUPPORT_ACTIVITY_ID, ID_INTERN);
        assertEquals(DEDICATED_HOURS, retrieved.getDedicatedHours());
    }

    @Test
    void testFindByActivityAndInternWhenNoRecordReturnsNull() throws Exception {
        InternActivity retrieved = dao.findByActivityAndIntern(NON_EXISTENT_ID, ID_INTERN);
        assertNull(retrieved);
    }

    @Test
    void testFindByActivityAndInternWithZeroActivityIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findByActivityAndIntern(INVALID_ID_ZERO, ID_INTERN));
    }

    @Test
    void testFindByInternAndProjectAfterSaveReturnsOneElement() throws Exception {
        insertSupportActivity();
        dao.save(buildValidInternActivity());
        List<InternActivity> activities = dao.findByInternAndProject(ID_INTERN, ID_PROJECT);
        assertEquals(1, activities.size());
    }

    @Test
    void testFindByInternAndProjectWhenNoActivitiesReturnsEmptyList() throws Exception {
        List<InternActivity> activities = dao.findByInternAndProject(ID_INTERN, ID_PROJECT);
        assertTrue(activities.isEmpty());
    }

    @Test
    void testFindByInternAndProjectWithNegativeInternIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findByInternAndProject(INVALID_ID_NEGATIVE, ID_PROJECT));
    }

    @Test
    void testGetTotalHoursByInternAfterSaveReturnsDedicatedHours() throws Exception {
        insertSupportActivity();
        dao.save(buildValidInternActivity());
        int total = dao.getTotalHoursByIntern(ID_INTERN);
        assertEquals(DEDICATED_HOURS, total);
    }

    @Test
    void testGetTotalHoursByInternWhenNoActivitiesReturnsZero() throws Exception {
        int total = dao.getTotalHoursByIntern(ID_INTERN);
        assertEquals(0, total);
    }

    @Test
    void testGetTotalHoursByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.getTotalHoursByIntern(INVALID_ID_ZERO));
    }

    @Test
    void testUpdateInternActivityReturnsTrue() throws Exception {
        insertSupportActivity();
        InternActivity activity = buildValidInternActivity();
        dao.save(activity);
        activity.setStatus(STATUS_COMPLETED);
        activity.setDedicatedHours(UPDATED_HOURS);
        boolean result = dao.update(activity);
        assertTrue(result);
    }

    @Test
    void testUpdateInternActivityPersistsNewHours() throws Exception {
        insertSupportActivity();
        InternActivity activity = buildValidInternActivity();
        dao.save(activity);
        activity.setDedicatedHours(UPDATED_HOURS);
        dao.update(activity);
        InternActivity retrieved = dao.findByActivityAndIntern(SUPPORT_ACTIVITY_ID, ID_INTERN);
        assertEquals(UPDATED_HOURS, retrieved.getDedicatedHours());
    }

    @Test
    void testUpdateInternActivityWithZeroIdThrowsValidationException() {
        InternActivity activity = buildValidInternActivity();
        activity.setIdInternActivity(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.update(activity));
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws Exception {
        insertSupportActivity();
        dao.save(buildValidInternActivity());
        boolean result = dao.deleteByInternAndProject(ID_INTERN, ID_PROJECT);
        assertTrue(result);
    }

    @Test
    void testDeleteByInternAndProjectWithZeroProjectIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.deleteByInternAndProject(ID_INTERN, INVALID_ID_ZERO));
    }
}
