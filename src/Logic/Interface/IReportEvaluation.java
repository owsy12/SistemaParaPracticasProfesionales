package Logic.Interface;

import Logic.DTOs.ReportEvaluation;
import Logic.Exceptions.DatabaseException;

import java.util.List;

public interface IReportEvaluation {
    int save(ReportEvaluation reportEvaluation) throws DatabaseException;
    ReportEvaluation getById(int idReportEvaluation) throws DatabaseException;
    ReportEvaluation getByIdReport(int idReport) throws DatabaseException;
    List<ReportEvaluation> getAll() throws DatabaseException;
}
