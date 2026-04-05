package Logic.Interface;

import java.util.List;
import java.util.Map;

public interface IUserRoleDAO {
    boolean saveUserRole(int userId, String role);
    List<String> findRolesByUserId(int userId);
    List<Map<String, Object>> findUsersByRole(String role);
    boolean deleteUserRole(int userId, String role);
}