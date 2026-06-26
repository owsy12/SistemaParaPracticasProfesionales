import DataAccess.DataBaseConnection;
import Logic.DAO.ReportActivityDAO;
import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportDeliverable;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportActivityDAOTest extends BaseDAOTest {

    private final ReportActivityDAO reportActivityDAO = new ReportActivityDAO();

    private ReportActivity buildReportActivity(int idReport, int idActivity) {
        ReportActivity reportActivity = new ReportActivity();
        reportActivity.setIdReport(idReport);
        reportActivity.setIdActivity(idActivity);
        reportActivity.setPeriod(TestConstants.DEFAULT_PERIOD);
        reportActivity.setWeeklyPlan(TestConstants.DEFAULT_PLAN_WEEKS);
        reportActivity.setRealWeeks(TestConstants.DEFAULT_REAL_WEEKS);
        reportActivity.setAdvancePercentage(TestConstants.DEFAULT_ADVANCE_PERCENTAGE);
        reportActivity.setObservations(TestConstants.DEFAULT_OBSERVATIONS);
        return reportActivity;
    }

    private ReportDeliverable buildDeliverable(int idReport) {
        ReportDeliverable deliverable = new ReportDeliverable();
        deliverable.setIdReport(idReport);
        deliverable.setResult(TestConstants.DEFAULT_DELIVERABLE_RESULT);
        deliverable.setDescription(TestConstants.DEFAULT_DELIVERABLE_DESCRIPTION);
        deliverable.setAdvancePercentage(TestConstants.DEFAULT_ADVANCE_PERCENTAGE);
        deliverable.setObservations(TestConstants.DEFAULT_OBSERVATIONS);
        return deliverable;
    }

    @Test
    void testSaveValidReportActivityReturnsPositiveId() throws ServiceException, ValidationException {
        ReportActivityContext context = persistContext();
        int generatedId = reportActivityDAO.save(buildReportActivity(context.idReport, context.idActivity));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveReportActivityWithZeroReportIdThrowsValidationException() throws ServiceException, ValidationException {
        ReportActivityContext context = persistContext();
        ReportActivity reportActivity =
                buildReportActivity(TestConstants.INVALID_ID_ZERO, context.idActivity);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                reportActivityDAO.save(reportActivity);
            }
        });
    }

    @Test
    void testFindByReportAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        ReportActivityContext context = persistContext();
        reportActivityDAO.save(buildReportActivity(context.idReport, context.idActivity));
        List<ReportActivity> byReport = reportActivityDAO.findByReport(context.idReport);
        assertEquals(TestConstants.SINGLE_RESULT, byReport.size());
    }

    @Test
    void testFindByReportWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                reportActivityDAO.findByReport(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testSaveDeliverableReturnsPositiveId() throws ServiceException, ValidationException {
        ReportActivityContext context = persistContext();
        int generatedId = reportActivityDAO.saveDeliverable(buildDeliverable(context.idReport));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testFindDeliverablesByReportAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        ReportActivityContext context = persistContext();
        reportActivityDAO.saveDeliverable(buildDeliverable(context.idReport));
        List<ReportDeliverable> deliverables = reportActivityDAO.findDeliverablesByReport(context.idReport);
        assertEquals(TestConstants.SINGLE_RESULT, deliverables.size());
    }

    @Test
    void testFindActivityIdsInMonthlyReportsByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                reportActivityDAO.findActivityIdsInMonthlyReportsByIntern(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    private ReportActivityContext persistContext() throws ServiceException, ValidationException {
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
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return context;
    }

    private static final class ReportActivityContext {
        int idReport;
        int idActivity;
    }
}
