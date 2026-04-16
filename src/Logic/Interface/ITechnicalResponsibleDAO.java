package Logic.Interface;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface ITechnicalResponsibleDAO {
    boolean saveTechnicalResponsible(TechnicalSupervisor technicalResponsible) throws DataAccessException;
    TechnicalSupervisor findById(int idTecnico) throws DataAccessException;
    List<TechnicalSupervisor> findByOrganization(int idOrganizacion) throws DataAccessException;
    boolean update(TechnicalSupervisor technicalResponsible) throws DataAccessException;
    boolean delete(int idTecnico) throws DataAccessException;
}