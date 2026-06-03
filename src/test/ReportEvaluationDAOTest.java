import Logic.DAO.ReportEvaluationDAO;
import Logic.DTOs.ReportEvaluation;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportEvaluationDAOTest extends BaseDAOTest {

    private static final int EVALUATION_GRADE = 9;
    private static final int EVALUATION_GRADE_MIN = 0;
    private static final int EVALUATION_GRADE_MAX = 10;
    private static final String EVALUATION_FEEDBACK = "Buen desempeño, mejorar la documentación.";

    private final ReportEvaluationDAO dao = new ReportEvaluationDAO();

    private ReportEvaluation buildValidEvaluation() {
        ReportEvaluation evaluation = new ReportEvaluation();
        evaluation.setIdReport(ID_REPORT);
        evaluation.setGrade(EVALUATION_GRADE);
        evaluation.setFeedback(EVALUATION_FEEDBACK);
        evaluation.setEvaluationDate(new Date());
        return evaluation;
    }

    @Test
    void testSaveValidEvaluationReturnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidEvaluation());
        assertEquals(1, result);
    }

    @Test
    void testSaveValidEvaluationAssignsGeneratedId() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        dao.save(evaluation);
        assertTrue(evaluation.getIdReportEvaluation() > 0);
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        dao.save(evaluation);
        ReportEvaluation retrieved = dao.getById(evaluation.getIdReportEvaluation());
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectGrade() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        dao.save(evaluation);
        ReportEvaluation retrieved = dao.getById(evaluation.getIdReportEvaluation());
        assertEquals(EVALUATION_GRADE, retrieved.getGrade());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectFeedback() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        dao.save(evaluation);
        ReportEvaluation retrieved = dao.getById(evaluation.getIdReportEvaluation());
        assertEquals(EVALUATION_FEEDBACK, retrieved.getFeedback());
    }

    @Test
    void testGetByIdReportAfterSaveReturnsNotNull() throws Exception {
        dao.save(buildValidEvaluation());
        ReportEvaluation retrieved = dao.getByIdReport(ID_REPORT);
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdReportAfterSaveReturnsCorrectReportId() throws Exception {
        dao.save(buildValidEvaluation());
        ReportEvaluation retrieved = dao.getByIdReport(ID_REPORT);
        assertEquals(ID_REPORT, retrieved.getIdReport());
    }

    @Test
    void testGetByIdReportWhenNoEvaluationReturnsNull() throws Exception {
        ReportEvaluation retrieved = dao.getByIdReport(ID_REPORT);
        assertNull(retrieved);
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws Exception {
        dao.save(buildValidEvaluation());
        List<ReportEvaluation> all = dao.getAll();
        assertEquals(1, all.size());
    }

    @Test
    void testSaveWithGradeZeroReturnsOneRowAffected() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        evaluation.setGrade(EVALUATION_GRADE_MIN);
        int result = dao.save(evaluation);
        assertEquals(1, result);
    }

    @Test
    void testSaveWithGradeTenReturnsOneRowAffected() throws Exception {
        ReportEvaluation evaluation = buildValidEvaluation();
        evaluation.setGrade(EVALUATION_GRADE_MAX);
        int result = dao.save(evaluation);
        assertEquals(1, result);
    }
}
