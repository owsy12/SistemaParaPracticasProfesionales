import DataAccess.DataBaseConnection;
import Logic.DAO.ProjectDAO;
import Logic.DTOs.Project;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectDAOTest extends BaseDAOTest {

    private static final String NEW_PROJECT_NAME = "Sistema de Recursos Humanos";
    private static final String NEW_PROJECT_DESCRIPTION = "Aplicación web para nómina y vacaciones.";
    private static final String NEW_PROJECT_OBJECTIVE = "Automatizar trámites del área de RH.";
    private static final LocalDate START_DATE = LocalDate.of(2025, 9, 1);
    private static final LocalDate END_DATE = LocalDate.of(2025, 12, 15);
    private static final LocalDate INVALID_END_BEFORE_START = LocalDate.of(2024, 12, 31);
    private static final int MAX_PLACES = 6;
    private static final int AVAILABLE_PLACES = 6;
    private static final int ZERO_MAX_PLACES = 0;
    private static final int INVALID_ID_ZERO = 0;
    private static final int NON_EXISTENT_ID = 9999;
    private static final String UPDATED_NAME = "Sistema de RH v2";
    private static final String STATUS_CANCELLED = "Cancelado";
    private static final String BLANK_NRC = "  ";

    private final ProjectDAO dao = new ProjectDAO();

    private Project buildValidProject(String name) {
        Project project = new Project();
        project.setIdOrganization(ID_ORGANIZATION);
        project.setIdTechnicalSupervisor(ID_TECHNICAL);
        project.setIdProfessor(ID_PROFESSOR);
        project.setName(name);
        project.setDescription(NEW_PROJECT_DESCRIPTION);
        project.setObjetivo(NEW_PROJECT_OBJECTIVE);
        project.setStartDate(START_DATE);
        project.setEndDate(END_DATE);
        project.setMaximumPlaces(MAX_PLACES);
        project.setAvaliablePlaces(AVAILABLE_PLACES);
        project.setNrc(NRC_EDUCATIONAL_EXPERIENCE);
        return project;
    }

    @Test
    void testSaveValidProjectReturnsTrue() throws Exception {
        boolean result = dao.saveProject(buildValidProject(NEW_PROJECT_NAME));
        assertTrue(result);
    }

    @Test
    void testSaveValidProjectAssignsGeneratedId() throws Exception {
        Project project = buildValidProject(NEW_PROJECT_NAME);
        dao.saveProject(project);
        assertTrue(project.getIdProyect() > 0);
    }

    @Test
    void testSaveProjectWithStartAfterEndThrowsValidationException() {
        Project project = buildValidProject(NEW_PROJECT_NAME);
        project.setEndDate(INVALID_END_BEFORE_START);
        assertThrows(ValidationException.class, () -> dao.saveProject(project));
    }

    @Test
    void testSaveProjectWithZeroMaxPlacesThrowsValidationException() {
        Project project = buildValidProject(NEW_PROJECT_NAME);
        project.setMaximumPlaces(ZERO_MAX_PLACES);
        assertThrows(ValidationException.class, () -> dao.saveProject(project));
    }

    @Test
    void testSaveDuplicateNameSameOrganizationThrowsDuplicateEntryException() throws Exception {
        dao.saveProject(buildValidProject(NEW_PROJECT_NAME));
        assertThrows(DuplicateEntryException.class,
                () -> dao.saveProject(buildValidProject(NEW_PROJECT_NAME)));
    }

    @Test
    void testFindByIdReturnsSupportProject() throws Exception {
        Project retrieved = dao.findById(ID_PROJECT);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        Project retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllReturnsAtLeastOneElement() throws Exception {
        List<Project> all = dao.findAll();
        assertEquals(1, all.size());
    }

    @Test
    void testFindAllAvailableReturnsAtLeastOneElement() throws Exception {
        List<Project> available = dao.findAllAvailable();
        assertEquals(1, available.size());
    }

    @Test
    void testUpdateProjectReturnsTrue() throws Exception {
        Project project = dao.findById(ID_PROJECT);
        project.setName(UPDATED_NAME);
        boolean result = dao.update(project);
        assertTrue(result);
    }

    @Test
    void testUpdateProjectPersistsNewName() throws Exception {
        Project project = dao.findById(ID_PROJECT);
        project.setName(UPDATED_NAME);
        dao.update(project);
        Project retrieved = dao.findById(ID_PROJECT);
        assertEquals(UPDATED_NAME, retrieved.getName());
    }

    @Test
    void testCancelProjectReturnsTrue() throws Exception {
        boolean result = dao.cancelProject(ID_PROJECT);
        assertTrue(result);
    }

    @Test
    void testCancelProjectPersistsCancelledStatus() throws Exception {
        dao.cancelProject(ID_PROJECT);
        String status = readProjectStatusDirectly(ID_PROJECT);
        assertEquals(STATUS_CANCELLED, status);
    }

    private String readProjectStatusDirectly(int idProject) throws Exception {
        String status = null;
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT estado FROM proyecto WHERE id_proyecto = ?")) {
            statement.setInt(1, idProject);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    status = resultSet.getString("estado");
                }
            }
        }
        return status;
    }

    @Test
    void testDecrementAvailableSlotReturnsTrue() throws Exception {
        boolean result = dao.decrementAvailableSlot(ID_PROJECT);
        assertTrue(result);
    }

    @Test
    void testDecrementAvailableSlotPersistsDecrement() throws Exception {
        int previous = dao.findById(ID_PROJECT).getAvaliablePlaces();
        dao.decrementAvailableSlot(ID_PROJECT);
        Project retrieved = dao.findById(ID_PROJECT);
        assertEquals(previous - 1, retrieved.getAvaliablePlaces());
    }

    @Test
    void testIncrementAvailableSlotPersistsIncrement() throws Exception {
        int previous = dao.findById(ID_PROJECT).getAvaliablePlaces();
        dao.incrementAvailableSlot(ID_PROJECT);
        Project retrieved = dao.findById(ID_PROJECT);
        assertEquals(previous + 1, retrieved.getAvaliablePlaces());
    }

    @Test
    void testExistsByNrcReturnsFalseForUnusedNrc() throws Exception {
        boolean exists = dao.existsByNrc("99999");
        assertFalse(exists);
    }

    @Test
    void testExistsByNrcWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.existsByNrc(BLANK_NRC));
    }

    @Test
    void testDeleteProjectWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.deleteProject(INVALID_ID_ZERO));
    }

    @Test
    void testFindByProfessorAvailableReturnsSupportProject() throws Exception {
        List<Project> projects = dao.findByProfessorAvailable(ID_PROFESSOR);
        assertEquals(1, projects.size());
    }
}
