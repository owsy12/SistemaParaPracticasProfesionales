package Logic.Interface;
import Logic.DTOs.Project;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import java.util.List;

public interface IProjectDAO {
    boolean saveProject(Project project) throws ServiceException, ValidationException;
    Project findById(int idProjector) throws ServiceException, ValidationException;
    List<Project> findAll() throws ServiceException, ValidationException;
    List<Project> findAllAvailable() throws ServiceException,ValidationException;
    List<Project> findByCoordinator(int idCoordinador) throws ServiceException,ValidationException;
    boolean update(Project project) throws ServiceException, ValidationException;
    boolean cancelProject(int idProject) throws ServiceException, ValidationException;
    boolean decrementAvailableSlot(int idProject) throws ServiceException, ValidationException;
    int deleteProject(int idProject) throws ServiceException, ValidationException;
}