package Logic.Interface;

import Logic.DTOs.Project;
import java.util.List;

public interface IProjectDAO {
    boolean saveProject(Project project);
    Project findById(int idProyecto);
    List<Project> findAll();
    List<Project> findAllAvailable();
    List<Project> findByCoordinator(int idCoordinador);
    boolean update(Project project);
    boolean cancelProject(int idProyecto);
    
    boolean decrementAvailableSlot(int idProyecto);
}