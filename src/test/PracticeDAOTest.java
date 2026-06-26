import DataAccess.DataBaseConnection;
import Logic.DAO.PracticeDAO;
import Logic.DTOs.Practice;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracticeDAOTest extends BaseDAOTest {

    private static final LocalDate PRACTICE_START = LocalDate.of(2025, 1, 15);
    private static final LocalDate PRACTICE_END = LocalDate.of(2025, 7, 15);
    private static final LocalDate UPDATED_END = LocalDate.of(2025, 8, 15);

    private final PracticeDAO practiceDAO = new PracticeDAO();

    private Practice buildPractice(int idIntern, String nrc) {
        Practice practice = new Practice();
        practice.setNrc(nrc);
        practice.setPeriod(TestConstants.DEFAULT_PERIOD);
        practice.setIdIntern(idIntern);
        practice.setStartDate(PRACTICE_START);
        practice.setEndDate(PRACTICE_END);
        practice.setStatus(TestConstants.STATUS_PRACTICE_ACTIVE);
        return practice;
    }

    @Test
    void testSaveValidPracticeReturnsTrue() throws ServiceException, ValidationException {
        TestScene scene = persistScene();
        boolean result = practiceDAO.save(buildPractice(scene.getInternId(), scene.getNrc()));
        assertTrue(result);
    }

    @Test
    void testSavePracticeWithZeroInternIdThrowsValidationException() throws ServiceException, ValidationException {
        TestScene scene = persistScene();
        Practice practice = buildPractice(TestConstants.INVALID_ID_ZERO, scene.getNrc());
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                practiceDAO.save(practice);
            }
        });
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        TestScene scene = persistScene();
        int idPractice = persistPractice(scene);
        Practice retrieved = practiceDAO.findById(idPractice);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                practiceDAO.findById(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        Practice retrieved = practiceDAO.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByNrcReturnsOneElement() throws ServiceException, ValidationException {
        TestScene scene = persistScene();
        persistPractice(scene);
        List<Practice> practiceList = practiceDAO.findByNrc(scene.getNrc());
        assertEquals(TestConstants.SINGLE_RESULT, practiceList.size());
    }

    @Test
    void testFindByNrcWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                practiceDAO.findByNrc(TestConstants.BLANK_TEXT);
            }
        });
    }

    @Test
    void testFindByInternReturnsOneElement() throws ServiceException, ValidationException {
        TestScene scene = persistScene();
        persistPractice(scene);
        List<Practice> byIntern = practiceDAO.findByIntern(scene.getInternId());
        assertEquals(TestConstants.SINGLE_RESULT, byIntern.size());
    }

    @Test
    void testUpdatePracticeReturnsTrue() throws ServiceException, ValidationException {
        TestScene scene = persistScene();
        int idPractice = persistPractice(scene);
        Practice practice = practiceDAO.findById(idPractice);
        practice.setEndDate(UPDATED_END);
        boolean result = practiceDAO.update(practice);
        assertTrue(result);
    }

    @Test
    void testDeletePracticeReturnsTrue() throws ServiceException, ValidationException {
        TestScene scene = persistScene();
        int idPractice = persistPractice(scene);
        boolean result = practiceDAO.delete(idPractice);
        assertTrue(result);
    }

    @Test
    void testDeletePracticeWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                practiceDAO.delete(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testFindActiveByInternReturnsNotNull() throws ServiceException, ValidationException {
        TestScene scene = persistScene();
        persistPractice(scene);
        Practice active = practiceDAO.findActiveByIntern(scene.getInternId());
        assertNotNull(active);
    }

    @Test
    void testHasConcludedPracticeReturnsFalseForActive() throws ServiceException, ValidationException {
        TestScene scene = persistScene();
        persistPractice(scene);
        boolean concluded = practiceDAO.hasConcludedPractice(scene.getInternId());
        assertFalse(concluded);
    }

    @Test
    void testConcludeActiveByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                practiceDAO.concludeActiveByIntern(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    private TestScene persistScene() throws ServiceException, ValidationException {
        TestScene scene;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            scene = TestScene.createFullScene(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return scene;
    }

    private int persistPractice(TestScene scene) throws ServiceException, ValidationException {
        int idPractice;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idPractice = new PracticeTestDataBuilder()
                    .withInternId(scene.getInternId())
                    .withNrc(scene.getNrc())
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idPractice;
    }
}
