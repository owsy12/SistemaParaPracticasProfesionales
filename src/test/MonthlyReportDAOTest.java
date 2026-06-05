import DataAccess.DataBaseConnection;
import Logic.DAO.MonthlyReportDAO;
import Logic.DTOs.MonthlyReport;
import Logic.DTOs.Report;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonthlyReportDAOTest extends BaseDAOTest {

    private final MonthlyReportDAO dao = new MonthlyReportDAO();

    private MonthlyReport buildMonthlyReport(ReportSceneContext context) {
        MonthlyReport monthlyReport = new MonthlyReport();
        monthlyReport.setIdIntern(context.idIntern);
        monthlyReport.setIdProject(context.idProject);
        monthlyReport.setIdProfessor(context.idProfessor);
        monthlyReport.setReportType(TestConstants.REPORT_TYPE_MONTHLY);
        monthlyReport.setPeriod(TestConstants.DEFAULT_PERIOD);
        monthlyReport.setDocumentPath(TestConstants.DEFAULT_DOCUMENT_PATH);
        monthlyReport.setStatus(TestConstants.STATUS_REPORT_PENDING);
        monthlyReport.setReportedHours(TestConstants.DEFAULT_MONTHLY_HOURS);
        monthlyReport.setSumissionDate(new Date());
        monthlyReport.setMonth(TestConstants.DEFAULT_REPORT_MONTH);
        monthlyReport.setYear(TestConstants.DEFAULT_REPORT_YEAR);
        monthlyReport.setMonthlyHours(TestConstants.DEFAULT_MONTHLY_HOURS);
        monthlyReport.setBlock(TestConstants.DEFAULT_BLOCK);
        monthlyReport.setSection(TestConstants.DEFAULT_SECTION);
        monthlyReport.setReportNumber(TestConstants.DEFAULT_REPORT_NUMBER);
        return monthlyReport;
    }

    @Test
    void testSaveValidMonthlyReportReturnsOneRowAffected() throws ServiceException, ValidationException {
        ReportSceneContext context = persistContext();
        int result = dao.save(buildMonthlyReport(context));
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testSaveMonthlyReportWithZeroInternIdThrowsServiceException() throws ServiceException, ValidationException {
        ReportSceneContext context = persistContext();
        MonthlyReport report = buildMonthlyReport(context);
        report.setIdIntern(TestConstants.INVALID_ID_ZERO);
        assertThrows(ServiceException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(report);
            }
        });
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        ReportSceneContext context = persistContext();
        MonthlyReport report = buildMonthlyReport(context);
        dao.save(report);
        MonthlyReport retrieved = dao.getById(report.getIdReport());
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdAfterSaveReturnsEqualObject() throws ServiceException, ValidationException {
        ReportSceneContext context = persistContext();
        MonthlyReport report = buildMonthlyReport(context);
        dao.save(report);
        MonthlyReport retrieved = dao.getById(report.getIdReport());
        assertEquals(report, retrieved);
    }

    @Test
    void testGetByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.getById(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testGetByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        MonthlyReport retrieved = dao.getById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        ReportSceneContext context = persistContext();
        dao.save(buildMonthlyReport(context));
        List<Report> all = dao.getAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testGetAllWithNoDataReturnsEmptyList() throws ServiceException {
        List<Report> all = dao.getAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testGetByStatusPendingReturnsOneElement() throws ServiceException, ValidationException {
        ReportSceneContext context = persistContext();
        dao.save(buildMonthlyReport(context));
        List<Report> pending = dao.getByStatusPending();
        assertEquals(TestConstants.SINGLE_RESULT, pending.size());
    }

    private ReportSceneContext persistContext() throws ServiceException {
        ReportSceneContext context = new ReportSceneContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
            context.idProfessor = scene.getProfessorId();
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return context;
    }

    private static final class ReportSceneContext {
        int idIntern;
        int idProject;
        int idProfessor;
    }
}
