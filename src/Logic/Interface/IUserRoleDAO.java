package Logic.Interface;

import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;
import java.util.Map;

public interface IUserRoleDAO {
    boolean saveUserRole(int userId, String role) throws DatabaseException, ValidationException;
    List<String> findRolesByUserId(int userId) throws DatabaseException, ValidationException;
    List<Map<String, Object>> findUsersByRole(String role) throws DatabaseException, ValidationException;
    boolean deleteUserRole(int userId, String role) throws DatabaseException, ValidationException;
}
