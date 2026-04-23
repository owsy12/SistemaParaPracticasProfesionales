package Logic.Interface;

import Logic.DTOs.Assignment;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IAssignmentDAO {
    int save(Assignment assignment) throws ServiceException, ValidationException;
    Assignment getById(int idAssignment) throws ServiceException, ValidationException;
    List<Assignment> getAll() throws ServiceException;
    Assignment getByIdIntern(int idIntern) throws ServiceException, ValidationException;
    List<Assignment> getByIdProject(int idProject) throws ServiceException, ValidationException;
}
