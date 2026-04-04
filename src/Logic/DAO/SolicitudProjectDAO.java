package Logic.DAO;

import Logic.DTOs.SolicitudProject;
import Logic.Interface.ISolicitudProjectDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolicitudProjectDAO implements ISolicitudProjectDAO {

    private static final Logger LOGGER = Logger.getLogger(SolicitudProjectDAO.class.getName());

    private final Connection connection;

    public SolicitudProjectDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveSolicitudProject(SolicitudProject solicitudProject) {
        String sql = "INSERT INTO solicitud_proyecto (id_solicitud, id_proyecto, orden_preferencia) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, solicitudProject.getIdSolicitud());
            preparedStatement.setInt(2, solicitudProject.getIdProyecto());
            preparedStatement.setInt(3, solicitudProject.getOrdenPreferencia());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar opción de proyecto para solicitud {0}: {1}",
                    new Object[]{ solicitudProject.getIdSolicitud(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public List<SolicitudProject> findBySolicitud(int idSolicitud) {
        List<SolicitudProject> solicitudProjectList = new ArrayList<>();
        String sql = "SELECT * FROM solicitud_proyecto WHERE id_solicitud = ? " +
                "ORDER BY orden_preferencia ASC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idSolicitud);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    solicitudProjectList.add(mapSolicitudProject(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar proyectos de solicitud {0}: {1}",
                    new Object[]{ idSolicitud, sqlException.getMessage() });
        }
        return solicitudProjectList;
    }

    @Override
    public SolicitudProject findById(int idSolicitudProyecto) {
        String sql = "SELECT * FROM solicitud_proyecto WHERE id_solicitud_proyecto = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idSolicitudProyecto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapSolicitudProject(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar solicitud_proyecto con id {0}: {1}",
                    new Object[]{ idSolicitudProyecto, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public boolean delete(int idSolicitudProyecto) {
        String sql = "DELETE FROM solicitud_proyecto WHERE id_solicitud_proyecto = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idSolicitudProyecto);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar solicitud_proyecto con id {0}: {1}",
                    new Object[]{ idSolicitudProyecto, sqlException.getMessage() });
            return false;
        }
    }

    private SolicitudProject mapSolicitudProject(ResultSet resultSet) throws SQLException {
        return new SolicitudProject(
                resultSet.getInt("id_solicitud_proyecto"),
                resultSet.getInt("id_solicitud"),
                resultSet.getInt("id_proyecto"),
                resultSet.getInt("orden_preferencia")
        );
    }
}