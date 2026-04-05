package Logic.Interface;

import Logic.DTOs.Application;
import java.util.List;

public interface IApplicationDAO {
    boolean create(Application application);
    Application findById(int applicationId);
    Application findByIntern(int internId);
    List<Application> findAll();
    List<Application> findByStatus(String status);
    boolean updateStatus(int applicationId, String status);
}