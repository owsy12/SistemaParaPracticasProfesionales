import Logic.DAO.MonthlyReportDAO;
import Logic.DTOs.MonthlyReport;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyReportDAOTest extends BaseDAOTest {

    private static final String REPORT_TYPE_MONTHLY = "Mensual";
    private static final String REPORT_PERIOD = "2025-03";
    private static final String REPORT_DOCUMENT_PATH = "/docs/mensual_2025_03.pdf";
    private static final String REPORT_MONTH = "Marzo";
    private static final int REPORT_YEAR = 2025;
    private static final String REPORT_BLOCK = "B";
    private static final String REPORT_SECTION = "IS-201";

    private final MonthlyReportDAO dao = new MonthlyReportDAO();

    private MonthlyReport buildValidReport() {
        MonthlyReport report = new MonthlyReport();
        report.setIdIntern(ID_INTERN);
        report.setIdProject(ID_PROJECT);
        report.setIdProfessor(ID_PROFESSOR);
        report.setReportType(REPORT_TYPE_MONTHLY);
        report.setPeriod(REPORT_PERIOD);
        report.setDocumentPath(REPORT_DOCUMENT_PATH);
        report.setStatus(STATUS_PENDING);
        report.setSumissionDate(new Date());
        report.setMonth(REPORT_MONTH);
        report.setYear(REPORT_YEAR);
        report.setBlock(REPORT_BLOCK);
        report.setSection(REPORT_SECTION);
        return report;
    }

    @Test
    void testSaveValidReportReturnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidReport());
        assertEquals(1, result);
    }

    @Test
    void testSaveValidReportAssignsGeneratedId() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);
        assertTrue(report.getIdReport() > 0);
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);
        MonthlyReport retrieved = dao.getById(report.getIdReport());
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectMonth() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);
        MonthlyReport retrieved = dao.getById(report.getIdReport());
        assertEquals(REPORT_MONTH, retrieved.getMonth());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectYear() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);
        MonthlyReport retrieved = dao.getById(report.getIdReport());
        assertEquals(REPORT_YEAR, retrieved.getYear());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectBlock() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);
        MonthlyReport retrieved = dao.getById(report.getIdReport());
        assertEquals(REPORT_BLOCK, retrieved.getBlock());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectSection() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);
        MonthlyReport retrieved = dao.getById(report.getIdReport());
        assertEquals(REPORT_SECTION, retrieved.getSection());
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws Exception {
        dao.save(buildValidReport());
        List<?> all = dao.getAll();
        assertEquals(1, all.size());
    }

    @Test
    void testGetByStatusPendingAfterSaveReturnsOneElement() throws Exception {
        dao.save(buildValidReport());
        List<?> pending = dao.getByStatusPending();
        assertEquals(1, pending.size());
    }

    @Test
    void testGetByStatusPendingWhenNoReportsReturnsEmptyList() throws Exception {
        List<?> pending = dao.getByStatusPending();
        assertTrue(pending.isEmpty());
    }

    @Test
    void testSaveSamePeriodSameInternSucceedsWhenSchemaAllows() throws Exception {
        dao.save(buildValidReport());
        MonthlyReport second = buildValidReport();
        int result = dao.save(second);
        assertEquals(1, result);
    }
}
