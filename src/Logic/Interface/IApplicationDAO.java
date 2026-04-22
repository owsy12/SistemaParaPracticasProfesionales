package Logic.Interface;
import Logic.DTOs.Application;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IApplicationDAO {
    boolean create(Application application) throws DatabaseException, ValidationException;
    Application findById(int applicationId) throws DatabaseException, ValidationException;
    Application findByIntern(int internId) throws DatabaseException, ValidationException;
    List<Application> findAll() throws DatabaseException, ValidationException;
    List<Application> findByStatus(String status) throws DatabaseException, ValidationException;
    boolean updateStatus(int applicationId, String status) throws DatabaseException, ValidationException;
}