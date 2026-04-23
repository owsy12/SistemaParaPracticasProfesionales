package Logic.Interface;

import Logic.DTOs.SelfEvaluation;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface ISelfEvaluationDAO {
    int save(SelfEvaluation selfEvaluation) throws ServiceException, ValidationException;
    SelfEvaluation getById(int idSelfEvaluation) throws ServiceException, ValidationException;
    List<SelfEvaluation> getAll() throws ServiceException;
}
