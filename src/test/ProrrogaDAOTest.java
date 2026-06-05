import DataAccess.DataBaseConnection;
import Logic.DAO.ProrrogaDAO;
import Logic.DTOs.Prorroga;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProrrogaDAOTest extends BaseDAOTest {

    private static final LocalDate ORIGINAL_END = LocalDate.of(2025, 3, 31);
    private static final LocalDate NEW_END = LocalDate.of(2025, 4, 30);

    private final ProrrogaDAO dao = new ProrrogaDAO();

    private Prorroga buildProrroga(int idActivity) {
        Prorroga prorroga = new Prorroga();
        prorroga.setIdActivity(idActivity);
        prorroga.setOriginalEndDate(ORIGINAL_END);
        prorroga.setNewEndDate(NEW_END);
        prorroga.setMotivo(TestConstants.DEFAULT_MOTIVE);
        return prorroga;
    }

    @Test
    void testSaveValidProrrogaReturnsPositiveId() throws Exception {
        int idActivity = persistActivity();
        int generatedId = dao.save(buildProrroga(idActivity));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveProrrogaWithZeroActivityIdThrowsValidationException() {
        Prorroga prorroga = buildProrroga(TestConstants.INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(prorroga));
    }

    @Test
    void testSaveProrrogaWithBlankMotiveThrowsValidationException() throws Exception {
        int idActivity = persistActivity();
        Prorroga prorroga = buildProrroga(idActivity);
        prorroga.setMotivo(TestConstants.BLANK_TEXT);
        assertThrows(ValidationException.class, () -> dao.save(prorroga));
    }

    private int persistActivity() throws Exception {
        int idActivity;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            idActivity = new ActivityTestDataBuilder()
                    .withProjectId(scene.getProjectId())
                    .persist(connection);
        }
        return idActivity;
    }
}
