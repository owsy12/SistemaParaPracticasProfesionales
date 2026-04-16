package Logic.Interface;
import Logic.DTOs.User;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface IUserDAO {
    boolean saveUser(User user) throws DataAccessException;
    User findById(int id) throws DataAccessException;
    List<User> findAll() throws DataAccessException;
    boolean update(User user) throws DataAccessException;
    boolean delete(int id) throws DataAccessException;
    User findByMatricula(String matricula) throws DataAccessException;
}