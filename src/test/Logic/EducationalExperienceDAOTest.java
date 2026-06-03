package test.Logic;

import Logic.DAO.EducationalExperienceDAO;
import Logic.DTOs.EducationalExperience;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EducationalExperienceDAOTest extends BaseDAOTest {

    private static final String NEW_NRC = "20002";
    private static final String NEW_NAME = "Sistemas Distribuidos";
    private static final String UPDATED_NAME = "Sistemas Distribuidos Avanzados";
    private static final String NON_EXISTENT_NRC = "99999";
    private static final String BLANK_NRC = "  ";
    private static final int INVALID_PROFESSOR_ID = 0;

    private final EducationalExperienceDAO dao = new EducationalExperienceDAO();

    private EducationalExperience buildValidExperience() {
        EducationalExperience experience = new EducationalExperience();
        experience.setNrc(NEW_NRC);
        experience.setName(NEW_NAME);
        experience.setIdProfessor(ID_PROFESSOR);
        return experience;
    }

    @Test
    void testSaveValidExperienceReturnsTrue() throws Exception {
        boolean result = dao.save(buildValidExperience());
        assertTrue(result);
    }

    @Test
    void testSaveExperienceWithBlankNrcThrowsValidationException() {
        EducationalExperience experience = buildValidExperience();
        experience.setNrc(BLANK_NRC);
        assertThrows(ValidationException.class, () -> dao.save(experience));
    }

    @Test
    void testSaveExperienceWithNullNameThrowsValidationException() {
        EducationalExperience experience = buildValidExperience();
        experience.setName(null);
        assertThrows(ValidationException.class, () -> dao.save(experience));
    }

    @Test
    void testSaveExperienceWithInvalidProfessorIdThrowsValidationException() {
        EducationalExperience experience = buildValidExperience();
        experience.setIdProfessor(INVALID_PROFESSOR_ID);
        assertThrows(ValidationException.class, () -> dao.save(experience));
    }

    @Test
    void testSaveDuplicateNrcThrowsDuplicateEntryException() throws Exception {
        dao.save(buildValidExperience());
        assertThrows(DuplicateEntryException.class, () -> dao.save(buildValidExperience()));
    }

    @Test
    void testFindByNrcAfterSaveReturnsNotNull() throws Exception {
        dao.save(buildValidExperience());
        EducationalExperience retrieved = dao.findByNrc(NEW_NRC);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByNrcAfterSaveReturnsCorrectName() throws Exception {
        dao.save(buildValidExperience());
        EducationalExperience retrieved = dao.findByNrc(NEW_NRC);
        assertEquals(NEW_NAME, retrieved.getName());
    }

    @Test
    void testFindByNrcWithNonExistentNrcReturnsNull() throws Exception {
        EducationalExperience retrieved = dao.findByNrc(NON_EXISTENT_NRC);
        assertNull(retrieved);
    }

    @Test
    void testFindByNrcWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findByNrc(BLANK_NRC));
    }

    @Test
    void testFindAllAfterSaveReturnsTwoElements() throws Exception {
        dao.save(buildValidExperience());
        List<EducationalExperience> all = dao.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testUpdateExperienceReturnsTrue() throws Exception {
        dao.save(buildValidExperience());
        EducationalExperience experience = buildValidExperience();
        experience.setName(UPDATED_NAME);
        boolean result = dao.update(experience);
        assertTrue(result);
    }

    @Test
    void testUpdateExperiencePersistsNewName() throws Exception {
        dao.save(buildValidExperience());
        EducationalExperience experience = buildValidExperience();
        experience.setName(UPDATED_NAME);
        dao.update(experience);
        EducationalExperience retrieved = dao.findByNrc(NEW_NRC);
        assertEquals(UPDATED_NAME, retrieved.getName());
    }

    @Test
    void testDeleteExistingExperienceReturnsTrue() throws Exception {
        dao.save(buildValidExperience());
        boolean result = dao.delete(NEW_NRC);
        assertTrue(result);
    }

    @Test
    void testDeleteNonExistentExperienceReturnsFalse() throws Exception {
        boolean result = dao.delete(NON_EXISTENT_NRC);
        assertFalse(result);
    }

    @Test
    void testDeleteWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.delete(BLANK_NRC));
    }
}
