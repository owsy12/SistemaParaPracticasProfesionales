package Logic.Interface;

import Logic.DTOs.TechnicalResponsible;
import java.util.List;

public interface ITechnicalResponsibleDAO {

    boolean saveTechnicalResponsible(TechnicalResponsible technicalResponsible);
    TechnicalResponsible findById(int idTecnico);
    List<TechnicalResponsible> findByOrganization(int idOrganizacion);
    boolean update(TechnicalResponsible technicalResponsible);
    boolean delete(int idTecnico);
}