import DataAccess.DataBaseConnection;
import Logic.DAO.ReportEvaluationDAO;
import Logic.DTOs.ReportEvaluation;
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

class ReportEvaluationDAOTest extends BaseDAOTest {

    private final ReportEvaluationDAO dao = new ReportEvaluationDAO();

    private ReportEvaluation buildEvaluation(int idReport) {
        ReportEvaluation evaluation = new ReportEvaluation();
        evaluation.setIdReport(idReport);
        evaluation.setGrade(TestConstants.DEFAULT_GRADE);
        evaluation.setFeedback(TestConstants.DEFAULT_FEEDBACK);
        evaluation.setEvaluationDate(new Date());
        return evaluation;
    }

    @Test
    void testSaveValidEvaluationReturnsOneRowAffected() throws ServiceException, ValidationException {
        int idReport = persistReport();
        int result = dao.save(buildEvaluation(idReport));
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testSaveEvaluationWithZeroReportIdThrowsValidationException() {
        ReportEvaluation evaluation = buildEvaluation(TestConstants.INVALID_ID_ZERO);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(evaluation);
            }
        });
    }

    @Test
    void testGetByIdReportAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        int idReport = persistReport();
        dao.save(buildEvaluation(idReport));
        ReportEvaluation retrieved = dao.getByIdReport(idReport);
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdReportWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.getByIdReport(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testGetByIdReportWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        ReportEvaluation retrieved = dao.getByIdReport(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        int idReport = persistReport();
        dao.save(buildEvaluation(idReport));
        List<ReportEvaluation> all = dao.getAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testGetAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<ReportEvaluation> all = dao.getAll();
        assertTrue(all.isEmpty());
    }

    private int persistReport() throws ServiceException, ValidationException {
        int idReport;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            idReport = new ReportTestDataBuilder()
                    .withInternId(scene.getInternId())
                    .withProjectId(scene.getProjectId())
                    .withProfessorId(scene.getProfessorId())
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idReport;
    }
}
