package Logic;

import Logic.DAO.ReportEvaluationDAO;
import Logic.DTOs.ReportEvaluation;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportEvaluationDAOTest extends BaseDAOTest {

    private final ReportEvaluationDAO dao = new ReportEvaluationDAO();

    // El reporte con ID_REPORTE=1 ya existe al finalizar BaseDAOTest.setUpDatabase()
    private ReportEvaluation buildValidEvaluation() {
        ReportEvaluation evaluation = new ReportEvaluation();
        evaluation.setIdReport      (ID_REPORTE);
        evaluation.setGrade         (9);
        evaluation.setFeedback      ("Buen desempeño, mejorar la documentación.");
        evaluation.setEvaluationDate(new Date());
        return evaluation;
    }

    // ---------------------------------------------------------------

    @Test
    void save_withValidData_returnsOneRowAffected() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();

        int result = dao.save(evaluation);

        assertEquals(1, result);
    }

    @Test
    void save_withValidData_assignsGeneratedId() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();

        dao.save(evaluation);

        assertTrue(evaluation.getIdReportEvaluation() > 0);
    }

    @Test
    void save_thenGetById_returnsCorrectGrade() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        dao.save(evaluation);

        ReportEvaluation retrieved = dao.getById(evaluation.getIdReportEvaluation());

        assertNotNull(retrieved);
        assertEquals(9, retrieved.getGrade());
    }

    @Test
    void save_thenGetById_returnsCorrectFeedback() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        dao.save(evaluation);

        ReportEvaluation retrieved = dao.getById(evaluation.getIdReportEvaluation());

        assertEquals("Buen desempeño, mejorar la documentación.", retrieved.getFeedback());
    }

    @Test
    void save_thenGetByIdReport_returnsCorrectEvaluation() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        dao.save(evaluation);

        ReportEvaluation retrieved = dao.getByIdReport(ID_REPORTE);

        assertNotNull(retrieved);
        assertEquals(ID_REPORTE, retrieved.getIdReport());
    }

    @Test
    void getByIdReport_whenNoEvaluation_returnsNull() throws Exception {
        // El reporte ID_REPORTE existe pero no tiene evaluación aún
        ReportEvaluation retrieved = dao.getByIdReport(ID_REPORTE);

        assertNull(retrieved, "Sin evaluación debe retornar null");
    }

    @Test
    void save_thenGetAll_containsSavedEvaluation() throws Exception {
        dao.save(buildValidEvaluation());

        List<ReportEvaluation> all = dao.getAll();

        assertEquals(1, all.size());
    }

    @Test
    void save_withGradeZero_isAccepted() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        evaluation.setGrade(0);

        int result = dao.save(evaluation);

        assertEquals(1, result);
    }

    @Test
    void save_withGradeTen_isAccepted() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        evaluation.setGrade(10);

        int result = dao.save(evaluation);

        assertEquals(1, result);
    }
}
