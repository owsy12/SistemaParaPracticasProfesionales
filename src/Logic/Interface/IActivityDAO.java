package Logic.Interface;

import Logic.DTOs.Activity;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IActivityDAO {
    int save(Activity activity) throws ServiceException, ValidationException;
    Activity findById(int idActivity) throws ServiceException, ValidationException;
    List<Activity> findByProject(int idProject) throws ServiceException, ValidationException;
    boolean update(Activity activity) throws ServiceException, ValidationException;
    boolean deactivate(int idActivity) throws ServiceException, ValidationException;
    boolean delete(int idActivity) throws ServiceException, ValidationException;
}
