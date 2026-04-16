package Logic.Interface;
import Logic.DTOs.Intern;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface IInternDAO {
    boolean saveIntern(Intern intern) throws DataAccessException;
    Intern findById(int id) throws DataAccessException;
    List<Intern> findAll() throws DataAccessException;
    boolean deactivateIntern(int id) throws DataAccessException;
    boolean updateCredits(int id, int creditos) throws DataAccessException;
}