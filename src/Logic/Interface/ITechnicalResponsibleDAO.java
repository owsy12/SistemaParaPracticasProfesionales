package Logic.Interface;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface ITechnicalResponsibleDAO {
    boolean saveTechnicalResponsible(TechnicalSupervisor technicalResponsible) throws DatabaseException;
    TechnicalSupervisor findById(int idTecnico) throws DatabaseException;
    List<TechnicalSupervisor> findByOrganization(int idOrganizacion) throws DatabaseException;
    boolean update(TechnicalSupervisor technicalResponsible) throws DatabaseException;
    boolean delete(int idTecnico) throws DatabaseException;
}