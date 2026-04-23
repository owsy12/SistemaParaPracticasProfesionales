package Logic.Interface;

import Logic.DTOs.Intern;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IInternDAO {
    boolean saveIntern(Intern intern) throws ServiceException, ValidationException;
    Intern findById(int id) throws ServiceException, ValidationException;
    List<Intern> findAll() throws ServiceException;
    boolean deactivateIntern(int id) throws ServiceException, ValidationException;
    boolean updateCredits(int id, int credits) throws ServiceException, ValidationException;
}
