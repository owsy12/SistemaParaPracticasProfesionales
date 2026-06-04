package Logic.Interface;

import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportDeliverable;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IReportActivityDAO {
    int save(ReportActivity reportActivity) throws ServiceException, ValidationException;
    List<ReportActivity> findByReport(int idReport) throws ServiceException, ValidationException;
    int saveDeliverable(ReportDeliverable rd) throws ServiceException, ValidationException;
    List<ReportDeliverable> findDeliverablesByReport(int idReport) throws ServiceException, ValidationException;
    List<Integer> findActivityIdsInMonthlyReportsByIntern(int internId)
            throws ServiceException, ValidationException;
}
