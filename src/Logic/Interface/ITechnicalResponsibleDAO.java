package Logic.Interface;

import Logic.DTOs.TechnicalSupervisor;
import java.util.List;

public interface ITechnicalResponsibleDAO {
    boolean saveTechnicalResponsible(TechnicalSupervisor technicalResponsible);
    TechnicalSupervisor findById(int idTecnico);
    List<TechnicalSupervisor> findByOrganization(int idOrganizacion);
    boolean update(TechnicalSupervisor technicalResponsible);
    boolean delete(int idTecnico);
}