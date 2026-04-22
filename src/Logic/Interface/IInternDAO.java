package Logic.Interface;

import Logic.DTOs.Intern;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IInternDAO {
    boolean saveIntern(Intern intern) throws DatabaseException, ValidationException;
    Intern findById(int id) throws DatabaseException, ValidationException;
    List<Intern> findAll() throws DatabaseException;
    boolean deactivateIntern(int id) throws DatabaseException, ValidationException;
    boolean updateCredits(int id, int credits) throws DatabaseException, ValidationException;
}
