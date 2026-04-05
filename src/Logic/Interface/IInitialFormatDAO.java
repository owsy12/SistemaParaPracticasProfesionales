package Logic.Interface;

import Logic.DTOs.InitialFormat;

import java.sql.SQLException;
import java.util.List;

public interface IInitialFormatDAO {
    int save(InitialFormat initialFormat) throws SQLException;
    InitialFormat getById(int idInitialFormat) throws SQLException;
    List<InitialFormat> getAll() throws SQLException;
    List<InitialFormat> getByIdIntern(int idIntern) throws SQLException;
}
