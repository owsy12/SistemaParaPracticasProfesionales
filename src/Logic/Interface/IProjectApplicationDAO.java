package Logic.Interface;
import Logic.DTOs.ProjectApplication;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface IProjectApplicationDAO {
    boolean create(ProjectApplication projectApplication) throws DatabaseException;
    ProjectApplication findById(int projectApplicationId) throws DatabaseException;
    List<ProjectApplication> findByApplication(int applicationId) throws DatabaseException;
    List<ProjectApplication> findAll() throws DatabaseException;
    boolean delete(int projectApplicationId) throws DatabaseException;
}