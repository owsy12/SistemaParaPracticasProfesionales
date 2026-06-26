import DataAccess.DataBaseConnection;
import Logic.DAO.EducationalExperienceDAO;
import Logic.DTOs.EducationalExperience;
import Logic.Exceptions.DuplicateEntryException;
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

class EducationalExperienceDAOTest extends BaseDAOTest {

    private static final String EE_NAME = "Construcción de Software";
    private static final String UPDATED_EE_NAME = "Principios de Construcción de Software";

    private final EducationalExperienceDAO educationalExperienceDAO = new EducationalExperienceDAO();

    private EducationalExperience buildEducationalExperience(String nrc, int idProfessor) {
        EducationalExperience educationalExperience = new EducationalExperience();
        educationalExperience.setNrc(nrc);
        educationalExperience.setName(EE_NAME);
        educationalExperience.setIdProfessor(idProfessor);
        return educationalExperience;
    }

    @Test
    void testSaveValidEducationalExperienceReturnsTrue() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        boolean result = educationalExperienceDAO.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        assertTrue(result);
    }

    @Test
    void testSaveDuplicateNrcThrowsDuplicateEntryException() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        educationalExperienceDAO.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        assertThrows(DuplicateEntryException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                educationalExperienceDAO.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
            }
        });
    }

    @Test
    void testFindByNrcAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        educationalExperienceDAO.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        EducationalExperience retrieved = educationalExperienceDAO.findByNrc(TestConstants.DEFAULT_NRC);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByNrcAfterSaveReturnsEqualObject() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        EducationalExperience experience = buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor);
        educationalExperienceDAO.save(experience);
        EducationalExperience retrieved = educationalExperienceDAO.findByNrc(TestConstants.DEFAULT_NRC);
        assertEquals(experience, retrieved);
    }

    @Test
    void testFindByNrcWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                educationalExperienceDAO.findByNrc(TestConstants.BLANK_TEXT);
            }
        });
    }

    @Test
    void testFindByNrcWithUnusedNrcReturnsNull() throws ServiceException, ValidationException {
        EducationalExperience retrieved = educationalExperienceDAO.findByNrc(TestConstants.UNUSED_NRC);
        assertNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<EducationalExperience> educationalExperienceList = educationalExperienceDAO.findAll();
        assertTrue(educationalExperienceList.isEmpty());
    }

    @Test
    void testFindAllAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        educationalExperienceDAO.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        List<EducationalExperience> all = educationalExperienceDAO.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testUpdateEducationalExperienceReturnsTrue() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        educationalExperienceDAO.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        EducationalExperience experience = educationalExperienceDAO.findByNrc(TestConstants.DEFAULT_NRC);
        experience.setName(UPDATED_EE_NAME);
        boolean result = educationalExperienceDAO.update(experience);
        assertTrue(result);
    }

    @Test
    void testDeleteEducationalExperienceReturnsTrue() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        educationalExperienceDAO.save(buildEducationalExperience(TestConstants.ALTERNATE_NRC, idProfessor));
        boolean result = educationalExperienceDAO.delete(TestConstants.ALTERNATE_NRC);
        assertTrue(result);
    }

    @Test
    void testDeleteWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                educationalExperienceDAO.delete(TestConstants.BLANK_TEXT);
            }
        });
    }

    private int persistProfessor() throws ServiceException, ValidationException {
        int idUser;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idUser = new UserTestDataBuilder()
                    .withRegistrationNumber("P88000001")
                    .withEmail("ee.test@uv.mx")
                    .withRole(TestConstants.ROLE_PROFESSOR)
                    .persist(connection);
            new ProfessorTestDataBuilder().withUserId(idUser).persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idUser;
    }
}
