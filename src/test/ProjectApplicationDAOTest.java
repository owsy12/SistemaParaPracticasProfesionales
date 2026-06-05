import DataAccess.DataBaseConnection;
import Logic.DAO.ProjectApplicationDAO;
import Logic.DTOs.ProjectApplication;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectApplicationDAOTest extends BaseDAOTest {

    private final ProjectApplicationDAO dao = new ProjectApplicationDAO();

    private ProjectApplication buildProjectApplication(int idApplication, int idProject) {
        ProjectApplication projectApplication = new ProjectApplication();
        projectApplication.setIdApplication(idApplication);
        projectApplication.setIdProject(idProject);
        projectApplication.setPreferenceOrder(TestConstants.DEFAULT_PREFERENCE_ORDER);
        return projectApplication;
    }

    @Test
    void testCreateValidProjectApplicationReturnsTrue() throws ServiceException, ValidationException {
        ProjectApplicationContext context = persistDependencies();
        boolean result = dao.create(buildProjectApplication(context.idApplication, context.idProject));
        assertTrue(result);
    }

    @Test
    void testCreateProjectApplicationWithZeroApplicationIdThrowsValidationException() {
        ProjectApplication projectApplication =
                buildProjectApplication(TestConstants.INVALID_ID_ZERO, TestConstants.NON_EXISTENT_ID);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.create(projectApplication);
            }
        });
    }

    @Test
    void testFindByIdAfterCreateReturnsNotNull() throws ServiceException, ValidationException {
        ProjectApplicationContext context = persistDependencies();
        dao.create(buildProjectApplication(context.idApplication, context.idProject));
        List<ProjectApplication> all = dao.findAll();
        ProjectApplication retrieved = dao.findById(all.get(0).getIdProjectApplication());
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        ProjectApplication retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindByApplicationReturnsOneElement() throws ServiceException, ValidationException {
        ProjectApplicationContext context = persistDependencies();
        dao.create(buildProjectApplication(context.idApplication, context.idProject));
        List<ProjectApplication> byApplication = dao.findByApplication(context.idApplication);
        assertEquals(TestConstants.SINGLE_RESULT, byApplication.size());
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<ProjectApplication> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testDeleteProjectApplicationReturnsTrue() throws ServiceException, ValidationException {
        ProjectApplicationContext context = persistDependencies();
        dao.create(buildProjectApplication(context.idApplication, context.idProject));
        List<ProjectApplication> all = dao.findAll();
        boolean result = dao.delete(all.get(0).getIdProjectApplication());
        assertTrue(result);
    }

    @Test
    void testDeleteWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.delete(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testFindProjectIdsByInternReturnsOneElement() throws ServiceException, ValidationException {
        ProjectApplicationContext context = persistDependencies();
        dao.create(buildProjectApplication(context.idApplication, context.idProject));
        List<Integer> projectIds = dao.findProjectIdsByIntern(context.idIntern);
        assertEquals(TestConstants.SINGLE_RESULT, projectIds.size());
    }

    @Test
    void testFindProjectIdsByInternWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findProjectIdsByIntern(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    private ProjectApplicationContext persistDependencies() throws ServiceException, ValidationException {
        ProjectApplicationContext context = new ProjectApplicationContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            context.idIntern = scene.getInternId();
            context.idProject = scene.getProjectId();
            context.idApplication = new ApplicationTestDataBuilder()
                    .withInternId(scene.getInternId())
                    .withStatus(TestConstants.STATUS_PENDING)
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return context;
    }

    private static final class ProjectApplicationContext {
        int idIntern;
        int idProject;
        int idApplication;
    }
}
