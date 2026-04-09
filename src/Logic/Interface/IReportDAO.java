package Logic.Interface;

import Logic.DTOs.Report;
import Logic.Exceptions.DataAccessException;
import java.util.List;

public interface IReportDAO {
   int save(Report report) throws DataAccessException;
   Report getById (int idReport) throws DataAccessException;
   List<Report> getAll() throws DataAccessException;
   List<Report> getByStatusPending() throws DataAccessException;
}
