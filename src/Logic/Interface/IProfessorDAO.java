package Logic.Interface;

import Logic.DTOs.Professor;
import java.util.List;

public interface IProfessorDAO {
    boolean saveProfessor(Professor professor);
    Professor findById(int id);
    List<Professor> findAll();
    boolean deactivateProfessor(int id);
}