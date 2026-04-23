package Logic;

import Logic.DAO.SelfEvaluationDAO;
import Logic.DTOs.SelfEvaluation;
import Logic.Exceptions.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelfEvaluationDAOTest extends BaseDAOTest {

    private final SelfEvaluationDAO dao = new SelfEvaluationDAO();

    private SelfEvaluation buildValidSelfEvaluation() {
        SelfEvaluation selfEvaluation = new SelfEvaluation();
        selfEvaluation.setIdIntern   (ID_PRACTICANTE);
        selfEvaluation.setIdProyect  (ID_PROYECTO);
        selfEvaluation.setPeriod     ("2025-01");
        selfEvaluation.setStatement01(4);
        selfEvaluation.setStatement02(5);
        selfEvaluation.setStatement03(3);
        selfEvaluation.setStatement04(4);
        selfEvaluation.setStatement05(5);
        selfEvaluation.setStatement06(4);
        selfEvaluation.setStatement07(3);
        selfEvaluation.setStatement08(5);
        selfEvaluation.setStatement09(4);
        selfEvaluation.setStatement10(5);
        selfEvaluation.setFinalScore (42);
        selfEvaluation.setPlaceAndDate("Xalapa, Ver., 15 de enero de 2025");
        selfEvaluation.setDocumentPath("/docs/autoevaluacion_2025_01.pdf");
        selfEvaluation.setStatus     ("Pendiente");
        return selfEvaluation;
    }

    @Test
    void save_withValidData_returnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidSelfEvaluation());

        assertEquals(1, result);
    }

    @Test
    void save_withValidData_assignsGeneratedId() throws Exception {
        SelfEvaluation selfEvaluation = buildValidSelfEvaluation();

        dao.save(selfEvaluation);

        assertTrue(selfEvaluation.getIdSelfEvalation() > 0);
    }

    @Test
    void save_thenGetById_returnsNotNull() throws Exception {
        SelfEvaluation selfEvaluation = buildValidSelfEvaluation();
        dao.save(selfEvaluation);

        SelfEvaluation retrieved = dao.getById(selfEvaluation.getIdSelfEvalation());

        assertNotNull(retrieved);
    }

    @Test
    void save_thenGetById_returnsCorrectPeriod() throws Exception {
        SelfEvaluation selfEvaluation = buildValidSelfEvaluation();
        dao.save(selfEvaluation);

        SelfEvaluation retrieved = dao.getById(selfEvaluation.getIdSelfEvalation());

        assertEquals("2025-01", retrieved.getPeriod());
    }

    @Test
    void save_thenGetById_returnsCorrectStatement01() throws Exception {
        SelfEvaluation selfEvaluation = buildValidSelfEvaluation();
        dao.save(selfEvaluation);

        SelfEvaluation retrieved = dao.getById(selfEvaluation.getIdSelfEvalation());

        assertEquals(4, retrieved.getStatement01());
    }

    @Test
    void save_thenGetById_returnsCorrectInternId() throws Exception {
        SelfEvaluation selfEvaluation = buildValidSelfEvaluation();
        dao.save(selfEvaluation);

        SelfEvaluation retrieved = dao.getById(selfEvaluation.getIdSelfEvalation());

        assertEquals(ID_PRACTICANTE, retrieved.getIdIntern());
    }

    @Test
    void save_thenGetAll_returnsOneElement() throws Exception {
        dao.save(buildValidSelfEvaluation());

        List<SelfEvaluation> all = dao.getAll();

        assertEquals(1, all.size());
    }

    @Test
    void getById_withNonExistentId_returnsNull() throws Exception {
        SelfEvaluation retrieved = dao.getById(9999);

        assertNull(retrieved);
    }

    @Test
    void save_duplicatePeriodSamePracticante_throwsDatabaseException() throws Exception {
        dao.save(buildValidSelfEvaluation());

        // UNIQUE KEY uq_autoev_prac_periodo
        assertThrows(ServiceException.class, () -> dao.save(buildValidSelfEvaluation()));
    }

    @Test
    void save_withDifferentPeriod_returnsOneRowAffected() throws Exception {
        dao.save(buildValidSelfEvaluation()); // periodo 2025-01

        SelfEvaluation second = buildValidSelfEvaluation();
        second.setPeriod("2025-02");

        int result = dao.save(second);

        assertEquals(1, result);
    }
}
