package Logic.DAO;

import Logic.DTOs.Project;
import Logic.Interface.IProyectDAO;

public class ProjectDAO implements IProyectDAO {

    @Override
    public boolean saveProyect(Project project) {

        return false;
    }

    @Override
    public Project findById(int id) {
        return null;
    }

    @Override
    public boolean deleteProyect(int id) {
        return false;
    }

    @Override
    public boolean updateProyect(Project project) {
        return false;
    }
}
