package Logic.Interface;

import Logic.DTOs.PartialAndFinalReport;
import Logic.DTOs.Report;

import java.sql.SQLException;
import java.util.List;

public interface IReportDAO {
   int save(Report report) throws SQLException;
   Report getById (int idReport) throws SQLException;
   List<Report> getAll() throws SQLException;
   List<Report> getByStatusPending() throws SQLException;
}
