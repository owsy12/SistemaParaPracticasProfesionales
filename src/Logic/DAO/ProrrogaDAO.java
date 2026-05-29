package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Prorroga;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProrrogaDAO {

    private static final Logger LOGGER = Logger.getLogger(ProrrogaDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO prorroga (id_actividad, fecha_fin_original, fecha_fin_nueva, motivo) " +
            "VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE_ACTIVITY =
            "UPDATE actividad SET fecha_fin = ? WHERE id_actividad = ?";

    public int save(Prorroga prorroga) throws ServiceException, ValidationException {
        if (prorroga.getIdActividad() <= 0) {
            throw new ValidationException(
                    "El ID de la actividad debe ser mayor a cero.");
        }
        if (prorroga.getMotivo() == null || prorroga.getMotivo().isBlank()) {
            throw new ValidationException("El motivo de la prórroga no puede estar vacío.");
        }
        if (prorroga.getFechaFinNueva() == null) {
            throw new ValidationException("La nueva fecha de fin es requerida.");
        }

        int generatedId = 0;

        try (Connection connection = DataBaseConnection.connectDatabase()) {
            connection.setAutoCommit(false);

            try {
                generatedId = insertProrroga(connection, prorroga);
                if (generatedId > 0) {
                    updateActivityStatus(connection, prorroga);
                    connection.commit();
                } else {
                    connection.rollback();
                    generatedId = 0;
                }
            } catch (SQLException sqlException) {
                connection.rollback();
                throw sqlException;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar prórroga para actividad {0}: {1}",
                    new Object[]{prorroga.getIdActividad(), sqlException.getMessage()});
            throw new ServiceException("Error al registrar la prórroga.", sqlException);
        }

        return generatedId;
    }

    private int insertProrroga(Connection connection, Prorroga prorroga) throws SQLException {
        int generatedId = 0;

        try (PreparedStatement statement = connection.prepareStatement(
                SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, prorroga.getIdActividad());
            statement.setDate(2, Date.valueOf(prorroga.getFechaFinOriginal()));
            statement.setDate(3, Date.valueOf(prorroga.getFechaFinNueva()));
            statement.setString(4, prorroga.getMotivo());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedId = generatedKeys.getInt(1);
                    prorroga.setIdProrroga(generatedId);
                }
            }
        }

        return generatedId;
    }

    private void updateActivityStatus(Connection connection, Prorroga prorroga) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_ACTIVITY)) {
            statement.setDate(1, Date.valueOf(prorroga.getFechaFinNueva()));
            statement.setInt(2, prorroga.getIdActividad());
            statement.executeUpdate();
        }
    }
}
