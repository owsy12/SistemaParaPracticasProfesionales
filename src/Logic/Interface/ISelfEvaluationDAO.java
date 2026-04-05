package Logic.Interface;

import Logic.DTOs.SelfEvaluation;

import java.sql.SQLException;
import java.util.List;

public interface ISelfEvaluationDAO {
    int save(SelfEvaluation selfEvaluation) throws SQLException;
    SelfEvaluation getById(int idSelfEvaluation) throws SQLException;
    List<SelfEvaluation> getAll() throws SQLException;
}
