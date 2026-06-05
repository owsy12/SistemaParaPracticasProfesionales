import DataAccess.DataBaseConnection;
import Logic.DAO.ReportActivityDAO;
import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportDeliverable;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportActivityDAOTest extends BaseDAOTest {

    private static final int SUPPORT_ACTIVITY_ID = 1;
    private static final String PERIOD = "2025-01";
    private static final String PLAN_WEEKS = "1-2";
    private static final String REAL_WEEKS = "1-2";
    private static final int ADVANCE_PERCENTAGE = 50;
    private static final String OBSERVATIONS = "Avance al 50%";
    private static final String DELIVERABLE_RESULT = "Documento de requerimientos";
    private static final String DELIVERABLE_DESCRIPTION = "Especificación funcional firmada.";
    private static final String BLANK_RESULT = "  ";
    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -1;

    private final ReportActivityDAO dao = new ReportActivityDAO();

    private void insertSupportActivity() throws Exception {
        try (Connection connection = DataBaseConnection.connectDatabase();
             Statement statement = connection.createStatement()) {
            statement.execute(
                "INSERT INTO actividad (id_actividad, id_proyecto, nombre, descripcion, " +
                "semana_inicio_plan, semana_fin_plan) VALUES (" + SUPPORT_ACTIVITY_ID + ", " +
                ID_PROJECT + ", 'Levantamiento', 'Entrevistas', 1, 2)"
            );
        }
    }

    private ReportActivity buildValidReportActivity() {
        ReportActivity ra = new ReportActivity();
        ra.setIdReport(ID_REPORT);
        ra.setIdActivity(SUPPORT_ACTIVITY_ID);
        ra.setPeriod(PERIOD);
        ra.setWeeklyPlan(PLAN_WEEKS);
        ra.setRealWeeks(REAL_WEEKS);
        ra.setAdvancePercentage(ADVANCE_PERCENTAGE);
        ra.setObservation(OBSERVATIONS);
        return ra;
    }

    private ReportDeliverable buildValidDeliverable(String result) {
        ReportDeliverable rd = new ReportDeliverable();
        rd.setIdReport(ID_REPORT);
        rd.setResultado(result);
        rd.setDescripcion(DELIVERABLE_DESCRIPTION);
        rd.setAdvancePercentage(ADVANCE_PERCENTAGE);
        rd.setObservaciones(OBSERVATIONS);
        return rd;
    }

    @Test
    void testSaveValidReportActivityReturnsOneRowAffected() throws Exception {
        insertSupportActivity();
        int rows = dao.save(buildValidReportActivity());
        assertEquals(1, rows);
    }

    @Test
    void testSaveValidReportActivityAssignsGeneratedId() throws Exception {
        insertSupportActivity();
        ReportActivity ra = buildValidReportActivity();
        dao.save(ra);
        assertTrue(ra.getIdReporteActividad() > 0);
    }

    @Test
    void testSaveNullReportActivityThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.save(null));
    }

    @Test
    void testSaveReportActivityWithZeroReportIdThrowsValidationException() {
        ReportActivity ra = buildValidReportActivity();
        ra.setIdReport(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(ra));
    }

    @Test
    void testFindByReportAfterSaveReturnsOneElement() throws Exception {
        insertSupportActivity();
        dao.save(buildValidReportActivity());
        List<ReportActivity> list = dao.findByReport(ID_REPORT);
        assertEquals(1, list.size());
    }

    @Test
    void testFindByReportWhenNoActivitiesReturnsEmptyList() throws Exception {
        List<ReportActivity> list = dao.findByReport(ID_REPORT);
        assertTrue(list.isEmpty());
    }

    @Test
    void testFindByReportWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findByReport(INVALID_ID_ZERO));
    }

    @Test
    void testSaveValidDeliverableReturnsOneRowAffected() throws Exception {
        int rows = dao.saveDeliverable(buildValidDeliverable(DELIVERABLE_RESULT));
        assertEquals(1, rows);
    }

    @Test
    void testSaveDeliverableWithNullResultThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.saveDeliverable(buildValidDeliverable(null)));
    }

    @Test
    void testSaveDeliverableWithBlankResultThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.saveDeliverable(buildValidDeliverable(BLANK_RESULT)));
    }

    @Test
    void testSaveNullDeliverableThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.saveDeliverable(null));
    }

    @Test
    void testFindDeliverablesByReportAfterSaveReturnsOneElement() throws Exception {
        dao.saveDeliverable(buildValidDeliverable(DELIVERABLE_RESULT));
        List<ReportDeliverable> list = dao.findDeliverablesByReport(ID_REPORT);
        assertEquals(1, list.size());
    }

    @Test
    void testFindDeliverablesByReportWhenNoneReturnsEmptyList() throws Exception {
        List<ReportDeliverable> list = dao.findDeliverablesByReport(ID_REPORT);
        assertTrue(list.isEmpty());
    }

    @Test
    void testFindDeliverablesByReportWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findDeliverablesByReport(INVALID_ID_NEGATIVE));
    }

    @Test
    void testFindActivityIdsInMonthlyReportsByInternReturnsEmptyForNonMonthly() throws Exception {
        List<Integer> ids = dao.findActivityIdsInMonthlyReportsByIntern(ID_INTERN);
        assertTrue(ids.isEmpty());
    }

    @Test
    void testFindActivityIdsInMonthlyReportsByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findActivityIdsInMonthlyReportsByIntern(INVALID_ID_ZERO));
    }
}
