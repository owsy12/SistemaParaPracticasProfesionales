package Logic.Interface;

import Logic.DTOs.Admin;

import java.util.List;

public interface IAdminDAO {
    Admin findById(int id);
    List<Admin> findAll();
    boolean save(Admin admin);
    boolean update(Admin admin);
    boolean delete(int id);
}
