package Logic.Interface;
import Logic.DTOs.Project;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface IProjectDAO {
    boolean saveProject(Project project) throws DatabaseException;
    Project findById(int idProyecto) throws DatabaseException;
    List<Project> findAll() throws DatabaseException;
    List<Project> findAllAvailable() throws DatabaseException;
    List<Project> findByCoordinator(int idCoordinador) throws DatabaseException;
    boolean update(Project project) throws DatabaseException;
    boolean cancelProject(int idProyecto) throws DatabaseException;
    boolean decrementAvailableSlot(int idProyecto) throws DatabaseException;
}