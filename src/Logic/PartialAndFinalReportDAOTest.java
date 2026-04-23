package Logic;

import Logic.DAO.PartialAndFinalReportDAO;
import Logic.DTOs.PartialAndFinalReport;
import Logic.Exceptions.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PartialAndFinalReportDAOTest extends BaseDAOTest {

    private final PartialAndFinalReportDAO dao = new PartialAndFinalReportDAO();

    private PartialAndFinalReport buildValidReport() {
        PartialAndFinalReport report = new PartialAndFinalReport();
        report.setIdIntern        (ID_PRACTICANTE);
        report.setIdProyect       (ID_PROYECTO);
        report.setIdProfessor     (ID_PROFESOR);
        report.setReportType      ("Parcial");
        report.setPeriod          ("2025-02");
        report.setDocumentPath    ("/docs/parcial_2025_02.pdf");
        report.setStatus          ("Pendiente");
        report.setSumissionDate   (new Date());
        report.setReportNumber    (2);
        report.setCoveredHours    (80);
        report.setGeneralObjective("Desarrollar módulo de reportes");
        report.setMethodology     ("Kanban");
        report.setObtainedResults ("Módulo completado al 100%");
        report.setObservations    ("Sin observaciones adicionales");
        return report;
    }

    @Test
    void save_withValidData_returnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidReport());

        assertEquals(1, result);
    }

    @Test
    void save_withValidData_assignsGeneratedId() throws Exception {
        PartialAndFinalReport report = buildValidReport();

        dao.save(report);

        assertTrue(report.getIdReport() > 0);
    }

    @Test
    void save_thenGetById_returnsNotNull() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);

        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());

        assertNotNull(retrieved);
    }

    @Test
    void save_thenGetById_returnsCorrectPeriod() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);

        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());

        assertEquals("2025-02", retrieved.getPeriod());
    }

    @Test
    void save_thenGetById_returnsCorrectReportNumber() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);

        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());

        assertEquals(2, retrieved.getReportNumber());
    }

    @Test
    void save_thenGetById_returnsCorrectCoveredHours() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);

        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());

        assertEquals(80, retrieved.getCoveredHours());
    }

    @Test
    void save_thenGetById_returnsCorrectStatus() throws Exception {
        PartialAndFinalReport report = buildValidReport();
        dao.save(report);

        PartialAndFinalReport retrieved = dao.getById(report.getIdReport());

        assertEquals("Pendiente", retrieved.getStatus());
    }

    @Test
    void getById_withNonExistentId_returnsNull() throws Exception {
        PartialAndFinalReport retrieved = dao.getById(9999);

        assertNull(retrieved);
    }

    @Test
    void save_duplicatePeriodSameIntern_throwsDatabaseException() throws Exception {
        dao.save(buildValidReport()); // periodo 2025-02

        // Mismo practicante, mismo periodo: viola la constraint única
        assertThrows(ServiceException.class, () -> dao.save(buildValidReport()));
    }
}
