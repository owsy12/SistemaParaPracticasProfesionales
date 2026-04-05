package Logic.Interface;

import Logic.DTOs.ProjectApplication;
import java.util.List;

public interface IProjectApplicationDAO {
    boolean create(ProjectApplication projectApplication);
    ProjectApplication findById(int projectApplicationId);
    List<ProjectApplication> findByApplication(int applicationId);
    List<ProjectApplication> findAll();
    boolean delete(int projectApplicationId);
}