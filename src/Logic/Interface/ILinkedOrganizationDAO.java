package Logic.Interface;
import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface ILinkedOrganizationDAO {
    boolean saveLinkedOrganization(LinkedOrganization linkedOrganization) throws DatabaseException;
    LinkedOrganization findById(int idOrganizacion) throws DatabaseException;
    List<LinkedOrganization> findAll() throws DatabaseException;
    List<LinkedOrganization> findAllActive() throws DatabaseException;
    boolean update(LinkedOrganization linkedOrganization) throws DatabaseException;
    boolean deactivateLinkedOrganization(int idOrganizacion) throws DatabaseException;
}