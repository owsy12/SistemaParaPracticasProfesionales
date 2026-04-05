package Logic.Interface;

import Logic.DTOs.Administrator;
import java.util.List;

public interface IAdministratorDAO {
    boolean saveAdmin(Administrator administrator);
    Administrator findById(int id);
    List<Administrator> findAll();
}