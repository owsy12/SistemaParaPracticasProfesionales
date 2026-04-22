package Logic.Interface;

import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface ITechnicalResponsibleDAO {
    boolean saveTechnicalResponsible(TechnicalSupervisor technicalResponsible)
            throws DatabaseException, ValidationException;
    TechnicalSupervisor findById(int idTecnico) throws DatabaseException, ValidationException;
    List<TechnicalSupervisor> findByOrganization(int idOrganizacion)
            throws DatabaseException, ValidationException;
    boolean update(TechnicalSupervisor technicalResponsible) throws DatabaseException, ValidationException;
    boolean delete(int idTecnico) throws DatabaseException, ValidationException;
}
