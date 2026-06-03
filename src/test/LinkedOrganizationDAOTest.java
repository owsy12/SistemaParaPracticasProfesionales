package test.Logic;

import DataAccess.DataBaseConnection;
import Logic.DAO.LinkedOrganizationDAO;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinkedOrganizationDAOTest extends BaseDAOTest {

    private static final String NEW_ORG_NAME = "InnovaSoft";
    private static final String NEW_ORG_EMAIL = "contacto@innovasoft.mx";
    private static final String NEW_ORG_ADDRESS = "Calle Reforma 123";
    private static final String NEW_ORG_SECTOR = "Software";
    private static final String UPDATED_NAME = "InnovaSoft Avanzado";
    private static final String ORG_STATUS_INACTIVE = "Inactiva";
    private static final int INVALID_ID_ZERO = 0;
    private static final int INVALID_ID_NEGATIVE = -1;
    private static final int NON_EXISTENT_ID = 9999;
    private static final int SECONDARY_ORG_ID = 2;

    private final LinkedOrganizationDAO dao = new LinkedOrganizationDAO();

    private void insertSecondaryOrganization() throws Exception {
        try (Connection connection = DataBaseConnection.connectDatabase();
             Statement statement = connection.createStatement()) {
            statement.execute(
                "INSERT INTO organizacion_vinculada (id_organizacion, nombre_organizacion, " +
                "correo_organizacion, direccion, sector, estado) VALUES " +
                "(" + SECONDARY_ORG_ID + ", 'DataLabs', 'contacto@datalabs.mx', " +
                "'Av. Tec 5', 'Datos', 'Activa')"
            );
        }
    }

    private LinkedOrganization buildValidOrganization() {
        return new LinkedOrganization(0, NEW_ORG_NAME, NEW_ORG_SECTOR, NEW_ORG_ADDRESS, NEW_ORG_EMAIL);
    }

    @Test
    void testSaveValidOrganizationReturnsTrue() throws Exception {
        boolean result = dao.saveLinkedOrganization(buildValidOrganization());
        assertTrue(result);
    }

    @Test
    void testSaveDuplicateNameThrowsDuplicateEntryException() throws Exception {
        dao.saveLinkedOrganization(buildValidOrganization());
        assertThrows(DuplicateEntryException.class,
                () -> dao.saveLinkedOrganization(buildValidOrganization()));
    }

    @Test
    void testFindByIdReturnsSupportOrganization() throws Exception {
        LinkedOrganization retrieved = dao.findById(ID_ORGANIZATION);
        assertNotNull(retrieved);
    }

    @Test
    void testFindByIdWithZeroIdThrowsValidationException() {
        assertThrows(ValidationException.class, () -> dao.findById(INVALID_ID_ZERO));
    }

    @Test
    void testFindByIdWithNonExistentIdReturnsNull() throws Exception {
        LinkedOrganization retrieved = dao.findById(NON_EXISTENT_ID);
        assertNull(retrieved);
    }

    @Test
    void testFindAllReturnsAtLeastOneElement() throws Exception {
        List<LinkedOrganization> all = dao.findAll();
        assertEquals(1, all.size());
    }

    @Test
    void testFindAllAfterSaveReturnsTwoElements() throws Exception {
        dao.saveLinkedOrganization(buildValidOrganization());
        List<LinkedOrganization> all = dao.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testFindAllActiveReturnsAtLeastOneElement() throws Exception {
        List<LinkedOrganization> active = dao.findAllActive();
        assertEquals(1, active.size());
    }

    @Test
    void testUpdateOrganizationReturnsTrue() throws Exception {
        LinkedOrganization organization = dao.findById(ID_ORGANIZATION);
        organization.setName(UPDATED_NAME);
        boolean result = dao.update(organization);
        assertTrue(result);
    }

    @Test
    void testUpdateOrganizationPersistsNewName() throws Exception {
        LinkedOrganization organization = dao.findById(ID_ORGANIZATION);
        organization.setName(UPDATED_NAME);
        dao.update(organization);
        LinkedOrganization retrieved = dao.findById(ID_ORGANIZATION);
        assertEquals(UPDATED_NAME, retrieved.getName());
    }

    @Test
    void testUpdateOrganizationWithZeroIdThrowsValidationException() {
        LinkedOrganization organization = buildValidOrganization();
        organization.setIdLinkedOrganization(INVALID_ID_ZERO);
        assertThrows(ValidationException.class, () -> dao.update(organization));
    }

    @Test
    void testDeactivateOrganizationReturnsTrue() throws Exception {
        insertSecondaryOrganization();
        boolean result = dao.deactivateLinkedOrganization(SECONDARY_ORG_ID);
        assertTrue(result);
    }

    @Test
    void testDeactivateOrganizationPersistsInactiveStatus() throws Exception {
        insertSecondaryOrganization();
        dao.deactivateLinkedOrganization(SECONDARY_ORG_ID);
        LinkedOrganization retrieved = dao.findById(SECONDARY_ORG_ID);
        assertEquals(ORG_STATUS_INACTIVE, retrieved.getStatus());
    }

    @Test
    void testDeactivateOrganizationWithNegativeIdThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.deactivateLinkedOrganization(INVALID_ID_NEGATIVE));
    }

    @Test
    void testHasAssociatedProjectsReturnsTrueForSupportOrganization() throws Exception {
        boolean result = dao.hasAssociatedProjects(ID_ORGANIZATION);
        assertTrue(result);
    }

    @Test
    void testHasAssociatedProjectsReturnsFalseForOrganizationWithoutProjects() throws Exception {
        insertSecondaryOrganization();
        boolean result = dao.hasAssociatedProjects(SECONDARY_ORG_ID);
        assertFalse(result);
    }

    @Test
    void testDeleteOrganizationWithAssociatedProjectsThrowsValidationException() {
        assertThrows(ValidationException.class,
                () -> dao.deleteLinkedOrganization(ID_ORGANIZATION));
    }

    @Test
    void testDeleteOrganizationWithoutProjectsReturnsTrue() throws Exception {
        insertSecondaryOrganization();
        boolean result = dao.deleteLinkedOrganization(SECONDARY_ORG_ID);
        assertTrue(result);
    }
}
