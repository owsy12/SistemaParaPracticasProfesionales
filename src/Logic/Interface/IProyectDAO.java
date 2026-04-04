package Logic.Interface;

import Logic.DTOs.Project;

public interface IProyectDAO {
    boolean saveProyect(Project project);
    Project findById(int id);
    boolean deleteProyect(int id);
    boolean updateProyect(Project project);

}
