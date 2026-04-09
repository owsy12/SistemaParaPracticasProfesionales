package Logic.Interface;
import Logic.DTOs.Application;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface IApplicationDAO {
    boolean create(Application application) throws DataAccessException;
    Application findById(int applicationId) throws DataAccessException;
    Application findByIntern(int internId) throws DataAccessException;
    List<Application> findAll() throws DataAccessException;
    List<Application> findByStatus(String status) throws DataAccessException;
    boolean updateStatus(int applicationId, String status) throws DataAccessException;
}