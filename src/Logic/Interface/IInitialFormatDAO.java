package Logic.Interface;

import Logic.DTOs.InitialFormat;
import Logic.Exceptions.DataAccessException;

import java.util.List;

public interface IInitialFormatDAO {
    int save(InitialFormat initialFormat) throws DataAccessException;
    InitialFormat getById(int idInitialFormat) throws DataAccessException;
    List<InitialFormat> getAll() throws DataAccessException  ;
    List<InitialFormat> getByIdIntern(int idIntern) throws DataAccessException;
}
