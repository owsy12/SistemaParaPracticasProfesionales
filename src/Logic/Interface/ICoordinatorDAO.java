package Logic.Interface;
import Logic.DTOs.Coordinator;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface ICoordinatorDAO {
    Coordinator findById(int id) throws DatabaseException;
    List<Coordinator> findAllCoordinators() throws DatabaseException;
    boolean save(Coordinator coordinator) throws DatabaseException;
    boolean update(Coordinator coordinator) throws DatabaseException;
    boolean delete(int id) throws DatabaseException;
}