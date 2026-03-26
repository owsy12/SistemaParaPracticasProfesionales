package Logic.Interface;

import Logic.DTOs.Intern;

import java.util.List;

public interface IInternDAO {
    Intern findById(int id);
    List<Intern> findAll();
    boolean save(Intern intern);
    boolean update(Intern intern);
    boolean delete(int id);
}
