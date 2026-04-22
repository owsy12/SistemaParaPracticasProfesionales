package Logic.Interface;

import Logic.DTOs.InitialFormat;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IInitialFormatDAO {
    int save(InitialFormat initialFormat) throws DatabaseException, ValidationException;
    InitialFormat getById(int idInitialFormat) throws DatabaseException, ValidationException;
    List<InitialFormat> getAll() throws DatabaseException;
    List<InitialFormat> getByIdIntern(int idIntern) throws DatabaseException, ValidationException;
}
