package Logic.Interface;
import Logic.DTOs.Project;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;
import java.util.List;

public interface IProjectDAO {
    boolean saveProject(Project project) throws DatabaseException, ValidationException;
    Project findById(int idProyecto) throws DatabaseException, ValidationException;
    List<Project> findAll() throws DatabaseException, ValidationException;
    List<Project> findAllAvailable() throws DatabaseException,ValidationException;
    List<Project> findByCoordinator(int idCoordinador) throws DatabaseException,ValidationException;
    boolean update(Project project) throws DatabaseException, ValidationException;
    boolean cancelProject(int idProyecto) throws DatabaseException, ValidationException;
    boolean decrementAvailableSlot(int idProyecto) throws DatabaseException, ValidationException;
}