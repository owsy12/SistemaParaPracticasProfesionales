import DataAccess.DataBaseConnection;
import Logic.DAO.EducationalExperienceDAO;
import Logic.DTOs.EducationalExperience;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EducationalExperienceDAOTest extends BaseDAOTest {

    private static final String EE_NAME = "Construcción de Software";
    private static final String UPDATED_EE_NAME = "Principios de Construcción de Software";

    private final EducationalExperienceDAO dao = new EducationalExperienceDAO();

    private EducationalExperience buildEducationalExperience(String nrc, int idProfessor) {
        EducationalExperience experience = new EducationalExperience();
        experience.setNrc(nrc);
        experience.setName(EE_NAME);
        experience.setIdProfessor(idProfessor);
        return experience;
    }

    @Test
    void testSaveValidEducationalExperienceReturnsTrue() throws Exception {
        int idProfessor = persistProfessor();
        boolean result = dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        assertTrue(result);
    }

    @Test
    void testSaveDuplicateNrcThrowsDuplicateEntryException() throws Exception {
        int idProfessor = persistProfessor();
        dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        assertThrows(DuplicateEntryException.class,
                () -> dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor)));
    }

    @Test
    void testFindByNrcAfterSaveReturnsNotNull() throws Exception {
        int idProfessor = persistProfessor();
        dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        EducationalExperience retrieved = dao.findByNrc(TestConstants.DEFAULT_NRC);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByNrcWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findByNrc(TestConstants.BLANK_TEXT));
    }

    @Test
    void testFindByNrcWithUnusedNrcReturnsNull() throws Exception {
        EducationalExperience retrieved = dao.findByNrc(TestConstants.UNUSED_NRC);
        assertNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws Exception {
        List<EducationalExperience> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterSaveReturnsOneElement() throws Exception {
        int idProfessor = persistProfessor();
        dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        List<EducationalExperience> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testUpdateEducationalExperienceReturnsTrue() throws Exception {
        int idProfessor = persistProfessor();
        dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        EducationalExperience experience = dao.findByNrc(TestConstants.DEFAULT_NRC);
        experience.setName(UPDATED_EE_NAME);
        boolean result = dao.update(experience);
        assertTrue(result);
    }

    @Test
    void testDeleteEducationalExperienceReturnsTrue() throws Exception {
        int idProfessor = persistProfessor();
        dao.save(buildEducationalExperience(TestConstants.ALTERNATE_NRC, idProfessor));
        boolean result = dao.delete(TestConstants.ALTERNATE_NRC);
        assertTrue(result);
    }

    @Test
    void testDeleteWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.delete(TestConstants.BLANK_TEXT));
    }

    private int persistProfessor() throws Exception {
        int idUser;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idUser = new UserTestDataBuilder()
                    .withRegistrationNumber("P88000001")
                    .withEmail("ee.test@uv.mx")
                    .withRole(TestConstants.ROLE_PROFESSOR)
                    .persist(connection);
            new ProfessorTestDataBuilder().withUserId(idUser).persist(connection);
        }
        return idUser;
    }
}
