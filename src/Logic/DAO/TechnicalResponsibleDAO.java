package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.TechnicalSupervisor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.ITechnicalResponsibleDAO;

import java.sql.*;
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
    public boolean saveTechnicalResponsible(TechnicalSupervisor technicalResponsible)
            throws ServiceException, ValidationException {
        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(INSERT_TECHNICAL_SUPERVISOR_SQL)) {

            ps.setInt   (1, technicalResponsible.getIdOrganization());
            ps.setString(2, technicalResponsible.getName());
            ps.setString(3, technicalResponsible.getLastName());
            ps.setString(4, technicalResponsible.getSecondLastName());
            ps.setString(5, technicalResponsible.geteMail());
            ps.setString(6, technicalResponsible.getPosition());

            if (ps.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar responsable técnico '{0} {1}': {2}",
                    new Object[]{technicalResponsible.getName(),
                            technicalResponsible.getLastName(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar el responsable técnico.", sqlException);
        }

        return isSaved;
    }

    @Override
    public TechnicalSupervisor findById(int idTecnico) throws ServiceException, ValidationException {
        if (idTecnico <= 0) {
            throw new ValidationException(
                    "El ID del responsable técnico debe ser mayor a cero. ID recibido: " + idTecnico);
        }
        TechnicalSupervisor technicalResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_TECHNICAL_SUPERVISOR_BY_ID_SQL)) {

            ps.setInt(1, idTecnico);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    technicalResult = mapTechnicalSupervisor(rs);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar responsable técnico con ID {0}: {1}",
                    new Object[]{idTecnico, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar el responsable técnico por ID.", sqlException);
        }

        return technicalResult;
    }

    @Override
    public List<TechnicalSupervisor> findByOrganization(int idOrganizacion)
            throws ServiceException, ValidationException {
        if (idOrganizacion <= 0) {
            throw new ValidationException(
                    "El ID de la organización debe ser mayor a cero. ID recibido: " + idOrganizacion);
        }
        List<TechnicalSupervisor> technicalList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(
                     SELECT_TECHNICAL_SUPERVISORS_BY_ORGANIZATION_SQL)) {

            ps.setInt(1, idOrganizacion);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    technicalList.add(mapTechnicalSupervisor(rs));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar responsables técnicos de organización {0}: {1}",
                    new Object[]{idOrganizacion, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException(
                    "Error al buscar los responsables técnicos de la organización.", sqlException);
        }

        return technicalList;
    }

    @Override
    public boolean update(TechnicalSupervisor technicalResponsible)
            throws ServiceException, ValidationException {
        if (technicalResponsible.getIdTechnicalSupervisor() <= 0) {
            throw new ValidationException(
                    "El ID del responsable técnico debe ser mayor a cero. ID recibido: "
                            + technicalResponsible.getIdTechnicalSupervisor());
        }
        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(UPDATE_TECHNICAL_SUPERVISOR_SQL)) {

            ps.setString(1, technicalResponsible.getName());
            ps.setString(2, technicalResponsible.getLastName());
            ps.setString(3, technicalResponsible.getSecondLastName());
            ps.setString(4, technicalResponsible.geteMail());
            ps.setString(5, technicalResponsible.getPosition());
            ps.setInt   (6, technicalResponsible.getIdTechnicalSupervisor());

            if (ps.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar responsable técnico con ID {0}: {1}",
                    new Object[]{technicalResponsible.getIdTechnicalSupervisor(),
                            sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al actualizar el responsable técnico.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean delete(int idTecnico) throws ServiceException, ValidationException {
        if (idTecnico <= 0) {
            throw new ValidationException(
                    "El ID del responsable técnico debe ser mayor a cero. ID recibido: " + idTecnico);
        }
        boolean isDeleted = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(DELETE_TECHNICAL_SUPERVISOR_SQL)) {

            ps.setInt(1, idTecnico);

            if (ps.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar responsable técnico con ID {0}: {1}",
                    new Object[]{idTecnico, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al eliminar el responsable técnico.", sqlException);
        }

        return isDeleted;
    }

    private TechnicalSupervisor mapTechnicalSupervisor(ResultSet rs) throws SQLException {
        return new TechnicalSupervisor(
                rs.getInt   ("id_tecnico"),
                rs.getInt   ("id_organizacion"),
                rs.getString("nombre"),
                rs.getString("apellido_paterno"),
                rs.getString("apellido_materno"),
                rs.getString("correo_responsable"),
                rs.getString("cargo")
        );
    }
}
