package Logic.Interface;

import Logic.DTOs.Report;
import Logic.Exceptions.DatabaseException;
import java.util.List;

public interface IReportDAO {
   int save(Report report) throws DatabaseException;
   Report getById (int idReport) throws DatabaseException;
   List<Report> getAll() throws DatabaseException;
   List<Report> getByStatusPending() throws DatabaseException;
}
