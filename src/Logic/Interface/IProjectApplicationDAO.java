package Logic.Interface;
import Logic.DTOs.ProjectApplication;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface IProjectApplicationDAO {
    boolean create(ProjectApplication projectApplication) throws DataAccessException;
    ProjectApplication findById(int projectApplicationId) throws DataAccessException;
    List<ProjectApplication> findByApplication(int applicationId) throws DataAccessException;
    List<ProjectApplication> findAll() throws DataAccessException;
    boolean delete(int projectApplicationId) throws DataAccessException;
}