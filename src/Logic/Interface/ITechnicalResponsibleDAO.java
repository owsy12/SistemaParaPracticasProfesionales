package Logic.Interface;

import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface ITechnicalResponsibleDAO {
    boolean saveTechnicalResponsible(TechnicalSupervisor technicalResponsible)
            throws ServiceException, ValidationException;
    TechnicalSupervisor findById(int idTecnico) throws ServiceException, ValidationException;
    List<TechnicalSupervisor> findAll() throws ServiceException;
    List<TechnicalSupervisor> findByOrganization(int idOrganizacion)
            throws ServiceException, ValidationException;
    boolean update(TechnicalSupervisor technicalResponsible)
            throws ServiceException, ValidationException;
    boolean delete(int idTecnico) throws ServiceException, ValidationException;
}