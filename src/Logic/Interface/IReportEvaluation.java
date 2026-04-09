package Logic.Interface;

import Logic.DTOs.ReportEvaluation;
import Logic.Exceptions.DataAccessException;

import java.util.List;

public interface IReportEvaluation {
    int save(ReportEvaluation reportEvaluation) throws DataAccessException;
    ReportEvaluation getById(int idReportEvaluation) throws DataAccessException;
    ReportEvaluation getByIdReport(int idReport) throws DataAccessException;
    List<ReportEvaluation> getAll() throws DataAccessException;
}
