import Logic.DAO.ReportDAO;
import Logic.DTOs.Report;
import Logic.DTOs.ReportStatusUpdate;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportDAOTest extends BaseDAOTest {

    private static final String REPORT_PERIOD = "2025-04";
    private static final String REPORT_DOCUMENT_PATH = "/docs/reporte_2025_04.pdf";
    private static final int REPORTED_HOURS = 60;
    private static final String NEW_DOCUMENT_PATH = "/docs/reporte_2025_04_updated.pdf";
    private static final String SIGNED_DOCUMENT_PATH = "/docs/firmado/reporte_2025_04.pdf";
    private static final String STATUS_IN_REVIEW = "En revision";
    private static final String STATUS_UPDATE_OBSERVATIONS = "Revisión inicial sin observaciones.";
    private static final String REPORT_MONTH = "Abril";
    private static final int REPORT_YEAR = 2025;
    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -1;
    private static final int NON_EXISTENT_ID = 9999;
    private static final String BLANK_MONTH = "  ";

    private final ReportDAO dao = new ReportDAO();

    private Report buildValidReport() {
        Report report = new Report();
        report.setIdIntern(ID_INTERN);
        report.setIdProyect(ID_PROJECT);
        report.setIdProfessor(ID_PROFESSOR);
        report.setReportType(REPORT_TYPE_PARTIAL);
        report.setPeriod(REPORT_PERIOD);
        report.setDocumentPath(REPORT_DOCUMENT_PATH);
        report.setStatus(STATUS_PENDING);
        report.setReportedHours(REPORTED_HOURS);
        report.setSumissionDate(new java.util.Date());
        return report;
    }

    private ReportStatusUpdate buildReviewUpdate() {
        return new ReportStatusUpdate(STATUS_IN_REVIEW, STATUS_UPDATE_OBSERVATIONS,
                new Date(System.currentTimeMillis()));
    }

    @Test
    void testSaveValidReportReturnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidReport());
        assertEquals(1, result);
    }

    @Test
    void testSaveValidReportAssignsGeneratedId() throws Exception {
        Report report = buildValidReport();
        dao.save(report);
        assertTrue(report.getIdReport() > 0);
    }

    @Test
    void testSaveReportWithZeroInternIdThrowsValidationException() {
        Report report = buildValidReport();
        report.setIdIntern(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(report));
    }

    @Test
    void testGetByIdReturnsSupportReport() throws Exception {
        Report retrieved = dao.getById(ID_REPORT);
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.getById(INVALID_ID_ZERO));
    }

    @Test
    void testGetByIdWithNonExistentIdReturnsNull() throws Exception {
        Report retrieved = dao.getById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testGetAllReturnsAtLeastSupportReport() throws Exception {
        List<Report> all = dao.getAll();
        assertEquals(1, all.size());
    }

    @Test
    void testGetByStatusPendingReturnsSupportReport() throws Exception {
        List<Report> pending = dao.getByStatusPending();
        assertEquals(1, pending.size());
    }

    @Test
    void testGetByIdInternReturnsSupportReport() throws Exception {
        List<Report> reports = dao.getByIdIntern(ID_INTERN);
        assertEquals(1, reports.size());
    }

    @Test
    void testGetByIdInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.getByIdIntern(INVALID_ID_ZERO));
    }

    @Test
    void testGetByIdInternWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.getByIdIntern(INVALID_ID_NEGATIVE));
    }

    @Test
    void testGetByIdProfessorReturnsEmptyForSupportReport() throws Exception {
        List<Report> reports = dao.getByIdProfessor(ID_PROFESSOR);
        assertTrue(reports.isEmpty());
    }

    @Test
    void testGetByInternAndProjectReturnsSupportReport() throws Exception {
        List<Report> reports = dao.getByInternAndProject(ID_INTERN, ID_PROJECT);
        assertEquals(1, reports.size());
    }

    @Test
    void testUpdateStatusReturnsTrue() throws Exception {
        boolean result = dao.updateStatus(ID_REPORT, buildReviewUpdate());
        assertTrue(result);
    }

    @Test
    void testUpdateSignedDocumentPathReturnsTrue() throws Exception {
        boolean result = dao.updateSignedDocumentPath(ID_REPORT, SIGNED_DOCUMENT_PATH);
        assertTrue(result);
    }

    @Test
    void testUpdateDocumentPathReturnsTrue() throws Exception {
        boolean result = dao.updateDocumentPath(ID_REPORT, NEW_DOCUMENT_PATH);
        assertTrue(result);
    }

    @Test
    void testMarkLateDeliveryReturnsTrue() throws Exception {
        boolean result = dao.markLateDelivery(ID_REPORT);
        assertTrue(result);
    }

    @Test
    void testGetTotalApprovedHoursByInternReturnsZeroWhenNoApproved() throws Exception {
        int total = dao.getTotalApprovedHoursByIntern(ID_INTERN);
        assertEquals(0, total);
    }

    @Test
    void testGetTotalApprovedHoursByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.getTotalApprovedHoursByIntern(INVALID_ID_ZERO));
    }

    @Test
    void testExistsMonthlyByInternAndPeriodReturnsFalseForUnknownPeriod() throws Exception {
        boolean exists = dao.existsMonthlyByInternAndPeriod(ID_INTERN, REPORT_MONTH, REPORT_YEAR);
        assertFalse(exists);
    }

    @Test
    void testExistsMonthlyByInternAndPeriodWithBlankMonthReturnsFalse() throws Exception {
        boolean exists = dao.existsMonthlyByInternAndPeriod(ID_INTERN, BLANK_MONTH, REPORT_YEAR);
        assertFalse(exists);
    }

    @Test
    void testExistsPartialByInternAndProjectReturnsTrueForSupportReport() throws Exception {
        boolean exists = dao.existsPartialByInternAndProject(ID_INTERN, ID_PROJECT);
        assertTrue(exists);
    }

    @Test
    void testExistsFinalByInternAndProjectReturnsFalseForSupportReport() throws Exception {
        boolean exists = dao.existsFinalByInternAndProject(ID_INTERN, ID_PROJECT);
        assertFalse(exists);
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws Exception {
        boolean result = dao.deleteByInternAndProject(ID_INTERN, ID_PROJECT);
        assertTrue(result);
    }
}
