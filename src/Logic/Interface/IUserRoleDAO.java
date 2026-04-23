package Logic.Interface;

import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;
import java.util.Map;

public interface IUserRoleDAO {
    boolean saveUserRole(int userId, String role) throws ServiceException, ValidationException;
    List<String> findRolesByUserId(int userId) throws ServiceException, ValidationException;
    List<Map<String, Object>> findUsersByRole(String role) throws ServiceException, ValidationException;
    boolean deleteUserRole(int userId, String role) throws ServiceException, ValidationException;
}
