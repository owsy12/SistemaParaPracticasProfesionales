package Logic.Interface;
import Logic.DTOs.Intern;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface IInternDAO {
    boolean saveIntern(Intern intern) throws DatabaseException;
    Intern findById(int id) throws DatabaseException;
    List<Intern> findAll() throws DatabaseException;
    boolean deactivateIntern(int id) throws DatabaseException;
    boolean updateCredits(int id, int creditos) throws DatabaseException;
}