package Logic.Interface;

import Logic.DTOs.SelfEvaluation;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface ISelfEvaluationDAO {
    int save(SelfEvaluation selfEvaluation) throws DatabaseException, ValidationException;
    SelfEvaluation getById(int idSelfEvaluation) throws DatabaseException, ValidationException;
    List<SelfEvaluation> getAll() throws DatabaseException;
}
