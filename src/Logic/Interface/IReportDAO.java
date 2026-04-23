package Logic.Interface;

import Logic.DTOs.Report;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IReportDAO {
    int save(Report report) throws ServiceException, ValidationException;
    Report getById(int idReport) throws ServiceException, ValidationException;
    List<Report> getAll() throws ServiceException;
    List<Report> getByStatusPending() throws ServiceException;
}
