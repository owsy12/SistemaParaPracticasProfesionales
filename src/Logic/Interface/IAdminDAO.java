package Logic.Interface;

import Logic.DTOs.Admin;
import java.util.List;

public interface IAdminDAO {
    boolean saveAdmin(Admin admin);
    Admin   findById(int id);
    List<Admin> findAll();
}