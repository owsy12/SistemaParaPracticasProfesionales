package Logic.Interface;
import Logic.DTOs.Application;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface IApplicationDAO {
    boolean create(Application application) throws DatabaseException;
    Application findById(int applicationId) throws DatabaseException;
    Application findByIntern(int internId) throws DatabaseException;
    List<Application> findAll() throws DatabaseException;
    List<Application> findByStatus(String status) throws DatabaseException;
    boolean updateStatus(int applicationId, String status) throws DatabaseException;
}