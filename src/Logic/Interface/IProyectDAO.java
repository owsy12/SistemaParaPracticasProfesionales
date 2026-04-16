package Logic.Interface;

import Logic.DTOs.Project;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface IProjectDAO {
    boolean saveProject(Project project) throws DataAccessException;
    Project findById(int idProyecto) throws DataAccessException;
    List<Project> findAll() throws DataAccessException;
    List<Project> findAllAvailable() throws DataAccessException;
    List<Project> findByCoordinator(int idCoordinador) throws DataAccessException;
    boolean update(Project project) throws DataAccessException;
    boolean cancelProject(int idProyecto) throws DataAccessException;
    boolean decrementAvailableSlot(int idProyecto) throws DataAccessException;
}