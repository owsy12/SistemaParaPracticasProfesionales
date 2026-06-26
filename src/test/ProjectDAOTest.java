import DataAccess.DataBaseConnection;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Project;
import Logic.Exceptions.DuplicateEntryException;
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

class ProjectDAOTest extends BaseDAOTest {

    private static final String NEW_PROJECT_NAME = "Sistema de Recursos Humanos";
    private static final String NEW_PROJECT_DESCRIPTION = "Aplicación web para nómina.";
    private static final String NEW_PROJECT_OBJECTIVE = "Automatizar trámites.";
    private static final String UPDATED_PROJECT_NAME = "Sistema de RH v2";
    private static final String PROJECT_PROFESSOR_REGISTRATION_NUMBER = "P99000001";
    private static final String PROJECT_PROFESSOR_EMAIL = "proj.prof@uv.mx";
    private static final LocalDate VALID_START = LocalDate.of(2025, 9, 1);
    private static final LocalDate VALID_END = LocalDate.of(2025, 12, 15);
    private static final LocalDate INVALID_END = LocalDate.of(2024, 12, 31);
    private static final int VALID_MAX_SLOTS = 6;
    private static final int VALID_AVAILABLE_SLOTS = 6;
    private static final int INVALID_MAX_SLOTS = 0;

    private final ProjectDAO projectDAO = new ProjectDAO();

    private Project buildProject(ProjectContext context, String name) {
        Project project = new Project();
        project.setIdOrganization(context.idOrganization);
        project.setIdTechnicalResponsible(context.idTechnical);
        project.setIdProfessor(context.idProfessor);
        project.setName(name);
        project.setDescription(NEW_PROJECT_DESCRIPTION);
        project.setObjective(NEW_PROJECT_OBJECTIVE);
        project.setStartDate(VALID_START);
        project.setEndDate(VALID_END);
        project.setMaximumPlaces(VALID_MAX_SLOTS);
        project.setAvailablePlaces(VALID_AVAILABLE_SLOTS);
        project.setNrc(TestConstants.DEFAULT_NRC);
        return project;
    }

    @Test
    void testSaveValidProjectReturnsTrue() throws ServiceException, ValidationException {
        ProjectContext context = persistFKDependencies();
        boolean result = projectDAO.saveProject(buildProject(context, NEW_PROJECT_NAME));
        assertTrue(result);
    }

    @Test
    void testSaveProjectWithStartAfterEndThrowsValidationException() throws ServiceException, ValidationException {
        ProjectContext context = persistFKDependencies();
        Project project = buildProject(context, NEW_PROJECT_NAME);
        project.setEndDate(INVALID_END);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                projectDAO.saveProject(project);
            }
        });
    }

    @Test
    void testSaveProjectWithZeroMaxSlotsThrowsValidationException() throws ServiceException, ValidationException {
        ProjectContext context = persistFKDependencies();
        Project project = buildProject(context, NEW_PROJECT_NAME);
        project.setMaximumPlaces(INVALID_MAX_SLOTS);
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                projectDAO.saveProject(project);
            }
        });
    }

    @Test
    void testSaveDuplicateNameSameOrganizationThrowsDuplicateEntryException() throws ServiceException, ValidationException {
        ProjectContext context = persistFKDependencies();
        projectDAO.saveProject(buildProject(context, NEW_PROJECT_NAME));
        assertThrows(DuplicateEntryException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                projectDAO.saveProject(buildProject(context, NEW_PROJECT_NAME));
            }
        });
    }

    @Test
    void testFindByIdAfterPersistReturnsNotNull() throws ServiceException, ValidationException {
        int idProject = persistProjectViaScene();
        Project retrieved = projectDAO.findById(idProject);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                projectDAO.findById(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        Project retrieved = projectDAO.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllReturnsOneElement() throws ServiceException, ValidationException {
        persistProjectViaScene();
        List<Project> projectList = projectDAO.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, projectList.size());
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<Project> projectList = projectDAO.findAll();
        assertTrue(projectList.isEmpty());
    }

    @Test
    void testFindAllAvailableReturnsOneElement() throws ServiceException, ValidationException {
        persistProjectViaScene();
        List<Project> allAvailable = projectDAO.findAllAvailable();
        assertEquals(TestConstants.SINGLE_RESULT, allAvailable.size());
    }

    @Test
    void testUpdateProjectReturnsTrue() throws ServiceException, ValidationException {
        int idProject = persistProjectViaScene();
        Project project = projectDAO.findById(idProject);
        project.setName(UPDATED_PROJECT_NAME);
        boolean result = projectDAO.update(project);
        assertTrue(result);
    }

    @Test
    void testCancelProjectReturnsTrue() throws ServiceException, ValidationException {
        int idProject = persistProjectViaScene();
        boolean result = projectDAO.cancelProject(idProject);
        assertTrue(result);
    }

    @Test
    void testDecrementAvailableSlotReturnsTrue() throws ServiceException, ValidationException {
        int idProject = persistProjectViaScene();
        boolean result = projectDAO.decrementAvailableSlot(idProject);
        assertTrue(result);
    }

    @Test
    void testIncrementAvailableSlotReturnsTrue() throws ServiceException, ValidationException {
        int idProject = persistProjectViaScene();
        boolean result = projectDAO.incrementAvailableSlot(idProject);
        assertTrue(result);
    }

    @Test
    void testExistsByNrcReturnsFalseForUnusedNrc() throws ServiceException, ValidationException {
        boolean exists = projectDAO.existsByNrcAndPeriod(TestConstants.UNUSED_NRC, TestConstants.DEFAULT_PERIOD);
        assertFalse(exists);
    }

    @Test
    void testExistsByNrcWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                projectDAO.existsByNrcAndPeriod(TestConstants.BLANK_TEXT, TestConstants.DEFAULT_PERIOD);
            }
        });
    }

    @Test
    void testDeleteProjectWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                projectDAO.deleteProject(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    private ProjectContext persistFKDependencies() throws ServiceException, ValidationException {
        ProjectContext context = new ProjectContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            context.idOrganization = new OrganizationTestDataBuilder().persist(connection);
            context.idProfessor = new UserTestDataBuilder()
                    .withRegistrationNumber(PROJECT_PROFESSOR_REGISTRATION_NUMBER)
                    .withEmail(PROJECT_PROFESSOR_EMAIL)
                    .withRole(TestConstants.ROLE_PROFESSOR)
                    .persist(connection);
            new ProfessorTestDataBuilder().withUserId(context.idProfessor).persist(connection);
            context.idTechnical = new TechnicalResponsibleTestDataBuilder()
                    .withOrganizationId(context.idOrganization)
                    .persist(connection);
            new EducationalExperienceTestDataBuilder()
                    .withProfessorId(context.idProfessor)
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return context;
    }

    private int persistProjectViaScene() throws ServiceException, ValidationException {
        int idProject;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            idProject = scene.getProjectId();
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idProject;
    }

    private static final class ProjectContext {
        int idOrganization;
        int idTechnical;
        int idProfessor;
    }
}
