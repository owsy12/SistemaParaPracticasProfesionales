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

    private final EducationalExperienceDAO dao = new EducationalExperienceDAO();

    private EducationalExperience buildEducationalExperience(String nrc, int idProfessor) {
        EducationalExperience experience = new EducationalExperience();
        experience.setNrc(nrc);
        experience.setName(EE_NAME);
        experience.setIdProfessor(idProfessor);
        return experience;
    }

    @Test
    void testSaveValidEducationalExperienceReturnsTrue() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        boolean result = dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        assertTrue(result);
    }

    @Test
    void testSaveDuplicateNrcThrowsDuplicateEntryException() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        assertThrows(DuplicateEntryException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
            }
        });
    }

    @Test
    void testFindByNrcAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        EducationalExperience retrieved = dao.findByNrc(TestConstants.DEFAULT_NRC);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByNrcAfterSaveReturnsEqualObject() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        EducationalExperience experience = buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor);
        dao.save(experience);
        EducationalExperience retrieved = dao.findByNrc(TestConstants.DEFAULT_NRC);
        assertEquals(experience, retrieved);
    }

    @Test
    void testFindByNrcWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findByNrc(TestConstants.BLANK_TEXT);
            }
        });
    }

    @Test
    void testFindByNrcWithUnusedNrcReturnsNull() throws ServiceException, ValidationException {
        EducationalExperience retrieved = dao.findByNrc(TestConstants.UNUSED_NRC);
        assertNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        List<EducationalExperience> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterSaveReturnsOneElement() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        List<EducationalExperience> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testUpdateEducationalExperienceReturnsTrue() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        dao.save(buildEducationalExperience(TestConstants.DEFAULT_NRC, idProfessor));
        EducationalExperience experience = dao.findByNrc(TestConstants.DEFAULT_NRC);
        experience.setName(UPDATED_EE_NAME);
        boolean result = dao.update(experience);
        assertTrue(result);
    }

    @Test
    void testDeleteEducationalExperienceReturnsTrue() throws ServiceException, ValidationException {
        int idProfessor = persistProfessor();
        dao.save(buildEducationalExperience(TestConstants.ALTERNATE_NRC, idProfessor));
        boolean result = dao.delete(TestConstants.ALTERNATE_NRC);
        assertTrue(result);
    }

    @Test
    void testDeleteWithBlankNrcThrowsValidationException() {
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.delete(TestConstants.BLANK_TEXT);
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
