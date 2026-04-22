package Logic.Interface;
import Logic.DTOs.User;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface IUserDAO {
    boolean saveUser(User user) throws DatabaseException;
    User findById(int id) throws DatabaseException;
    List<User> findAll() throws DatabaseException;
    boolean update(User user) throws DatabaseException;
    boolean delete(int id) throws DatabaseException;
    User findByMatricula(String matricula) throws DatabaseException;
}