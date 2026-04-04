package Logic.Interface;

import Logic.DTOs.UserRole;
import java.util.List;

public interface IUserRoleDAO {
    boolean saveUserRole(UserRole userRole);
    List<UserRole> findRolesByUserId(int idUsuario);
    List<UserRole> findUsersByRole(String role);
    boolean deleteUserRole(int idUsuario, String role);
}