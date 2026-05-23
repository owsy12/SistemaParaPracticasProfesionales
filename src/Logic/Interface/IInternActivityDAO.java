package Logic.Interface;

import Logic.DTOs.InternActivity;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IInternActivityDAO {
    int save(InternActivity internActivity) throws ServiceException, ValidationException;
    InternActivity findByActivityAndIntern(int idActivity, int idIntern)
            throws ServiceException, ValidationException;
    List<InternActivity> findByInternAndProject(int idIntern, int idProject)
            throws ServiceException, ValidationException;
    int getTotalHoursByIntern(int idIntern) throws ServiceException, ValidationException;
    boolean update(InternActivity internActivity) throws ServiceException, ValidationException;
}
