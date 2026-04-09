package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.DataAccessException;
import Logic.Interface.ITechnicalResponsibleDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TechnicalResponsibleDAO implements ITechnicalResponsibleDAO {

    private static final Logger LOGGER = Logger.getLogger(TechnicalResponsibleDAO.class.getName());

    private static final String INSERT_TECHNICAL_SUPERVISOR_SQL =
            "INSERT INTO tecnico_responsable " +
                    "(id_organizacion, nombre, apellido_paterno, apellido_materno, " +
                    "correo_responsable, cargo) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_TECHNICAL_SUPERVISOR_BY_ID_SQL =
            "SELECT id_tecnico, id_organizacion, nombre, apellido_paterno, " +
                    "apellido_materno, correo_responsable, cargo FROM tecnico_responsable " +
                    "WHERE id_tecnico = ?";
    private static final String SELECT_TECHNICAL_SUPERVISORS_BY_ORGANIZATION_SQL =
            "SELECT id_tecnico, id_organizacion, nombre, apellido_paterno, " +
                    "apellido_materno, correo_responsable, cargo FROM tecnico_responsable " +
                    "WHERE id_organizacion = ?";
    private static final String UPDATE_TECHNICAL_SUPERVISOR_SQL =
            "UPDATE tecnico_responsable " +
                    "SET nombre = ?, apellido_paterno = ?, apellido_materno = ?, " +
                    "correo_responsable = ?, cargo = ? WHERE id_tecnico = ?";
    private static final String DELETE_TECHNICAL_SUPERVISOR_SQL =
            "DELETE FROM tecnico_responsable WHERE id_tecnico = ?";

    @Override
    public boolean saveTechnicalResponsible(TechnicalSupervisor technicalResponsible) throws DataAccessException {
        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TECHNICAL_SUPERVISOR_SQL)) {

            preparedStatement.setInt(1, technicalResponsible.getIdOrganization());
            preparedStatement.setString(2, technicalResponsible.getName());
            preparedStatement.setString(3, technicalResponsible.getLastName());
            preparedStatement.setString(4, technicalResponsible.getSecondLastName());
            preparedStatement.setString(5, technicalResponsible.geteMail());
            preparedStatement.setString(6, technicalResponsible.getPosition());

            if (preparedStatement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving technical supervisor {0} {1}: {2}",
                    new Object[]{technicalResponsible.getName(), technicalResponsible.getLastName(), sqlException.getMessage()});
            throw new DataAccessException("Error al guardar el responsable técnico.", sqlException);
        }

        return isSaved;
    }

    @Override
    public TechnicalSupervisor findById(int idTecnico) throws DataAccessException {
        TechnicalSupervisor technicalResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TECHNICAL_SUPERVISOR_BY_ID_SQL)) {

            preparedStatement.setInt(1, idTecnico);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    technicalResult = mapTechnicalSupervisor(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding technical supervisor with ID {0}: {1}",
                    new Object[]{idTecnico, sqlException.getMessage()});
            throw new DataAccessException("Error al buscar el responsable técnico por ID.", sqlException);
        }

        return technicalResult;
    }

    @Override
    public List<TechnicalSupervisor> findByOrganization(int idOrganizacion) throws DataAccessException {
        List<TechnicalSupervisor> technicalList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TECHNICAL_SUPERVISORS_BY_ORGANIZATION_SQL)) {

            preparedStatement.setInt(1, idOrganizacion);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    technicalList.add(mapTechnicalSupervisor(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding technical supervisors for organization {0}: {1}",
                    new Object[]{idOrganizacion, sqlException.getMessage()});
            throw new DataAccessException("Error al buscar los responsables técnicos de la organización.", sqlException);
        }

        return technicalList;
    }

    @Override
    public boolean update(TechnicalSupervisor technicalResponsible) throws DataAccessException {
        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_TECHNICAL_SUPERVISOR_SQL)) {

            preparedStatement.setString(1, technicalResponsible.getName());
            preparedStatement.setString(2, technicalResponsible.getLastName());
            preparedStatement.setString(3, technicalResponsible.getSecondLastName());
            preparedStatement.setString(4, technicalResponsible.geteMail());
            preparedStatement.setString(5, technicalResponsible.getPosition());
            preparedStatement.setInt(6, technicalResponsible.getIdTechnicalSupervisor());

            if (preparedStatement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error updating technical supervisor with ID {0}: {1}",
                    new Object[]{technicalResponsible.getIdTechnicalSupervisor(), sqlException.getMessage()});
            throw new DataAccessException("Error al actualizar el responsable técnico.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean delete(int idTecnico) throws DataAccessException {
        boolean isDeleted = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_TECHNICAL_SUPERVISOR_SQL)) {

            preparedStatement.setInt(1, idTecnico);

            if (preparedStatement.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deleting technical supervisor with ID {0}: {1}",
                    new Object[]{idTecnico, sqlException.getMessage()});
            throw new DataAccessException("Error al eliminar el responsable técnico.", sqlException);
        }

        return isDeleted;
    }

    private TechnicalSupervisor mapTechnicalSupervisor(ResultSet resultSet) throws SQLException {
        return new TechnicalSupervisor(
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