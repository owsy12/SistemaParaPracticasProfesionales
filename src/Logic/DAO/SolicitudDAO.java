package Logic.DAO;

import Logic.DTOs.Solicitud;
import Logic.Interface.ISolicitudDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolicitudDAO implements ISolicitudDAO {

    private static final Logger LOGGER = Logger.getLogger(SolicitudDAO.class.getName());

    private final Connection connection;

    public SolicitudDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveSolicitud(Solicitud solicitud) {
        String sql = "INSERT INTO solicitud (id_practicante, estado) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, solicitud.getIdPracticante());
            preparedStatement.setString(2, solicitud.getEstado());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar solicitud del practicante {0}: {1}",
                    new Object[]{ solicitud.getIdPracticante(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public Solicitud findById(int idSolicitud) {
        String sql = "SELECT * FROM solicitud WHERE id_solicitud = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idSolicitud);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapSolicitud(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar solicitud con id {0}: {1}",
                    new Object[]{ idSolicitud, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public Solicitud findByIntern(int idPracticante) {
        String sql = "SELECT * FROM solicitud WHERE id_practicante = ? " +
                "ORDER BY fecha_solicitud DESC LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idPracticante);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapSolicitud(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar solicitud del practicante {0}: {1}",
                    new Object[]{ idPracticante, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public List<Solicitud> findAll() {
        List<Solicitud> solicitudList = new ArrayList<>();
        String sql = "SELECT * FROM solicitud";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                solicitudList.add(mapSolicitud(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al obtener todas las solicitudes: {0}",
                    sqlException.getMessage());
        }
        return solicitudList;
    }

    @Override
    public List<Solicitud> findByStatus(String estado) {
        List<Solicitud> solicitudList = new ArrayList<>();
        String sql = "SELECT * FROM solicitud WHERE estado = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, estado);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    solicitudList.add(mapSolicitud(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar solicitudes con estado {0}: {1}",
                    new Object[]{ estado, sqlException.getMessage() });
        }
        return solicitudList;
    }

    @Override
    public boolean updateStatus(int idSolicitud, String estado) {
        String sql = "UPDATE solicitud SET estado = ? WHERE id_solicitud = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, estado);
            preparedStatement.setInt(2, idSolicitud);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar estado de solicitud {0}: {1}",
                    new Object[]{ idSolicitud, sqlException.getMessage() });
            return false;
        }
    }

    private Solicitud mapSolicitud(ResultSet resultSet) throws SQLException {
        return new Solicitud(
                resultSet.getInt("id_solicitud"),
                resultSet.getInt("id_practicante"),
                resultSet.getString("estado"),
                resultSet.getTimestamp("fecha_solicitud").toLocalDateTime()
        );
    }
}