package Logic.Interface;

import Logic.DTOs.Administrator;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IAdministratorDAO {
    boolean saveAdmin(Administrator administrator) throws DatabaseException, ValidationException;
    Administrator findById(int id) throws DatabaseException, ValidationException;
    List<Administrator> findAll() throws DatabaseException;
}
