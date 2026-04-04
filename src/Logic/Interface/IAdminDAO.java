package Logic.Interface;

import Logic.DTOs.Administrator;

import java.util.List;

public interface IAdminDAO {
    Administrator findById(int id);
    List<Administrator> findAll();
    boolean save(Administrator administrator);
    boolean update(Administrator administrator);
    boolean delete(int id);
}
