package Logic.Interface;
import Logic.DTOs.Administrator;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface IAdministratorDAO {
    boolean saveAdmin(Administrator administrator) throws DatabaseException;
    Administrator findById(int id) throws DatabaseException;
    List<Administrator> findAll() throws DatabaseException;
}