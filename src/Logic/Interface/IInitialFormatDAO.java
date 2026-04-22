package Logic.Interface;

import Logic.DTOs.InitialFormat;
import Logic.Exceptions.DatabaseException;

import java.util.List;

public interface IInitialFormatDAO {
    int save(InitialFormat initialFormat) throws DatabaseException;
    InitialFormat getById(int idInitialFormat) throws DatabaseException;
    List<InitialFormat> getAll() throws DatabaseException  ;
    List<InitialFormat> getByIdIntern(int idIntern) throws DatabaseException;
}
