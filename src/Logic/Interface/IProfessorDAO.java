package Logic.Interface;

import Logic.DTOs.Professor;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IProfessorDAO {
    boolean saveProfessor(Professor professor) throws DatabaseException, ValidationException;
    Professor findById(int id) throws DatabaseException, ValidationException;
    List<Professor> findAll() throws DatabaseException;
    boolean deactivateProfessor(int id) throws DatabaseException, ValidationException;
}
