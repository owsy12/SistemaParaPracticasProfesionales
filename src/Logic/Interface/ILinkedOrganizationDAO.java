package Logic.Interface;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface ILinkedOrganizationDAO {
    boolean saveLinkedOrganization(LinkedOrganization linkedOrganization) throws DataAccessException;
    LinkedOrganization findById(int idOrganizacion) throws DataAccessException;
    List<LinkedOrganization> findAll() throws DataAccessException;
    List<LinkedOrganization> findAllActive() throws DataAccessException;
    boolean update(LinkedOrganization linkedOrganization) throws DataAccessException;
    boolean deactivateLinkedOrganization(int idOrganizacion) throws DataAccessException;
}