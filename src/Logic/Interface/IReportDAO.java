package Logic.Interface;

import Logic.DTOs.Report;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.util.Date;
import java.util.List;

public interface IReportDAO {
    int save(Report report) throws ServiceException, ValidationException;
    Report getById(int idReport) throws ServiceException, ValidationException;
    List<Report> getAll() throws ServiceException;
    List<Report> getByStatusPending() throws ServiceException;
    List<Report> getByIdIntern(int idIntern) throws ServiceException, ValidationException;
    List<Report> getByIdProfessor(int idProfessor) throws ServiceException, ValidationException;
    boolean updateStatus(int idReport, String status, String professorObservations,
                         java.sql.Date reviewDate)
            throws ServiceException, ValidationException;
    boolean updateSignedDocumentPath(int idReport, String signedPath)
            throws ServiceException, ValidationException;
    int getTotalApprovedHoursByIntern(int idIntern)
            throws ServiceException, ValidationException;
    boolean existsMonthlyByInternAndPeriod(int idIntern, String month, int year)
            throws ServiceException;
    boolean existsPartialByInternAndProject(int idIntern, int idProject)
            throws ServiceException;
    boolean existsFinalByInternAndProject(int idIntern, int idProject)
            throws ServiceException;
    boolean markLateDelivery(int idReport) throws ServiceException, ValidationException;
}
