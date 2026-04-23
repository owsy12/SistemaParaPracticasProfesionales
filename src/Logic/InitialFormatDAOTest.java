package Logic;

import Logic.DAO.InitialFormatDAO;
import Logic.DTOs.InitialFormat;
import Logic.Exceptions.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InitialFormatDAOTest extends BaseDAOTest {

    private final InitialFormatDAO dao = new InitialFormatDAO();

    private InitialFormat buildValidFormat(String formatType) {
        InitialFormat format = new InitialFormat();
        format.setIdIntern      (ID_PRACTICANTE);
        format.setFormatType    (formatType);
        format.setFilePath      ("/docs/" + formatType.replace(" ", "_") + ".pdf");
        format.setStatus        ("Pendiente");
        format.setSubmissionDate(new Date());
        return format;
    }

    @Test
    void save_withAssignmentLetter_returnsOneRowAffected() throws Exception {
        int result = dao.save(buildValidFormat("Carta de Asignación"));

        assertEquals(1, result);
    }

    @Test
    void save_withValidData_assignsGeneratedId() throws Exception {
        InitialFormat format = buildValidFormat("Horario");

        dao.save(format);

        assertTrue(format.getIdInitialFormat() > 0);
    }

    @Test
    void save_thenGetById_returnsNotNull() throws Exception {
        InitialFormat format = buildValidFormat("Certificado de Seguro");
        dao.save(format);

        InitialFormat retrieved = dao.getById(format.getIdInitialFormat());

        assertNotNull(retrieved);
    }

    @Test
    void save_thenGetById_returnsCorrectFormatType() throws Exception {
        InitialFormat format = buildValidFormat("Certificado de Seguro");
        dao.save(format);

        InitialFormat retrieved = dao.getById(format.getIdInitialFormat());

        assertEquals("Certificado de Seguro", retrieved.getFormatType());
    }

    @Test
    void save_thenGetById_returnsCorrectFilePath() throws Exception {
        InitialFormat format = buildValidFormat("Horario");
        dao.save(format);

        InitialFormat retrieved = dao.getById(format.getIdInitialFormat());

        assertEquals("/docs/Horario.pdf", retrieved.getFilePath());
    }

    @Test
    void save_thenGetById_returnsCorrectStatus() throws Exception {
        InitialFormat format = buildValidFormat("Cronograma de Actividades");
        dao.save(format);

        InitialFormat retrieved = dao.getById(format.getIdInitialFormat());

        assertEquals("Pendiente", retrieved.getStatus());
    }

    @Test
    void save_fourFormats_thenGetByIdIntern_returnsFour() throws Exception {
        dao.save(buildValidFormat("Carta de Asignación"));
        dao.save(buildValidFormat("Horario"));
        dao.save(buildValidFormat("Certificado de Seguro"));
        dao.save(buildValidFormat("Cronograma de Actividades"));

        List<InitialFormat> formats = dao.getByIdIntern(ID_PRACTICANTE);

        assertEquals(4, formats.size());
    }

    @Test
    void save_twoFormats_thenGetAll_returnsTwo() throws Exception {
        dao.save(buildValidFormat("Carta de Asignación"));
        dao.save(buildValidFormat("Horario"));

        List<InitialFormat> all = dao.getAll();

        assertEquals(2, all.size());
    }

    @Test
    void getByIdIntern_whenNoFormats_returnsEmptyList() throws Exception {
        List<InitialFormat> formats = dao.getByIdIntern(ID_PRACTICANTE);

        assertTrue(formats.isEmpty());
    }

    @Test
    void save_duplicateFormatTypeSamePracticante_throwsDatabaseException() throws Exception {
        dao.save(buildValidFormat("Horario"));

        // UNIQUE KEY uq_fmt_prac_tipo: no permite el mismo tipo dos veces para el mismo practicante
        assertThrows(ServiceException.class, () -> dao.save(buildValidFormat("Horario")));
    }

    @Test
    void save_withNullSubmissionDate_returnsOneRowAffected() throws Exception {
        InitialFormat format = buildValidFormat("Carta de Asignación");
        format.setSubmissionDate(null); // fecha_entrega es nullable en BD

        int result = dao.save(format);

        assertEquals(1, result);
    }
}
