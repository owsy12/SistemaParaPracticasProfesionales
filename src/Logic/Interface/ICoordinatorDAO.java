package Logic.Interface;
import Logic.DTOs.Coordinator;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface ICoordinatorDAO {
    Coordinator findById(int id) throws DataAccessException;
    List<Coordinator> findAllCoordinators() throws DataAccessException;
    boolean save(Coordinator coordinator) throws DataAccessException;
    boolean update(Coordinator coordinator) throws DataAccessException;
    boolean delete(int id) throws DataAccessException;
}