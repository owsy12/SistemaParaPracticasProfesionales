import DataAccess.DataBaseConnection;
import Logic.DAO.ReportActivityDAO;
import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportDeliverable;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportActivityDAOTest extends BaseDAOTest {

    private final ReportActivityDAO dao = new ReportActivityDAO();

    private ReportActivity buildReportActivity(int idReport, int idActivity) {
        ReportActivity reportActivity = new ReportActivity();
        reportActivity.setIdReport(idReport);
        reportActivity.setIdActivity(idActivity);
        reportActivity.setPeriod(TestConstants.DEFAULT_PERIOD);
        reportActivity.setWeeklyPlan(TestConstants.DEFAULT_PLAN_WEEKS);
        reportActivity.setRealWeeks(TestConstants.DEFAULT_REAL_WEEKS);
        reportActivity.setAdvancePercentage(TestConstants.DEFAULT_ADVANCE_PERCENTAGE);
        reportActivity.setObservation(TestConstants.DEFAULT_OBSERVATIONS);
        return reportActivity;
    }

    private ReportDeliverable buildDeliverable(int idReport) {
        ReportDeliverable deliverable = new ReportDeliverable();
        deliverable.setIdReport(idReport);
        deliverable.setResultado(TestConstants.DEFAULT_DELIVERABLE_RESULT);
        deliverable.setDescripcion(TestConstants.DEFAULT_DELIVERABLE_DESCRIPTION);
        deliverable.setAdvancePercentage(TestConstants.DEFAULT_ADVANCE_PERCENTAGE);
        deliverable.setObservaciones(TestConstants.DEFAULT_OBSERVATIONS);
        return deliverable;
    }

    @Test
    void testSaveValidReportActivityReturnsPositiveId() throws Exception {
        ReportActivityContext context = persistContext();
        int generatedId = dao.save(buildReportActivity(context.idReport, context.idActivity));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveReportActivityWithZeroReportIdThrowsValidationException() throws Exception {
        ReportActivityContext context = persistContext();
        ReportActivity reportActivity =
                buildReportActivity(TestConstants.INVALID_ID_ZERO, context.idActivity);
        assertThrows(ValidationException.class, () -> dao.save(reportActivity));
    }

    @Test
    void testFindByReportAfterSaveReturnsOneElement() throws Exception {
        ReportActivityContext context = persistContext();
        dao.save(buildReportActivity(context.idReport, context.idActivity));
        List<ReportActivity> byReport = dao.findByReport(context.idReport);
        assertEquals(TestConstants.SINGLE_RESULT, byReport.size());
    }

    @Test
    void testFindByReportWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findByReport(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testSaveDeliverableReturnsPositiveId() throws Exception {
        ReportActivityContext context = persistContext();
        int generatedId = dao.saveDeliverable(buildDeliverable(context.idReport));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testFindDeliverablesByReportAfterSaveReturnsOneElement() throws Exception {
        ReportActivityContext context = persistContext();
        dao.saveDeliverable(buildDeliverable(context.idReport));
        List<ReportDeliverable> deliverables = dao.findDeliverablesByReport(context.idReport);
        assertEquals(TestConstants.SINGLE_RESULT, deliverables.size());
    }

    @Test
    void testFindActivityIdsInMonthlyReportsByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findActivityIdsInMonthlyReportsByIntern(TestConstants.INVALID_ID_ZERO));
    }

    private ReportActivityContext persistContext() throws Exception {
        ReportActivityContext context = new ReportActivityContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idActivity = new ActivityTestDataBuilder()
                    .withProjectId(scene.getProjectId())
                    .persist(connection);
            context.idReport = new ReportTestDataBuilder()
                    .withInternId(scene.getInternId())
                    .withProjectId(scene.getProjectId())
                    .withProfessorId(scene.getProfessorId())
                    .persist(connection);
        }
        return context;
    }

    private static final class ReportActivityContext {
        int idReport;
        int idActivity;
    }
}
