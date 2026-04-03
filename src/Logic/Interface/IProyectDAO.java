package Logic.Interface;

import Logic.DTOs.Proyect;

public interface IProyectDAO {
    boolean saveProyect(Proyect proyect);
    Proyect findById(int id);
    boolean deleteProyect(int id);
    boolean updateProyect(Proyect proyect);

}
