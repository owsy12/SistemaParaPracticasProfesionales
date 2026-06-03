import Logic.DAO.PartialAndFinalReportDAO;
import Logic.DTOs.PartialAndFinalReport;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PartialAndFinalReportDAOTest extends BaseDAOTest {

    private static final String REPORT_PERIOD = "2025-02";
    private static final String REPORT_DOCUMENT_PATH = "/docs/parcial_2025_02.pdf";
    private static final int REPORT_NUMBER = 2;
    private static final int REPORT_COVERED_HOURS = 80;
    private static final String REPORT_GENERAL_OBJECTIVE = "Desarrollar módulo de reportes";
    private static final String REPORT_METHODOLOGY = "Kanban";
    private static final String REPORT_RESULTS = "Módulo completado al 100%";
    private static final String REPORT_OBSERVATIONS = "Sin observaciones adicionales";
    private static final int NON_EXISTENT_REPORT_ID = 9999;

    private final PartialAndFinalReportDAO dao = new PartialAndFinalReportDAO();

    private PartialAndFinalReport buildValidReport() {
        PartialAndFinalReport report = new PartialAndFinalReport();
        report.setIdIntern(ID_INTERN);
        report.setIdProyect(ID_PROJECT);
        report.setIdProfessor(ID_PROFESSOR);
        report.setReportType(REPORT_TYPE_PARTIAL);
        report.setPeriod(REPORT_PERIOD);
        report.setDocumentPath(REPORT_DOCUMENT_PATH);
        report.setStatus(STATUS_PENDING);
        report.setSumissionDate(new Date());
        report.setReportNumber(REPORT_NUMBER);
        report.setCoveredHours(REPORT_COVERED_HOURS);
        report.setGeneralObjective(REPORT_GENERAL_OBJECTIVE);
        report.setMethodology(REPORT_METHODOLOGY);
        report.setObtainedResults(REPORT_RESULTS);
        report.setObservations(REPORT_OBSERVATIONS);
        return report;
    }

    @Test
    void testSaveValidReportReturnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidReport());
        assertEquals(1, result);
    }

    @Test
    void testSaveValidReportAssignsGeneratedId() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);
        assertTrue(report.getIdReport() > 0);
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);
        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectPeriod() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);
        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());
        assertEquals(REPORT_PERIOD, retrieved.getPeriod());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectReportNumber() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);
        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());
        assertEquals(REPORT_NUMBER, retrieved.getReportNumber());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectCoveredHours() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);
        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());
        assertEquals(REPORT_COVERED_HOURS, retrieved.getCoveredHours());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectStatus() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);
        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());
        assertEquals(STATUS_PENDING, retrieved.getStatus());
    }

    @Test
    void testGetByIdWithNonExistentIdReturnsNull() throws Exception {
        PartialAndFinalReport retrieved = dao.getById(NON_EXISTENT_REPORT_ID);
        assertNull(retrieved);
    }

    @Test
    void testSaveSamePeriodSameInternSucceedsWhenSchemaAllows() throws Exception {
        dao.save(buildValidReport());
        int result = dao.save(buildValidReport());
        assertEquals(1, result);
    }
}
