package Logic.Interface;

import Logic.DTOs.Professor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IProfessorDAO {
    boolean saveProfessor(Professor professor) throws ServiceException, ValidationException;
    Professor findById(int id) throws ServiceException, ValidationException;
    List<Professor> findAll() throws ServiceException;
    boolean deactivateProfessor(int id) throws ServiceException, ValidationException;
    List<Professor> findProfessorsWithoutCoordinatorRole() throws ServiceException;
}
