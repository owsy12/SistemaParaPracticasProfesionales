import DataAccess.DataBaseConnection;
import Logic.DAO.ReportDAO;
import Logic.DTOs.Report;
import Logic.DTOs.ReportStatusUpdate;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportDAOTest extends BaseDAOTest {

    private static final String NEW_DOCUMENT_PATH = "/documents/updated_report.pdf";
    private static final String SIGNED_DOCUMENT_PATH = "/documents/signed_report.pdf";

    private final ReportDAO dao = new ReportDAO();

    private Report buildReport(ReportContext context) {
        Report report = new Report();
        report.setIdIntern(context.idIntern);
        report.setIdProject(context.idProject);
        report.setIdProfessor(context.idProfessor);
        report.setReportType(TestConstants.REPORT_TYPE_PARTIAL);
        report.setPeriod(TestConstants.DEFAULT_PERIOD);
        report.setDocumentPath(TestConstants.DEFAULT_DOCUMENT_PATH);
        report.setStatus(TestConstants.STATUS_REPORT_PENDING);
        report.setReportedHours(TestConstants.DEFAULT_REPORTED_HOURS);
        report.setSumissionDate(new java.util.Date());
        return report;
    }

    private ReportStatusUpdate buildReviewUpdate() {
        return new ReportStatusUpdate(TestConstants.STATUS_REPORT_IN_REVIEW,
                TestConstants.DEFAULT_OBSERVATIONS,
                new Date(System.currentTimeMillis()));
    }

    @Test
    void testSaveValidReportReturnsOneRowAffected() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        int result = dao.save(buildReport(context));
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testSaveValidReportAssignsGeneratedId() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        Report report = buildReport(context);
        dao.save(report);
        assertTrue(report.getIdReport() > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveReportWithZeroInternIdThrowsValidationException() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        Report report = buildReport(context);
        report.setIdIntern(TestConstants.INVALID_ID_ZERO);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(report);
            }
        });
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        int idReport = persistReportRow(context);
        Report retrieved = dao.getById(idReport);
        assertNotNull(retrieved);
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
        Report retrieved = dao.getById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        persistReportRow(context);
        List<Report> all = dao.getAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testGetAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<Report> all = dao.getAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testGetByStatusPendingReturnsOneElement() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        persistReportRow(context);
        List<Report> pending = dao.getByStatusPending();
        assertEquals(TestConstants.SINGLE_RESULT, pending.size());
    }

    @Test
    void testGetByIdInternReturnsOneElement() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        persistReportRow(context);
        List<Report> reports = dao.getByIdIntern(context.idIntern);
        assertEquals(TestConstants.SINGLE_RESULT, reports.size());
    }

    @Test
    void testGetByIdInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.getByIdIntern(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testGetByInternAndProjectReturnsOneElement() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        persistReportRow(context);
        List<Report> reports = dao.getByInternAndProject(context.idIntern, context.idProject);
        assertEquals(TestConstants.SINGLE_RESULT, reports.size());
    }

    @Test
    void testUpdateStatusReturnsTrue() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        int idReport = persistReportRow(context);
        boolean result = dao.updateStatus(idReport, buildReviewUpdate());
        assertTrue(result);
    }

    @Test
    void testUpdateSignedDocumentPathReturnsTrue() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        int idReport = persistReportRow(context);
        boolean result = dao.updateSignedDocumentPath(idReport, SIGNED_DOCUMENT_PATH);
        assertTrue(result);
    }

    @Test
    void testUpdateDocumentPathReturnsTrue() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        int idReport = persistReportRow(context);
        boolean result = dao.updateDocumentPath(idReport, NEW_DOCUMENT_PATH);
        assertTrue(result);
    }

    @Test
    void testMarkLateDeliveryReturnsTrue() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        int idReport = persistReportRow(context);
        boolean result = dao.markLateDelivery(idReport);
        assertTrue(result);
    }

    @Test
    void testGetTotalApprovedHoursByInternReturnsZero() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        persistReportRow(context);
        int total = dao.getTotalApprovedHoursByIntern(context.idIntern);
        assertEquals(TestConstants.ZERO_RESULTS, total);
    }

    @Test
    void testGetTotalApprovedHoursByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.getTotalApprovedHoursByIntern(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testExistsPartialByInternAndProjectReturnsTrue() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        persistReportRow(context);
        boolean exists = dao.existsPartialByInternAndProject(context.idIntern, context.idProject);
        assertTrue(exists);
    }

    @Test
    void testExistsFinalByInternAndProjectReturnsFalse() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        persistReportRow(context);
        boolean exists = dao.existsFinalByInternAndProject(context.idIntern, context.idProject);
        assertFalse(exists);
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws ServiceException, ValidationException {
        ReportContext context = persistContext();
        persistReportRow(context);
        boolean result = dao.deleteByInternAndProject(context.idIntern, context.idProject);
        assertTrue(result);
    }

    private ReportContext persistContext() throws ServiceException, ValidationException {
        ReportContext context = new ReportContext();
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

    private int persistReportRow(ReportContext context) throws ServiceException, ValidationException {
        int idReport;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idReport = new ReportTestDataBuilder()
                    .withInternId(context.idIntern)
                    .withProjectId(context.idProject)
                    .withProfessorId(context.idProfessor)
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idReport;
    }

    private static final class ReportContext {
        int idIntern;
        int idProject;
        int idProfessor;
    }
}
