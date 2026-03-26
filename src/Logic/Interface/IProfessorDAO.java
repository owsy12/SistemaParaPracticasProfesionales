package Logic.Interface;

import Logic.DTOs.Professor;

import java.util.List;

public interface IProfessorDAO {
    Professor findById(int id);
    List<Professor> findAll();
    boolean save(Professor professor);
    boolean update(Professor professor);
    boolean delete(int id);
}
