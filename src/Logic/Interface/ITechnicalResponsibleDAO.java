package Logic.Interface;

import Logic.DTOs.TechnicalResponsible;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface ITechnicalResponsibleDAO {
    boolean saveTechnicalResponsible(TechnicalResponsible technicalResponsible)
            throws ServiceException, ValidationException;
    TechnicalResponsible findById(int idTecnico) throws ServiceException, ValidationException;
    List<TechnicalResponsible> findAll() throws ServiceException;
    List<TechnicalResponsible> findByOrganization(int idOrganizacion)
            throws ServiceException, ValidationException;
    boolean update(TechnicalResponsible technicalResponsible)
            throws ServiceException, ValidationException;
    boolean delete(int idTecnico) throws ServiceException, ValidationException;
}