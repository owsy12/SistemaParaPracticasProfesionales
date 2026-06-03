package test.Logic;

import Logic.DAO.ReportObservationDAO;
import Logic.DTOs.ReportObservation;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportObservationDAOTest extends BaseDAOTest {

    private static final String OBSERVATION_COMMENT = "Falta detallar la metodología empleada.";
    private static final String OBSERVATION_COMMENT_SECOND = "Adjuntar evidencias firmadas.";
    private static final String BLANK_COMMENT = "  ";
    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -1;
    private static final int EXPECTED_TWO_OBSERVATIONS = 2;

    private final ReportObservationDAO dao = new ReportObservationDAO();

    private ReportObservation buildValidObservation(String comment) {
        ReportObservation observation = new ReportObservation();
        observation.setIdReport(ID_REPORT);
        observation.setIdProfessor(ID_PROFESSOR);
        observation.setComment(comment);
        return observation;
    }

    @Test
    void testSaveValidObservationReturnsTrue() throws Exception {
        boolean result = dao.save(buildValidObservation(OBSERVATION_COMMENT));
        assertTrue(result);
    }

    @Test
    void testSaveValidObservationAssignsGeneratedId() throws Exception {
        ReportObservation observation = buildValidObservation(OBSERVATION_COMMENT);
        dao.save(observation);
        assertTrue(observation.getIdObservation() > 0);
    }

    @Test
    void testSaveObservationWithZeroReportIdThrowsValidationException() {
        ReportObservation observation = buildValidObservation(OBSERVATION_COMMENT);
        observation.setIdReport(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(observation));
    }

    @Test
    void testSaveObservationWithZeroProfessorIdThrowsValidationException() {
        ReportObservation observation = buildValidObservation(OBSERVATION_COMMENT);
        observation.setIdProfessor(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(observation));
    }

    @Test
    void testSaveObservationWithNullCommentThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.save(buildValidObservation(null)));
    }

    @Test
    void testSaveObservationWithBlankCommentThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.save(buildValidObservation(BLANK_COMMENT)));
    }

    @Test
    void testFindByReportAfterSaveReturnsOneElement() throws Exception {
        dao.save(buildValidObservation(OBSERVATION_COMMENT));
        List<ReportObservation> observations = dao.findByReport(ID_REPORT);
        assertEquals(1, observations.size());
    }

    @Test
    void testFindByReportAfterSaveReturnsCorrectComment() throws Exception {
        dao.save(buildValidObservation(OBSERVATION_COMMENT));
        List<ReportObservation> observations = dao.findByReport(ID_REPORT);
        assertEquals(OBSERVATION_COMMENT, observations.get(0).getComment());
    }

    @Test
    void testFindByReportAfterSavingTwoReturnsTwo() throws Exception {
        dao.save(buildValidObservation(OBSERVATION_COMMENT));
        dao.save(buildValidObservation(OBSERVATION_COMMENT_SECOND));
        List<ReportObservation> observations = dao.findByReport(ID_REPORT);
        assertEquals(EXPECTED_TWO_OBSERVATIONS, observations.size());
    }

    @Test
    void testFindByReportWhenNoObservationsReturnsEmptyList() throws Exception {
        List<ReportObservation> observations = dao.findByReport(ID_REPORT);
        assertTrue(observations.isEmpty());
    }

    @Test
    void testFindByReportWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findByReport(INVALID_ID_ZERO));
    }

    @Test
    void testFindByReportWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findByReport(INVALID_ID_NEGATIVE));
    }
}
