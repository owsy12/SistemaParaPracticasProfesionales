package Logic.Interface;
import Logic.DTOs.Professor;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface IProfessorDAO {
    boolean saveProfessor(Professor professor) throws DataAccessException;
    Professor findById(int id) throws DataAccessException;
    List<Professor> findAll() throws DataAccessException;
    boolean deactivateProfessor(int id) throws DataAccessException;
}