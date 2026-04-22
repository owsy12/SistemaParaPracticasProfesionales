package Logic.Interface;

import Logic.DTOs.ReportEvaluation;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IReportEvaluation {
    int save(ReportEvaluation reportEvaluation) throws DatabaseException, ValidationException;
    ReportEvaluation getById(int idReportEvaluation) throws DatabaseException, ValidationException;
    ReportEvaluation getByIdReport(int idReport) throws DatabaseException, ValidationException;
    List<ReportEvaluation> getAll() throws DatabaseException;
}
