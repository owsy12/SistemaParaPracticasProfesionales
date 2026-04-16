package Logic.Interface;
import Logic.DTOs.Assignment;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface IAssignmentDAO {
    int save(Assignment assignment) throws DataAccessException;
    Assignment getById(int idAssignment) throws DataAccessException;
    List<Assignment> getAll() throws DataAccessException;
    Assignment getByIdIntern(int idIntern) throws DataAccessException;
    List<Assignment> getByIdProject(int idProject) throws DataAccessException;
}