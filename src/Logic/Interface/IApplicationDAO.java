package Logic.Interface;
import Logic.DTOs.Application;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IApplicationDAO {
    int create(Application application) throws ServiceException, ValidationException;
    Application findById(int applicationId) throws ServiceException, ValidationException;
    Application findByIntern(int internId) throws ServiceException, ValidationException;
    List<Application> findAll() throws ServiceException, ValidationException;
    List<Application> findByStatus(String status) throws ServiceException, ValidationException;
    boolean updateStatus(int applicationId, String status) throws ServiceException, ValidationException;
    Application findActiveApplicationByIntern(int internId) throws ServiceException;
    boolean cancelAcceptedByIntern(int internId) throws ServiceException, ValidationException;
}