package Logic.Interface;

import Logic.DTOs.Coordinator;

import java.util.List;

public interface ICoordinatorDAO {
    Coordinator findById(int id);
    List<Coordinator> findAllCoordinators();
    boolean save(Coordinator coordinator);
    boolean update(Coordinator coordinator);
    boolean delete(int id);

}
