import DataAccess.DataBaseConnection;
import Logic.DAO.PartialAndFinalReportDAO;
import Logic.DTOs.PartialAndFinalReport;
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

class PartialAndFinalReportDAOTest extends BaseDAOTest {

    private final PartialAndFinalReportDAO dao = new PartialAndFinalReportDAO();

    private PartialAndFinalReport buildPartialReport(ReportSceneContext context) {
        PartialAndFinalReport partialReport = new PartialAndFinalReport();
        partialReport.setIdIntern(context.idIntern);
        partialReport.setIdProject(context.idProject);
        partialReport.setIdProfessor(context.idProfessor);
        partialReport.setReportType(TestConstants.REPORT_TYPE_PARTIAL);
        partialReport.setPeriod(TestConstants.DEFAULT_PERIOD);
        partialReport.setDocumentPath(TestConstants.DEFAULT_DOCUMENT_PATH);
        partialReport.setStatus(TestConstants.STATUS_REPORT_PENDING);
        partialReport.setReportedHours(TestConstants.DEFAULT_REPORTED_HOURS);
        partialReport.setSubmissionDate(new Date());
        partialReport.setReportNumber(TestConstants.DEFAULT_REPORT_NUMBER);
        partialReport.setCoveredHours(TestConstants.DEFAULT_REPORTED_HOURS);
        partialReport.setGeneralObjective(TestConstants.DEFAULT_GENERAL_OBJECTIVE);
        partialReport.setMethodology(TestConstants.DEFAULT_METHODOLOGY);
        partialReport.setObtainedResults(TestConstants.DEFAULT_OBTAINED_RESULTS);
        partialReport.setObservations(TestConstants.DEFAULT_OBSERVATIONS);
        return partialReport;
    }

    @Test
    void testSaveValidPartialReportReturnsOneRowAffected() throws ServiceException, ValidationException {
        ReportSceneContext context = persistContext();
        int result = dao.save(buildPartialReport(context));
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testSavePartialReportWithZeroInternIdThrowsServiceException() throws ServiceException, ValidationException {
        ReportSceneContext context = persistContext();
        PartialAndFinalReport report = buildPartialReport(context);
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
        PartialAndFinalReport report = buildPartialReport(context);
        dao.save(report);
        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdAfterSaveReturnsEqualObject() throws ServiceException, ValidationException {
        ReportSceneContext context = persistContext();
        PartialAndFinalReport report = buildPartialReport(context);
        dao.save(report);
        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());
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
        PartialAndFinalReport retrieved = dao.getById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        ReportSceneContext context = persistContext();
        dao.save(buildPartialReport(context));
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
        ReportSceneContext context = persistContext();
        dao.save(buildPartialReport(context));
        List<Report> pending = dao.getByStatusPending();
        assertEquals(TestConstants.SINGLE_RESULT, pending.size());
    }

    private ReportSceneContext persistContext() throws ServiceException, ValidationException {
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
