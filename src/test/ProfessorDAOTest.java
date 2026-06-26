import DataAccess.DataBaseConnection;
import Logic.DAO.ProfessorDAO;
import Logic.DTOs.Professor;
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

class ProfessorDAOTest extends BaseDAOTest {

    private static final String PROF_REGISTRATION_NUMBER = "F80000001";
    private static final String PROF_EMAIL = "prof.test@uv.mx";
    private static final String PROF_FIRST_NAME = "Lourdes";
    private static final String PROF_LAST_NAME = "Vázquez";
    private static final String PROF_ACADEMIC_AREA = "Sistemas Computacionales";

    private ProfessorDAO buildDAO() throws ServiceException, ValidationException {
        return new ProfessorDAO();
    }

    private Professor buildProfessor() {
        Professor professor = new Professor();
        professor.setRegistrationNumber(PROF_REGISTRATION_NUMBER);
        professor.setFirstName(PROF_FIRST_NAME);
        professor.setLastName(PROF_LAST_NAME);
        professor.setSecondLastName(PROF_LAST_NAME);
        professor.setPassword(TestConstants.DEFAULT_PASSWORD_HASH);
        professor.setEmail(PROF_EMAIL);
        professor.setStatus(TestConstants.STATUS_ACTIVE_USER);
        professor.setRole(TestConstants.ROLE_PROFESSOR);
        professor.setAcademicArea(PROF_ACADEMIC_AREA);
        return professor;
    }

    @Test
    void testSaveValidProfessorReturnsTrue() throws ServiceException, ValidationException {
        ProfessorDAO dao = buildDAO();
        boolean result = dao.saveProfessor(buildProfessor());
        assertTrue(result);
    }

    @Test
    void testFindByIdAfterSaveReturnsNotNull() throws ServiceException, ValidationException {
        int idUser = persistProfessorViaBuilders();
        ProfessorDAO dao = buildDAO();
        Professor retrieved = dao.findById(idUser);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() throws ServiceException, ValidationException {
        ProfessorDAO dao = buildDAO();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findById(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testFindByIdWithNegativeIdThrowsValidationException() throws ServiceException, ValidationException {
        ProfessorDAO dao = buildDAO();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.findById(TestConstants.INVALID_ID_NEGATIVE);
            }
        });
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws ServiceException, ValidationException {
        ProfessorDAO dao = buildDAO();
        Professor retrieved = dao.findById(TestConstants.NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllWithNoDataReturnsEmptyList() throws ServiceException, ValidationException {
        ProfessorDAO dao = buildDAO();
        List<Professor> all = dao.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void testFindAllAfterPersistReturnsOneElement() throws ServiceException, ValidationException {
        persistProfessorViaBuilders();
        ProfessorDAO dao = buildDAO();
        List<Professor> all = dao.findAll();
        assertEquals(TestConstants.SINGLE_RESULT, all.size());
    }

    @Test
    void testFindActiveProfessorsReturnsOnlyActive() throws ServiceException, ValidationException {
        persistProfessorViaBuilders();
        ProfessorDAO dao = buildDAO();
        List<Professor> active = dao.findActiveProfessors();
        assertEquals(TestConstants.SINGLE_RESULT, active.size());
    }

    @Test
    void testDeactivateProfessorWithZeroIdThrowsValidationException() throws ServiceException, ValidationException {
        ProfessorDAO dao = buildDAO();
        assertThrows(ValidationException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                dao.deactivateProfessor(TestConstants.INVALID_ID_ZERO);
            }
        });
    }

    @Test
    void testDeactivateProfessorReturnsTrue() throws ServiceException, ValidationException {
        int idUser = persistProfessorViaBuilders();
        ProfessorDAO dao = buildDAO();
        boolean result = dao.deactivateProfessor(idUser);
        assertTrue(result);
    }

    private int persistProfessorViaBuilders() throws ServiceException, ValidationException {
        int idUser;
        try (Connection connection = DataBaseConnection.connectDatabase()) {
            idUser = new UserTestDataBuilder()
                    .withRegistrationNumber(PROF_REGISTRATION_NUMBER)
                    .withEmail(PROF_EMAIL)
                    .withFirstName(PROF_FIRST_NAME)
                    .withLastName(PROF_LAST_NAME)
                    .withRole(TestConstants.ROLE_PROFESSOR)
                    .persist(connection);
            new ProfessorTestDataBuilder()
                    .withUserId(idUser)
                    .withAcademicArea(PROF_ACADEMIC_AREA)
                    .persist(connection);
        } catch (SQLException sqlException) {
            throw new ServiceException("Failed to access test database connection", sqlException);
        }
        return idUser;
    }
}
