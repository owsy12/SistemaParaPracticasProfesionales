import DataAccess.DataBaseConnection;
import Logic.DAO.ReportObservationDAO;
import Logic.DTOs.ReportObservation;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportObservationDAOTest extends BaseDAOTest {

    private final ReportObservationDAO dao = new ReportObservationDAO();

    private ReportObservation buildObservation(int idReport, int idProfessor) {
        ReportObservation observation = new ReportObservation();
        observation.setIdReport(idReport);
        observation.setIdProfessor(idProfessor);
        observation.setComment(TestConstants.DEFAULT_COMMENT);
        observation.setObservationDate(LocalDateTime.now());
        return observation;
    }

    @Test
    void testSaveValidObservationReturnsTrue() throws Exception {
        ObservationContext context = persistContext();
        boolean result = dao.save(buildObservation(context.idReport, context.idProfessor));
        assertTrue(result);
    }

    @Test
    void testSaveObservationWithZeroReportIdThrowsValidationException() throws Exception {
        ObservationContext context = persistContext();
        ReportObservation observation = buildObservation(TestConstants.INVALID_ID_ZERO, context.idProfessor);
        assertThrows(ValidationException.class, () -> dao.save(observation));
    }

    @Test
    void testFindByReportAfterSaveReturnsOneElement() throws Exception {
        ObservationContext context = persistContext();
        dao.save(buildObservation(context.idReport, context.idProfessor));
        List<ReportObservation> observations = dao.findByReport(context.idReport);
        assertEquals(TestConstants.SINGLE_RESULT, observations.size());
    }

    @Test
    void testFindByReportWithNoDataReturnsEmptyList() throws Exception {
        ObservationContext context = persistContext();
        List<ReportObservation> observations = dao.findByReport(context.idReport);
        assertTrue(observations.isEmpty());
    }

    @Test
    void testFindByReportWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.findByReport(TestConstants.INVALID_ID_ZERO));
    }

    private ObservationContext persistContext() throws Exception {
        ObservationContext context = new ObservationContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idProfessor = scene.getProfessorId();
            context.idReport = new ReportTestDataBuilder()
                    .withInternId(scene.getInternId())
                    .withProjectId(scene.getProjectId())
                    .withProfessorId(scene.getProfessorId())
                    .persist(connection);
        }
        return context;
    }

    private static final class ObservationContext {
        int idReport;
        int idProfessor;
    }
}
