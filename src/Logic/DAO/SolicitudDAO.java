package Logic.DAO;

import Logic.DTOs.Solicitud;
import Logic.DTOs.SolicitudProject;
import Logic.Interface.ISolicitudDAO;

import java.sql.*;
import java.util.*;

public class SolicitudDAO implements ISolicitudDAO {

    private Connection connection;

    public SolicitudDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean save(Solicitud solicitud) {

        String sqlSolicitud = "INSERT INTO solicitud (id_practicante, estado) VALUES (?, 'Pendiente')";
        String sqlOption = "INSERT INTO solicitud_proyecto (id_solicitud, id_proyecto, orden_preferencia) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            int idSolicitud;

            try (PreparedStatement ps = connection.prepareStatement(sqlSolicitud, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, solicitud.getIdIntern());
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) {
                    throw new SQLException("No se generó ID para la solicitud.");
                }
                idSolicitud = keys.getInt(1);
            }

            try (PreparedStatement ps = connection.prepareStatement(sqlOption)) {

                for (SolicitudProject option : solicitud.getProjectOptions()) {
                    ps.setInt(1, idSolicitud);
                    ps.setInt(2, option.getIdProject());
                    ps.setInt(3, option.getPreferenceOrder());
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            connection.commit();
            solicitud.setIdSolicitud(idSolicitud);
            return true;

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { throw new RuntimeException(ex); }
            throw new RuntimeException(e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { throw new RuntimeException(e); }
        }
    }

    @Override
    public Solicitud findById(int id) {

        String sql = "SELECT id_solicitud, id_practicante, estado, fecha_solicitud FROM solicitud WHERE id_solicitud = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Solicitud solicitud = mapSolicitud(rs);
                solicitud.setProjectOptions(findOptions(solicitud.getIdSolicitud()));
                return solicitud;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public List<Solicitud> findPending() {

        List<Solicitud> list = new ArrayList<>();

        String sql = "SELECT id_solicitud, id_practicante, estado, fecha_solicitud FROM solicitud WHERE estado = 'Pendiente' ORDER BY fecha_solicitud";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Solicitud solicitud = mapSolicitud(rs);
                solicitud.setProjectOptions(findOptions(solicitud.getIdSolicitud()));
                list.add(solicitud);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public boolean updateState(int idSolicitud, String newState) {

        String sql = "UPDATE solicitud SET estado = ? WHERE id_solicitud = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newState);
            ps.setInt(2, idSolicitud);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<SolicitudProject> findOptions(int idSolicitud) throws SQLException {

        List<SolicitudProject> options = new ArrayList<>();

        String sql = "SELECT id_solicitud, id_proyecto, orden_preferencia FROM solicitud_proyecto WHERE id_solicitud = ? ORDER BY orden_preferencia";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idSolicitud);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                SolicitudProject option = new SolicitudProject();
                option.setIdSolicitud(rs.getInt("id_solicitud"));
                option.setIdProject(rs.getInt("id_proyecto"));
                option.setPreferenceOrder(rs.getInt("orden_preferencia"));
                options.add(option);
            }
        }

        return options;
    }

    private Solicitud mapSolicitud(ResultSet rs) throws SQLException {
        Solicitud solicitud = new Solicitud();

        solicitud.setIdSolicitud(rs.getInt("id_solicitud"));
        solicitud.setIdIntern(rs.getInt("id_practicante"));
        solicitud.setState(rs.getString("estado"));

        Timestamp requestDate = rs.getTimestamp("fecha_solicitud");
        if (requestDate != null) {
            solicitud.setRequestDate(requestDate.toLocalDateTime());
        }

        return solicitud;
    }
}