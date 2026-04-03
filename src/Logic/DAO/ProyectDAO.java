package Logic.DAO;

import Logic.DTOs.Proyect;
import Logic.Interface.IProyectDAO;

public class ProyectDAO implements IProyectDAO {

    @Override
    public boolean saveProyect(Proyect proyect) {

        return false;
    }

    @Override
    public Proyect findById(int id) {
        return null;
    }

    @Override
    public boolean deleteProyect(int id) {
        return false;
    }

    @Override
    public boolean updateProyect(Proyect proyect) {
        return false;
    }
}
