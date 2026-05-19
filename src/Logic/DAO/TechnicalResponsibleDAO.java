package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
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

    private static final Logger LOGGER =
            Logger.getLogger(TechnicalResponsibleDAO.class.getName());

    private static final String INSERT_TECHNICAL_SUPERVISOR_SQL =
            "INSERT INTO tecnico_responsable " +
                    "(id_organizacion, nombre, apellido_paterno, apellido_materno, " +
                    "correo_responsable, cargo) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_TECHNICAL_SUPERVISOR_BY_ID_SQL =
            "SELECT id_tecnico, id_organizacion, nombre, apellido_paterno, " +
                    "apellido_materno, correo_responsable, cargo FROM tecnico_responsable " +
                    "WHERE id_tecnico = ?";
    private static final String SELECT_ALL_TECHNICAL_SUPERVISORS_SQL =
            "SELECT id_tecnico, id_organizacion, nombre, apellido_paterno, " +
                    "apellido_materno, correo_responsable, cargo FROM tecnico_responsable";
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

    private static final String ERROR_ORGANIZATION_HAS_PROJECTS =
            "No se puede eliminar el técnico responsable porque su organización tiene proyectos vinculados.";
    private static final String ERROR_DUPLICATE_ENTRY =
            "Ya existe un registro con esa clave en la base de datos.";
    private static final String VALIDATION_ID_TECHNICAL =
            "El ID del responsable técnico debe ser mayor a cero. ID recibido: ";

    @Override
    public boolean saveTechnicalResponsible(TechnicalSupervisor technicalResponsible)
            throws ServiceException, ValidationException {
        boolean isSaved = false;
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(INSERT_TECHNICAL_SUPERVISOR_SQL)) {
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
            LOGGER.log(Level.SEVERE, "Error al guardar responsable técnico '{0} {1}': {2}",
                    new Object[]{technicalResponsible.getName(),
                            technicalResponsible.getLastName(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException("Error al guardar el responsable técnico.", sqlException);
        }
        return isSaved;
    }

    @Override
    public TechnicalSupervisor findById(int idTecnico)
            throws ServiceException, ValidationException {
        if (idTecnico <= 0) {
            throw new ValidationException(VALIDATION_ID_TECHNICAL + idTecnico);
        }
        TechnicalSupervisor technicalResult = null;
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_TECHNICAL_SUPERVISOR_BY_ID_SQL)) {
            preparedStatement.setInt(1, idTecnico);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    technicalResult = mapTechnicalSupervisor(resultSet);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar responsable técnico con ID {0}: {1}",
                    new Object[]{idTecnico, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException(
                    "Error al buscar el responsable técnico por ID.", sqlException);
        }
        return technicalResult;
    }

    @Override
    public List<TechnicalSupervisor> findAll() throws ServiceException {
        List<TechnicalSupervisor> technicalList = new ArrayList<>();
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(SELECT_ALL_TECHNICAL_SUPERVISORS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                technicalList.add(mapTechnicalSupervisor(resultSet));
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al recuperar todos los responsables técnicos: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar todos los responsables técnicos.", sqlException);
        }
        return technicalList;
    }

    @Override
    public List<TechnicalSupervisor> findByOrganization(int idOrganizacion)
            throws ServiceException, ValidationException {
        if (idOrganizacion <= 0) {
            throw new ValidationException(
                    "El ID de la organización debe ser mayor a cero. ID recibido: "
                            + idOrganizacion);
        }
        List<TechnicalSupervisor> technicalList = new ArrayList<>();
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SELECT_TECHNICAL_SUPERVISORS_BY_ORGANIZATION_SQL)) {
            preparedStatement.setInt(1, idOrganizacion);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    technicalList.add(mapTechnicalSupervisor(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al buscar responsables técnicos de organización {0}: {1}",
                    new Object[]{idOrganizacion, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException(
                    "Error al buscar los responsables técnicos de la organización.",
                    sqlException);
        }
        return technicalList;
    }

    @Override
    public boolean update(TechnicalSupervisor technicalResponsible)
            throws ServiceException, ValidationException {
        if (technicalResponsible.getIdTechnicalSupervisor() <= 0) {
            throw new ValidationException(
                    VALIDATION_ID_TECHNICAL + technicalResponsible.getIdTechnicalSupervisor());
        }
        boolean isUpdated = false;
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(UPDATE_TECHNICAL_SUPERVISOR_SQL)) {
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
            LOGGER.log(Level.SEVERE,
                    "Error al actualizar responsable técnico con ID {0}: {1}",
                    new Object[]{technicalResponsible.getIdTechnicalSupervisor(),
                            sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException(
                    "Error al actualizar el responsable técnico.", sqlException);
        }
        return isUpdated;
    }

    @Override
    public boolean delete(int idTecnico) throws ServiceException, ValidationException {
        if (idTecnico <= 0) {
            throw new ValidationException(VALIDATION_ID_TECHNICAL + idTecnico);
        }
        boolean isDeleted = false;
        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement =
                     connection.prepareStatement(DELETE_TECHNICAL_SUPERVISOR_SQL)) {
            statement.setInt(1, idTecnico);
            if (statement.executeUpdate() > 0) {
                isDeleted = true;
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al eliminar responsable técnico con ID {0}: {1}",
                    new Object[]{idTecnico, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(ERROR_DUPLICATE_ENTRY, sqlException);
            }
            throw new ServiceException(
                    "Error al eliminar el responsable técnico.", sqlException);
        }
        return isDeleted;
    }

    public boolean deleteWithOrganizationValidation(int idTecnico)
            throws ServiceException, ValidationException {
        if (idTecnico <= 0) {
            throw new ValidationException(VALIDATION_ID_TECHNICAL + idTecnico);
        }
        boolean isDeleted = false;
        TechnicalSupervisor technicalSupervisor = findById(idTecnico);
        LinkedOrganizationDAO linkedOrganizationDAO = new LinkedOrganizationDAO();
        if (linkedOrganizationDAO.hasAssociatedProjects(
                technicalSupervisor.getIdOrganization())) {
            throw new ValidationException(ERROR_ORGANIZATION_HAS_PROJECTS + idTecnico);
        } else {
            isDeleted = delete(idTecnico);
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