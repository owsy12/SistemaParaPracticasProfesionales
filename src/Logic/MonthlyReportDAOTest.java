package Logic;

import Logic.DAO.MonthlyReportDAO;
import Logic.DTOs.MonthlyReport;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyReportDAOTest extends BaseDAOTest {

    private final MonthlyReportDAO dao = new MonthlyReportDAO();

    private MonthlyReport buildValidReport() {
        MonthlyReport report = new MonthlyReport();
        report.setIdIntern    (ID_PRACTICANTE);
        report.setIdProyect   (ID_PROYECTO);
        report.setIdProfessor (ID_PROFESOR);
        report.setReportType  ("Mensual");
        report.setPeriod      ("2025-03");
        report.setDocumentPath("/docs/mensual_2025_03.pdf");
        report.setStatus      ("Pendiente");
        report.setSumissionDate(new Date());
        report.setMonth       ("Marzo");
        report.setYear        (2025);
        report.setBlock       ("B");
        report.setSection     ("IS-201");
        return report;
    }

    // ---------------------------------------------------------------

    @Test
    void save_withValidData_returnsOneRowAffected() throws Exception {
        MonthlyReport report = buildValidReport();

        int result = dao.save(report);

        assertEquals(1, result);
    }

    @Test
    void save_withValidData_assignsGeneratedId() throws Exception {
        MonthlyReport report = buildValidReport();

        dao.save(report);

        assertTrue(report.getIdReport() > 0);
    }

    @Test
    void save_thenGetById_returnsCorrectMonth() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);

        MonthlyReport retrieved = dao.getById(report.getIdReport());

        assertNotNull(retrieved);
        assertEquals("Marzo", retrieved.getMonth());
    }

    @Test
    void save_thenGetById_returnsCorrectYear() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);

        MonthlyReport retrieved = dao.getById(report.getIdReport());

        assertEquals(2025, retrieved.getYear());
    }

    @Test
    void save_thenGetById_returnsCorrectBlock() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);

        MonthlyReport retrieved = dao.getById(report.getIdReport());

        assertEquals("B", retrieved.getBlock());
    }

    @Test
    void save_thenGetById_returnsCorrectSection() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);

        MonthlyReport retrieved = dao.getById(report.getIdReport());

        assertEquals("IS-201", retrieved.getSection());
    }

    @Test
    void save_thenGetAll_containsSavedReport() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);

        List<?> all = dao.getAll();

        assertFalse(all.isEmpty());
        assertEquals(1, all.size());
    }

    @Test
    void save_thenGetByStatusPending_containsSavedReport() throws Exception {
        MonthlyReport report = buildValidReport();
        dao.save(report);

        List<?> pending = dao.getByStatusPending();

        assertEquals(1, pending.size());
    }
}
