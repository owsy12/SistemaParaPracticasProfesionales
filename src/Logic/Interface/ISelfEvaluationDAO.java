package Logic.Interface;

import Logic.DTOs.SelfEvaluation;
import Logic.Exceptions.DatabaseException;

import java.util.List;

public interface ISelfEvaluationDAO {
    int save(SelfEvaluation selfEvaluation) throws DatabaseException;
    SelfEvaluation getById(int idSelfEvaluation) throws DatabaseException;
    List<SelfEvaluation> getAll() throws DatabaseException;
}
