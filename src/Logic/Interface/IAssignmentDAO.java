package Logic.Interface;

import Logic.DTOs.Assignment;
import java.util.List;

public interface IAssignmentDAO {
    boolean saveAssignment(Assignment assignment);
    Assignment findById(int idAsignacion);
    Assignment findByIntern(int idPracticante);
    List<Assignment> findByProject(int idProyecto);
    List<Assignment> findAll();
}