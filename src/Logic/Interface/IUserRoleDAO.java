package Logic.Interface;

import Logic.DTOs.User;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;
import java.util.Map;

public interface IUserRoleDAO {
    boolean saveUserRole(User user) throws ServiceException, ValidationException;
    List<String> findRolesByUserId(int userId) throws ServiceException, ValidationException;
    List<Map<String, Object>> findUsersByRole(String role) throws ServiceException, ValidationException;
    boolean deleteUserRole(int userId, String role) throws ServiceException, ValidationException;
    boolean updateUserRolStatus(User user) throws ServiceException, ValidationException;
    List<String> getActiveRolsByUserId(int userId) throws ServiceException, ValidationException;
}
