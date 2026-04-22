package Logic.Interface;

import Logic.Exceptions.DatabaseException;
import java.util.List;
import java.util.Map;

public interface IUserRoleDAO {
    boolean saveUserRole(int userId, String role) throws DatabaseException;
    List<String> findRolesByUserId(int userId) throws DatabaseException;
    List<Map<String, Object>> findUsersByRole(String role) throws DatabaseException;
    boolean deleteUserRole(int userId, String role) throws DatabaseException;
}