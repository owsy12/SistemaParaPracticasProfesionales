package Logic.Interface;

import Logic.DTOs.Assignment;

import java.sql.SQLException;
import java.util.List;

public interface IAssignmentDAO {
    int save(Assignment assignment) throws SQLException;
    Assignment getById(int idAssignment) throws SQLException;
    List<Assignment> getAll() throws SQLException;
    Assignment getByIdIntern(int idIntern) throws SQLException;
    List<Assignment> getByIdProject(int idProject) throws SQLException;
}
