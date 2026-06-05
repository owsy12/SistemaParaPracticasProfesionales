import DataAccess.DataBaseConnection;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Project;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
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

    private final ProjectDAO dao = new ProjectDAO();

    private Project buildProject(ProjectContext context, String name) {
        Project project = new Project();
        project.setIdOrganization(context.idOrganization);
        project.setIdTechnicalSupervisor(context.idTechnical);
        project.setIdProfessor(context.idProfessor);
        project.setName(name);
        project.setDescription(NEW_PROJECT_DESCRIPTION);
        project.setObjetivo(NEW_PROJECT_OBJECTIVE);
        project.setStartDate(VALID_START);
        project.setEndDate(VALID_END);
        project.setMaximumPlaces(VALID_MAX_SLOTS);
        project.setAvaliablePlaces(VALID_AVAILABLE_SLOTS);
        project.setNrc(TestConstants.DEFAULT_NRC);
        return project;
    }

    @Test
    void testSaveValidProjectReturnsTrue() throws Exception {
        ProjectContext context = persistFKDependencies();
        boolean result = dao.saveProject(buildProject(context, NEW_PROJECT_NAME));
        assertTrue(result);
    }

    @Test
    void testSaveProjectWithStartAfterEndThrowsValidationException() throws Exception {
        ProjectContext context = persistFKDependencies();
        Project project = buildProject(context, NEW_PROJECT_NAME);
        project.setEndDate(INVALID_END);
        assertThrows(ValidationException.class, () -> dao.saveProject(project));
    }

    @Test
    void testSaveProjectWithZeroMaxSlotsThrowsValidationException() throws Exception {
        ProjectContext context = persistFKDependencies();
        Project project = buildProject(context, NEW_PROJECT_NAME);
        project.setMaximumPlaces(INVALID_MAX_SLOTS);
        assertThrows(ValidationException.class, () -> dao.saveProject(project));
    }

    @Test
    void testSaveDuplicateNameSameOrganizationThrowsDuplicateEntryException() throws Exception {
        ProjectContext context = persistFKDependencies();
        dao.saveProject(buildProject(context, NEW_PROJECT_NAME));
        assertThrows(DuplicateEntryException.class,
                () -> dao.saveProject(buildProject(context, NEW_PROJECT_NAME)));
    }

    @Test
    void testFindByIdAfterPersistReturnsNotNull() throws Exception {
        int idProject = persistProjectViaScene();
        Project retrieved = dao.findById(idProject);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(TestConstants.INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        Project retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllReturnsOneElement() throws Exception {
        persistProjectViaScene();
        List<Project> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws Exception {
        List<Project> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAvailableReturnsOneElement() throws Exception {
        persistProjectViaScene();
        List<Project> available = dao.findAllAvailable();
        assertEquals(TestConstants.SINGLE_RESULT, available.size());
    }

    @Test
    void testUpdateProjectReturnsTrue() throws Exception {
        int idProject = persistProjectViaScene();
        Project project = dao.findById(idProject);
        project.setName(UPDATED_PROJECT_NAME);
        boolean result = dao.update(project);
        assertTrue(result);
    }

    @Test
    void testCancelProjectReturnsTrue() throws Exception {
        int idProject = persistProjectViaScene();
        boolean result = dao.cancelProject(idProject);
        assertTrue(result);
    }

    @Test
    void testDecrementAvailableSlotReturnsTrue() throws Exception {
        int idProject = persistProjectViaScene();
        boolean result = dao.decrementAvailableSlot(idProject);
        assertTrue(result);
    }

    @Test
    void testIncrementAvailableSlotReturnsTrue() throws Exception {
        int idProject = persistProjectViaScene();
        boolean result = dao.incrementAvailableSlot(idProject);
        assertTrue(result);
    }

    @Test
    void testExistsByNrcReturnsFalseForUnusedNrc() throws Exception {
        boolean exists = dao.existsByNrc(TestConstants.UNUSED_NRC);
        assertFalse(exists);
    }

    @Test
    void testExistsByNrcWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.existsByNrc(TestConstants.BLANK_TEXT));
    }

    @Test
    void testDeleteProjectWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.deleteProject(TestConstants.INVALID_ID_ZERO));
    }

    private ProjectContext persistFKDependencies() throws Exception {
        ProjectContext context = new ProjectContext();
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            context.idOrganization = new OrganizationTestDataBuilder().persist(connection);
            context.idProfessor = new UserTestDataBuilder()
                    .withRegistrationNumber(PROJECT_PROFESSOR_REGISTRATION_NUMBER)
                    .withEmail(PROJECT_PROFESSOR_EMAIL)
                    .withRole(TestConstants.ROLE_PROFESSOR)
                    .persist(connection);
            new ProfessorTestDataBuilder().withUserId(context.idProfessor).persist(connection);
            context.idTechnical = new TechnicalSupervisorTestDataBuilder()
                    .withOrganizationId(context.idOrganization)
                    .persist(connection);
            new EducationalExperienceTestDataBuilder()
                    .withProfessorId(context.idProfessor)
                    .persist(connection);
        }
        return context;
    }

    private int persistProjectViaScene() throws Exception {
        int idProject;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            TestScene scene = TestScene.createFullScene(connection);
            idProject = scene.getProjectId();
        }
        return idProject;
    }

    private static final class ProjectContext {
        int idOrganization;
        int idTechnical;
        int idProfessor;
    }
}
