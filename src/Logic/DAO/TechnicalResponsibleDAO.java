package Logic.DAO;

import Logic.DTOs.TechnicalResponsible;
import Logic.Interface.ITechnicalResponsibleDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TechnicalResponsibleDAO implements ITechnicalResponsibleDAO {

    private static final Logger LOGGER = Logger.getLogger(TechnicalResponsibleDAO.class.getName());

    private final Connection connection;

    public TechnicalResponsibleDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveTechnicalResponsible(TechnicalResponsible technicalResponsible) {
        String sql = "INSERT INTO tecnico_responsable " +
                "(id_organizacion, nombre, apellido_paterno, apellido_materno, " +
                "correo_responsable, cargo) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, technicalResponsible.getIdOrganizacion());
            preparedStatement.setString(2, technicalResponsible.getNombre());
            preparedStatement.setString(3, technicalResponsible.getApellidoPaterno());
            preparedStatement.setString(4, technicalResponsible.getApellidoMaterno());
            preparedStatement.setString(5, technicalResponsible.getCorreoResponsable());
            preparedStatement.setString(6, technicalResponsible.getCargo());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar técnico responsable {0}: {1}",
                    new Object[]{ technicalResponsible.getFullName(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public TechnicalResponsible findById(int idTecnico) {
        String sql = "SELECT * FROM tecnico_responsable WHERE id_tecnico = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idTecnico);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTechnicalResponsible(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar técnico con id {0}: {1}",
                    new Object[]{ idTecnico, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public List<TechnicalResponsible> findByOrganization(int idOrganizacion) {
        List<TechnicalResponsible> technicalList = new ArrayList<>();
        String sql = "SELECT * FROM tecnico_responsable WHERE id_organizacion = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idOrganizacion);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    technicalList.add(mapTechnicalResponsible(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar técnicos de organización {0}: {1}",
                    new Object[]{ idOrganizacion, sqlException.getMessage() });
        }
        return technicalList;
    }

    @Override
    public boolean update(TechnicalResponsible technicalResponsible) {
        String sql = "UPDATE tecnico_responsable " +
                "SET nombre = ?, apellido_paterno = ?, apellido_materno = ?, " +
                "correo_responsable = ?, cargo = ? " +
                "WHERE id_tecnico = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, technicalResponsible.getNombre());
            preparedStatement.setString(2, technicalResponsible.getApellidoPaterno());
            preparedStatement.setString(3, technicalResponsible.getApellidoMaterno());
            preparedStatement.setString(4, technicalResponsible.getCorreoResponsable());
            preparedStatement.setString(5, technicalResponsible.getCargo());
            preparedStatement.setInt(6, technicalResponsible.getIdTecnico());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar técnico con id {0}: {1}",
                    new Object[]{ technicalResponsible.getIdTecnico(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public boolean delete(int idTecnico) {
        String sql = "DELETE FROM tecnico_responsable WHERE id_tecnico = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, idTecnico);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar técnico con id {0}: {1}",
                    new Object[]{ idTecnico, sqlException.getMessage() });
            return false;
        }
    }

    private TechnicalResponsible mapTechnicalResponsible(ResultSet resultSet) throws SQLException {
        return new TechnicalResponsible(
                resultSet.getInt("id_tecnico"),
                resultSet.getInt("id_organizacion"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido_paterno"),
                resultSet.getString("apellido_materno"),
                resultSet.getString("correo_responsable"),
                resultSet.getString("cargo")
        );
    }
}