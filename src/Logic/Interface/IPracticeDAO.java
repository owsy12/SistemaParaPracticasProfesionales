package Logic.Interface;

import Logic.DTOs.Practice;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IPracticeDAO {
    boolean save(Practice practice) throws ServiceException, ValidationException;
    Practice findById(int idPractice) throws ServiceException, ValidationException;
    List<Practice> findByNrc(String nrc) throws ServiceException, ValidationException;
    List<Practice> findByIntern(int idIntern) throws ServiceException, ValidationException;
    boolean update(Practice practice) throws ServiceException, ValidationException;
    boolean delete(int idPractice) throws ServiceException, ValidationException;
}
