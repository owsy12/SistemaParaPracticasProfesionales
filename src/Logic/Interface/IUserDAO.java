package Logic.Interface;
import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import java.util.List;

public interface IUserDAO {
    int saveUser(User user) throws ServiceException, ValidationException;
    User findById(int id) throws ServiceException, ValidationException;
    List<User> findAll() throws ServiceException, ValidationException;
    boolean update(User user) throws ServiceException, ValidationException;
    boolean delete(int id) throws ServiceException, ValidationException;
    User findByIdentifier(String matricula) throws ServiceException, ValidationException;
    User findByEmail(String email) throws ServiceException, ValidationException;

}