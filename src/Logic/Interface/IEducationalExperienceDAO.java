package Logic.Interface;

import Logic.DTOs.EducationalExperience;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IEducationalExperienceDAO {
    boolean save(EducationalExperience educationalExperience) throws ServiceException, ValidationException;
    EducationalExperience findByNrc(String nrc) throws ServiceException, ValidationException;
    List<EducationalExperience> findAll() throws ServiceException;
    boolean update(EducationalExperience educationalExperience) throws ServiceException, ValidationException;
    boolean delete(String nrc) throws ServiceException, ValidationException;
}
