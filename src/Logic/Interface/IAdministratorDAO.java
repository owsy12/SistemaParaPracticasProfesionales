package Logic.Interface;

import Logic.DTOs.Administrator;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IAdministratorDAO {
    boolean saveAdmin(Administrator administrator) throws ServiceException, ValidationException;
    Administrator findById(int id) throws ServiceException, ValidationException;
    List<Administrator> findAll() throws ServiceException;
}
