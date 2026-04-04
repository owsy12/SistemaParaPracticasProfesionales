package Logic.Interface;

import Logic.DTOs.Intern;
import java.util.List;

public interface IInternDAO {
    boolean saveIntern(Intern intern);
    Intern  findById(int id);
    List<Intern> findAll();
    boolean deactivateIntern(int id);
    boolean updateCredits(int id, int creditos);
}