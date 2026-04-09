package Logic.Interface;

import Logic.Exceptions.DataAccessException;
import java.util.List;
import java.util.Map;

public interface IUserRoleDAO {
    boolean saveUserRole(int userId, String role) throws DataAccessException;
    List<String> findRolesByUserId(int userId) throws DataAccessException;
    List<Map<String, Object>> findUsersByRole(String role) throws DataAccessException;
    boolean deleteUserRole(int userId, String role) throws DataAccessException;
}