package Logic.Interface;

import Logic.DTOs.Assignment;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IAssignmentDAO {
    int save(Assignment assignment) throws DatabaseException, ValidationException;
    Assignment getById(int idAssignment) throws DatabaseException, ValidationException;
    List<Assignment> getAll() throws DatabaseException;
    Assignment getByIdIntern(int idIntern) throws DatabaseException, ValidationException;
    List<Assignment> getByIdProject(int idProject) throws DatabaseException, ValidationException;
}
