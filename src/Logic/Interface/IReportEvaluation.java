package Logic.Interface;

import Logic.DTOs.ReportEvaluation;

import java.sql.SQLException;
import java.util.List;

public interface IReportEvaluation {
    int save(ReportEvaluation reportEvaluation) throws SQLException;
    ReportEvaluation getById(int idReportEvaluation) throws SQLException;
    ReportEvaluation getByIdReport(int idReport) throws SQLException;
    List<ReportEvaluation> getAll() throws SQLException;
}
