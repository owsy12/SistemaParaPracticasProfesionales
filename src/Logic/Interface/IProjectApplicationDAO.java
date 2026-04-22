package Logic.Interface;

import Logic.DTOs.ProjectApplication;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IProjectApplicationDAO {
    boolean create(ProjectApplication projectApplication) throws DatabaseException, ValidationException;
    ProjectApplication findById(int projectApplicationId) throws DatabaseException, ValidationException;
    List<ProjectApplication> findByApplication(int applicationId) throws DatabaseException, ValidationException;
    List<ProjectApplication> findAll() throws DatabaseException;
    boolean delete(int projectApplicationId) throws DatabaseException, ValidationException;
}
