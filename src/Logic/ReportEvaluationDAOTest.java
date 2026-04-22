package Logic;

import Logic.DAO.ReportEvaluationDAO;
import Logic.DTOs.ReportEvaluation;
import Logic.Exceptions.DatabaseException;
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

    @Test
    void save_withValidData_returnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidEvaluation());

        assertEquals(1, result);
    }

    @Test
    void save_withValidData_assignsGeneratedId() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();

        dao.save(evaluation);

        assertTrue(evaluation.getIdReportEvaluation() > 0);
    }

    @Test
    void save_thenGetById_returnsNotNull() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        dao.save(evaluation);

        ReportEvaluation retrieved = dao.getById(evaluation.getIdReportEvaluation());

        assertNotNull(retrieved);
    }

    @Test
    void save_thenGetById_returnsCorrectGrade() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        dao.save(evaluation);

        ReportEvaluation retrieved = dao.getById(evaluation.getIdReportEvaluation());

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
    void save_thenGetByIdReport_returnsNotNull() throws Exception {
        dao.save(buildValidEvaluation());

        ReportEvaluation retrieved = dao.getByIdReport(ID_REPORTE);

        assertNotNull(retrieved);
    }

    @Test
    void save_thenGetByIdReport_returnsCorrectReportId() throws Exception {
        dao.save(buildValidEvaluation());

        ReportEvaluation retrieved = dao.getByIdReport(ID_REPORTE);

        assertEquals(ID_REPORTE, retrieved.getIdReport());
    }

    @Test
    void getByIdReport_whenNoEvaluation_returnsNull() throws Exception {
        // El reporte ID_REPORTE existe pero no tiene evaluación aún
        ReportEvaluation retrieved = dao.getByIdReport(ID_REPORTE);

        assertNull(retrieved);
    }

    @Test
    void save_thenGetAll_returnsOneElement() throws Exception {
        dao.save(buildValidEvaluation());

        List<ReportEvaluation> all = dao.getAll();

        assertEquals(1, all.size());
    }

    @Test
    void save_withGradeZero_returnsOneRowAffected() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        evaluation.setGrade(0);

        int result = dao.save(evaluation);

        assertEquals(1, result);
    }

    @Test
    void save_withGradeTen_returnsOneRowAffected() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        evaluation.setGrade(10);

        int result = dao.save(evaluation);

        assertEquals(1, result);
    }
}
