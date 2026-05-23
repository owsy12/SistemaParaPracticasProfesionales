package Logic.Interface;

import Logic.DTOs.ReportObservation;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IReportObservationDAO {
    boolean save(ReportObservation observation) throws ServiceException, ValidationException;
    List<ReportObservation> findByReport(int idReport) throws ServiceException, ValidationException;
}
