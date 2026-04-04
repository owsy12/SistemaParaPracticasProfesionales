package Logic.Interface;

import Logic.DTOs.Coordinator;
import java.util.List;

public interface ICoordinatorDAO {
    boolean saveCoordinator(Coordinator coordinator);
    Coordinator findById(int id);
    List<Coordinator> findAll();
    boolean deactivateCoordinator(int id);
}