import DataAccess.DataBaseConnection;
import Logic.DAO.ProrrogaDAO;
import Logic.DTOs.Prorroga;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProrrogaDAOTest extends BaseDAOTest {

    private static final int SUPPORT_ACTIVITY_ID = 1;
    private static final int INVALID_ACTIVITY_ID_ZERO = 0;
    private static final LocalDate ORIGINAL_END_DATE = LocalDate.of(2025, 4, 15);
    private static final LocalDate NEW_END_DATE = LocalDate.of(2025, 5, 30);
    private static final String EXTENSION_REASON = "Cambio de alcance del proyecto.";
    private static final String BLANK_REASON = "  ";

    private final ProrrogaDAO dao = new ProrrogaDAO();

    private void insertSupportActivity() throws Exception {
        try (Connection connection = DataBaseConnection.connectDatabase();
             Statement statement = connection.createStatement()) {
            statement.execute(
                "INSERT INTO actividad (id_actividad, id_proyecto, nombre, descripcion, " +
                "semana_inicio_plan, semana_fin_plan) VALUES (" + SUPPORT_ACTIVITY_ID + ", " +
                ID_PROJECT + ", 'Levantamiento de requerimientos', 'Entrevistar al cliente', 1, 4)"
            );
        }
    }

    private Prorroga buildValidProrroga() {
        Prorroga prorroga = new Prorroga();
        prorroga.setIdActividad(SUPPORT_ACTIVITY_ID);
        prorroga.setFechaFinOriginal(ORIGINAL_END_DATE);
        prorroga.setFechaFinNueva(NEW_END_DATE);
        prorroga.setMotivo(EXTENSION_REASON);
        return prorroga;
    }

    @Test
    void testSaveValidProrrogaReturnsGeneratedId() throws Exception {
        insertSupportActivity();
        int generatedId = dao.save(buildValidProrroga());
        assertTrue(generatedId > 0);
    }

    @Test
    void testSaveValidProrrogaAssignsGeneratedId() throws Exception {
        insertSupportActivity();
        Prorroga prorroga = buildValidProrroga();
        dao.save(prorroga);
        assertTrue(prorroga.getIdProrroga() > 0);
    }

    @Test
    void testSaveProrrogaWithZeroActivityIdThrowsValidationException() {
        Prorroga prorroga = buildValidProrroga();
        prorroga.setIdActividad(INVALID_ACTIVITY_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.save(prorroga));
    }

    @Test
    void testSaveProrrogaWithNullReasonThrowsValidationException() {
        Prorroga prorroga = buildValidProrroga();
        prorroga.setMotivo(null);
        assertThrows(ValidationException.class, () -> dao.save(prorroga));
    }

    @Test
    void testSaveProrrogaWithBlankReasonThrowsValidationException() {
        Prorroga prorroga = buildValidProrroga();
        prorroga.setMotivo(BLANK_REASON);
        assertThrows(ValidationException.class, () -> dao.save(prorroga));
    }

    @Test
    void testSaveProrrogaWithNullNewEndDateThrowsValidationException() {
        Prorroga prorroga = buildValidProrroga();
        prorroga.setFechaFinNueva(null);
        assertThrows(ValidationException.class, () -> dao.save(prorroga));
    }
}
