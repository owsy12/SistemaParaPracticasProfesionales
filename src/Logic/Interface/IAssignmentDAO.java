package Logic.Interface;
import Logic.DTOs.Assignment;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface IAssignmentDAO {
    int save(Assignment assignment) throws DatabaseException;
    Assignment getById(int idAssignment) throws DatabaseException;
    List<Assignment> getAll() throws DatabaseException;
    Assignment getByIdIntern(int idIntern) throws DatabaseException;
    List<Assignment> getByIdProject(int idProject) throws DatabaseException;
}