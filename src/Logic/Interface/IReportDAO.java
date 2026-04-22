package Logic.Interface;

import Logic.DTOs.Report;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.ValidationException;

import java.util.List;

public interface IReportDAO {
    int save(Report report) throws DatabaseException, ValidationException;
    Report getById(int idReport) throws DatabaseException, ValidationException;
    List<Report> getAll() throws DatabaseException;
    List<Report> getByStatusPending() throws DatabaseException;
}
