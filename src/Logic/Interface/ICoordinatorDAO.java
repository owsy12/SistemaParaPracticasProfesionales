package Logic.Interface;
import Logic.DTOs.Coordinator;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import java.util.List;

public interface ICoordinatorDAO {
    Coordinator findById(int id) throws ServiceException, ValidationException;
    List<Coordinator> findAllCoordinators() throws ServiceException, ValidationException;
    boolean save(Coordinator coordinator) throws ServiceException, ValidationException;
    boolean update(Coordinator coordinator) throws ServiceException, ValidationException;
    boolean delete(int id) throws ServiceException, ValidationException;
    List<Coordinator> findCoordinatorsWithoutProfessorRole() throws ServiceException;
}