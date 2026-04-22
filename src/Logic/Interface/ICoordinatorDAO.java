package Logic.Interface;
import Logic.DTOs.Coordinator;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;
import java.util.List;

public interface ICoordinatorDAO {
    Coordinator findById(int id) throws DatabaseException, ValidationException;
    List<Coordinator> findAllCoordinators() throws DatabaseException, ValidationException;
    boolean save(Coordinator coordinator) throws DatabaseException, ValidationException;
    boolean update(Coordinator coordinator) throws DatabaseException, ValidationException;
    boolean delete(int id) throws DatabaseException, ValidationException;
    List<Coordinator> findCoordinatorsWithoutProfessorRole() throws DatabaseException;
}