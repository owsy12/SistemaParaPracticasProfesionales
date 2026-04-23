package Logic.Interface;

import Logic.DTOs.InitialFormat;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IInitialFormatDAO {
    int save(InitialFormat initialFormat) throws ServiceException, ValidationException;
    InitialFormat getById(int idInitialFormat) throws ServiceException, ValidationException;
    List<InitialFormat> getAll() throws ServiceException;
    List<InitialFormat> getByIdIntern(int idIntern) throws ServiceException, ValidationException;
}
