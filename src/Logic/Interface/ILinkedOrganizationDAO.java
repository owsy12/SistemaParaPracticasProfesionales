package Logic.Interface;

import Logic.DTOs.LinkedOrganization;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface ILinkedOrganizationDAO {
    boolean saveLinkedOrganization(LinkedOrganization linkedOrganization)
            throws ServiceException, ValidationException;
    LinkedOrganization findById(int idOrganization) throws ServiceException, ValidationException;
    List<LinkedOrganization> findAll() throws ServiceException;
    List<LinkedOrganization> findAllActive() throws ServiceException;
    boolean update(LinkedOrganization linkedOrganization) throws ServiceException, ValidationException;
    boolean deactivateLinkedOrganization(int idOrganization) throws ServiceException, ValidationException;
    boolean hasAssociatedProjects(int idOrganization) throws ServiceException, ValidationException;
    boolean deleteLinkedOrganization(int idOrganization) throws ServiceException, ValidationException;
}