package Logic.Interface;
import Logic.DTOs.Professor;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface IProfessorDAO {
    boolean saveProfessor(Professor professor) throws DatabaseException;
    Professor findById(int id) throws DatabaseException;
    List<Professor> findAll() throws DatabaseException;
    boolean deactivateProfessor(int id) throws DatabaseException;
}