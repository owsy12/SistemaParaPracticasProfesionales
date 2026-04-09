package Logic.Interface;
import Logic.DTOs.Administrator;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface IAdministratorDAO {
    boolean saveAdmin(Administrator administrator) throws DataAccessException;
    Administrator findById(int id) throws DataAccessException;
    List<Administrator> findAll() throws DataAccessException;
}