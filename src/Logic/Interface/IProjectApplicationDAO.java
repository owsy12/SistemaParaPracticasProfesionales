package Logic.Interface;

import Logic.DTOs.ProjectApplication;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IProjectApplicationDAO {
    boolean create(ProjectApplication projectApplication) throws ServiceException, ValidationException;
    ProjectApplication findById(int projectApplicationId) throws ServiceException, ValidationException;
    List<ProjectApplication> findByApplication(int applicationId) throws ServiceException, ValidationException;
    List<ProjectApplication> findAll() throws ServiceException;
    boolean delete(int projectApplicationId) throws ServiceException, ValidationException;
}
