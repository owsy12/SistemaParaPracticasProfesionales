import Logic.DAO.InitialFormatDAO;
import Logic.DTOs.InitialFormat;
import Logic.Exceptions.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InitialFormatDAOTest extends BaseDAOTest {

    private static final String FORMAT_ASSIGNMENT_LETTER = "Carta de Asignación";
    private static final String FORMAT_SCHEDULE = "Horario";
    private static final String FORMAT_INSURANCE_CERTIFICATE = "Certificado de Seguro";
    private static final String FORMAT_ACTIVITIES_TIMELINE = "Cronograma de Actividades";
    private static final String DOCUMENT_PATH_PREFIX = "/docs/";
    private static final String DOCUMENT_EXTENSION = ".pdf";
    private static final int EXPECTED_TOTAL_FORMATS_PER_INTERN = 4;
    private static final int EXPECTED_TOTAL_FORMATS_TWO = 2;

    private final InitialFormatDAO dao = new InitialFormatDAO();

    private InitialFormat buildValidFormat(String formatType) {
        InitialFormat format = new InitialFormat();
        format.setIdIntern(ID_INTERN);
        format.setIdProject(ID_PROJECT);
        format.setFormatType(formatType);
        format.setFilePath(DOCUMENT_PATH_PREFIX + formatType.replace(" ", "_") + DOCUMENT_EXTENSION);
        format.setStatus(STATUS_PENDING);
        return format;
    }

    @Test
    void testSaveAssignmentLetterReturnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidFormat(FORMAT_ASSIGNMENT_LETTER));
        assertEquals(1, result);
    }

    @Test
    void testSaveValidFormatAssignsGeneratedId() throws Exception {
        InitialFormat format = buildValidFormat(FORMAT_SCHEDULE);
        dao.save(format);
        assertTrue(format.getIdInitialFormat() > 0);
    }

    @Test
    void testGetByIdAfterSaveReturnsNotNull() throws Exception {
        InitialFormat format = buildValidFormat(FORMAT_INSURANCE_CERTIFICATE);
        dao.save(format);
        InitialFormat retrieved = dao.getById(format.getIdInitialFormat());
        assertNotNull(retrieved);
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectFormatType() throws Exception {
        InitialFormat format = buildValidFormat(FORMAT_INSURANCE_CERTIFICATE);
        dao.save(format);
        InitialFormat retrieved = dao.getById(format.getIdInitialFormat());
        assertEquals(FORMAT_INSURANCE_CERTIFICATE, retrieved.getFormatType());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectFilePath() throws Exception {
        InitialFormat format = buildValidFormat(FORMAT_SCHEDULE);
        dao.save(format);
        InitialFormat retrieved = dao.getById(format.getIdInitialFormat());
        assertEquals(DOCUMENT_PATH_PREFIX + FORMAT_SCHEDULE + DOCUMENT_EXTENSION, retrieved.getFilePath());
    }

    @Test
    void testGetByIdAfterSaveReturnsCorrectStatus() throws Exception {
        InitialFormat format = buildValidFormat(FORMAT_ACTIVITIES_TIMELINE);
        dao.save(format);
        InitialFormat retrieved = dao.getById(format.getIdInitialFormat());
        assertEquals(STATUS_PENDING, retrieved.getStatus());
    }

    @Test
    void testGetByIdInternAfterSaveAllFormatsReturnsFour() throws Exception {
        dao.save(buildValidFormat(FORMAT_ASSIGNMENT_LETTER));
        dao.save(buildValidFormat(FORMAT_SCHEDULE));
        dao.save(buildValidFormat(FORMAT_INSURANCE_CERTIFICATE));
        dao.save(buildValidFormat(FORMAT_ACTIVITIES_TIMELINE));
        List<InitialFormat> formats = dao.getByIdIntern(ID_INTERN);
        assertEquals(EXPECTED_TOTAL_FORMATS_PER_INTERN, formats.size());
    }

    @Test
    void testGetAllAfterSavingTwoFormatsReturnsTwo() throws Exception {
        dao.save(buildValidFormat(FORMAT_ASSIGNMENT_LETTER));
        dao.save(buildValidFormat(FORMAT_SCHEDULE));
        List<InitialFormat> all = dao.getAll();
        assertEquals(EXPECTED_TOTAL_FORMATS_TWO, all.size());
    }

    @Test
    void testGetByIdInternWhenNoFormatsReturnsEmptyList() throws Exception {
        List<InitialFormat> formats = dao.getByIdIntern(ID_INTERN);
        assertTrue(formats.isEmpty());
    }

    @Test
    void testSaveDuplicateFormatTypeSameInternThrowsServiceException() throws Exception {
        dao.save(buildValidFormat(FORMAT_SCHEDULE));
        assertThrows(ServiceException.class, () -> dao.save(buildValidFormat(FORMAT_SCHEDULE)));
    }

    @Test
    void testSaveWithNullSubmissionDateReturnsOneRowAffected() throws Exception {
        InitialFormat format = buildValidFormat(FORMAT_ASSIGNMENT_LETTER);
        format.setSubmissionDate(null);
        int result = dao.save(format);
        assertEquals(1, result);
    }
}
