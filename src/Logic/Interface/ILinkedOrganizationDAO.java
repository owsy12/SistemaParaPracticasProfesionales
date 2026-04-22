package Logic.Interface;

import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface ILinkedOrganizationDAO {
    boolean saveLinkedOrganization(LinkedOrganization linkedOrganization)
            throws DatabaseException, ValidationException;
    LinkedOrganization findById(int idOrganizacion) throws DatabaseException, ValidationException;
    List<LinkedOrganization> findAll() throws DatabaseException;
    List<LinkedOrganization> findAllActive() throws DatabaseException;
    boolean update(LinkedOrganization linkedOrganization) throws DatabaseException, ValidationException;
    boolean deactivateLinkedOrganization(int idOrganizacion) throws DatabaseException, ValidationException;
}
