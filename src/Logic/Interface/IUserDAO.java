package Logic.Interface;
import Logic.DTOs.User;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;
import java.util.List;

public interface IUserDAO {
    int saveUser(User user) throws DatabaseException, ValidationException;
    User findById(int id) throws DatabaseException, ValidationException;
    List<User> findAll() throws DatabaseException, ValidationException;
    boolean update(User user) throws DatabaseException, ValidationException;
    boolean delete(int id) throws DatabaseException, ValidationException;
    User findByMatricula(String matricula) throws DatabaseException, ValidationException;
}