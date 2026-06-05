import DataAccess.DataBaseConnection;
import Logic.DAO.InitialFormatDAO;
import Logic.DTOs.InitialFormat;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitialFormatDAOTest extends BaseDAOTest {

    private final InitialFormatDAO dao = new InitialFormatDAO();

    private InitialFormat buildInitialFormat(int idIntern, int idProject, String formatType) {
        InitialFormat initialFormat = new InitialFormat();
        initialFormat.setIdIntern(idIntern);
        initialFormat.setIdProject(idProject);
        initialFormat.setFormatType(formatType);
        initialFormat.setFilePath(TestConstants.DEFAULT_DOCUMENT_PATH);
        initialFormat.setStatus(TestConstants.STATUS_INITIAL_FORMAT_PENDING);
        initialFormat.setSubmissionDate(LocalDate.now());
        return initialFormat;
    }

    @Test
    void testSaveValidInitialFormatReturnsPositiveId() throws ServiceException, ValidationException {
        InitialFormatContext context = persistContext();
        int generatedId = dao.save(buildInitialFormat(
                context.idIntern, context.idProject, TestConstants.INITIAL_FORMAT_TYPE_ASSIGNMENT));
        assertTrue(generatedId > TestConstants.ZERO_RESULTS);
    }

    @Test
    void testSaveInitialFormatWithZeroInternIdThrowsValidationException() throws ServiceException, ValidationException {
        InitialFormatContext context = persistContext();
        InitialFormat initialFormat = buildInitialFormat(
                TestConstants.INVALID_ID_ZERO, context.idProject, TestConstants.INITIAL_FORMAT_TYPE_ASSIGNMENT);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(initialFormat);
            }
        });
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        InitialFormatContext context = persistContext();
        int idInitialFormat = dao.save(buildInitialFormat(
                context.idIntern, context.idProject, TestConstants.INITIAL_FORMAT_TYPE_ASSIGNMENT));
        InitialFormat retrieved = dao.getById(idInitialFormat);
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.getById(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testGetByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        InitialFormat retrieved = dao.getById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testGetAllAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        InitialFormatContext context = persistContext();
        dao.save(buildInitialFormat(
                context.idIntern, context.idProject, TestConstants.INITIAL_FORMAT_TYPE_ASSIGNMENT));
        List<InitialFormat> all = dao.getAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testGetAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<InitialFormat> all = dao.getAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testGetByIdInternAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        InitialFormatContext context = persistContext();
        dao.save(buildInitialFormat(
                context.idIntern, context.idProject, TestConstants.INITIAL_FORMAT_TYPE_ASSIGNMENT));
        List<InitialFormat> formats = dao.getByIdIntern(context.idIntern);
        assertEquals(TestConstants.SINGLE_RESULT, formats.size());
    }

    @Test
    void testGetByIdInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.getByIdIntern(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testFindPendingByInternAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        InitialFormatContext context = persistContext();
        dao.save(buildInitialFormat(
                context.idIntern, context.idProject, TestConstants.INITIAL_FORMAT_TYPE_ASSIGNMENT));
        List<InitialFormat> pending = dao.findPendingByIntern(context.idIntern);
        assertEquals(TestConstants.SINGLE_RESULT, pending.size());
    }

    @Test
    void testUpdateStatusReturnsOneRowAffected() throws ServiceException, ValidationException {
        InitialFormatContext context = persistContext();
        int idInitialFormat = dao.save(buildInitialFormat(
                context.idIntern, context.idProject, TestConstants.INITIAL_FORMAT_TYPE_SCHEDULE));
        InitialFormat updateData = dao.getById(idInitialFormat);
        updateData.setStatus(TestConstants.STATUS_INITIAL_FORMAT_DELIVERED);
        int result = dao.updateStatus(updateData);
        assertEquals(TestConstants.ONE_ROW_AFFECTED, result);
    }

    @Test
    void testDeleteByInternAndProjectReturnsTrue() throws ServiceException, ValidationException {
        InitialFormatContext context = persistContext();
        dao.save(buildInitialFormat(
                context.idIntern, context.idProject, TestConstants.INITIAL_FORMAT_TYPE_CERTIFICATE));
        boolean result = dao.deleteByInternAndProject(context.idIntern, context.idProject);
        assertTrue(result);
    }

    private InitialFormatContext persistContext() throws ServiceException, ValidationException {
        InitialFormatContext context = new InitialFormatContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return context;
    }

    private static final class InitialFormatContext {
        int idIntern;
        int idProject;
    }
}
