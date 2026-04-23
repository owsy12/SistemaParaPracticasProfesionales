package Logic.Interface;

import Logic.DTOs.ReportEvaluation;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IReportEvaluation {
    int save(ReportEvaluation reportEvaluation) throws ServiceException, ValidationException;
    ReportEvaluation getById(int idReportEvaluation) throws ServiceException, ValidationException;
    ReportEvaluation getByIdReport(int idReport) throws ServiceException, ValidationException;
    List<ReportEvaluation> getAll() throws ServiceException;
}
