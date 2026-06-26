package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.ReportObservation;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IReportObservationDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportObservationDAO implements IReportObservationDAO {

    private static final Logger LOGGER = Logger.getLogger(ReportObservationDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO observacion_reporte (id_reporte, id_profesor, comentario) VALUES (?, ?, ?)";

    private static final String SQL_SELECT_BY_REPORT =
            "SELECT id_observacion, id_reporte, id_profesor, comentario, fecha_observacion " +
                    "FROM observacion_reporte WHERE id_reporte = ? ORDER BY fecha_observacion ASC";

    @Override
    public boolean save(ReportObservation observation) throws ServiceException, ValidationException {
        validateObservation(observation);

        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, observation.getIdReport());
            statement.setInt(2, observation.getIdProfessor());
            statement.setString(3, observation.getComment());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        observation.setIdObservation(generatedKeys.getInt(1));
                    }
                }
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving report observation {0}: {1}",
                    new Object[]{observation.getIdReport(), sqlException.getMessage()});
            throw new ServiceException("Error al guardar la observación del reporte.", sqlException);
        }

        return isSaved;
    }

    @Override
    public List<ReportObservation> findByReport(int idReport) throws ServiceException, ValidationException {
        if (idReport <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + idReport);
        }

        List<ReportObservation> list = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_REPORT)) {

            statement.setInt(1, idReport);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving observations for report {0}: {1}",
                    new Object[]{idReport, sqlException.getMessage()});
            throw new ServiceException("Error al recuperar las observaciones del reporte.", sqlException);
        }

        return list;
    }

    private void validateObservation(ReportObservation observation) throws ValidationException {
        if (observation.getIdReport() <= 0) {
            throw new ValidationException(
                    "El ID del reporte debe ser mayor a cero. ID recibido: " + observation.getIdReport());
        }
        if (observation.getIdProfessor() <= 0) {
            throw new ValidationException(
                    "El ID del profesor debe ser mayor a cero. ID recibido: " + observation.getIdProfessor());
        }
        if (observation.getComment() == null || observation.getComment().isBlank()) {
            throw new ValidationException("El comentario de la observación no puede estar vacío.");
        }
    }

    private ReportObservation mapResultSet(ResultSet resultSet) throws SQLException {
        ReportObservation observation = new ReportObservation();
        observation.setIdObservation(resultSet.getInt("id_observacion"));
        observation.setIdReport(resultSet.getInt("id_reporte"));
        observation.setIdProfessor(resultSet.getInt("id_profesor"));
        observation.setComment(resultSet.getString("comentario"));

        Timestamp observationTimestamp = resultSet.getTimestamp("fecha_observacion");
        if (observationTimestamp != null) {
            observation.setObservationDate(observationTimestamp.toLocalDateTime());
        }

        return observation;
    }
}
