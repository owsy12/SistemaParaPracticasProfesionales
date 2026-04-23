package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Intern;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IInternDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InternDAO implements IInternDAO {

    private static final Logger LOGGER = Logger.getLogger(InternDAO.class.getName());
    private static final String INSERT_INTERN_SQL =
            "INSERT INTO practicante (id_usuario, creditos) VALUES (?, ?)";
    private static final String SELECT_INTERN_BY_ID_SQL =
            "SELECT u.id_usuario, u.matricula, u.nombre, u.apellido_paterno, " +
                    "u.apellido_materno, u.contrasenia, u.estado, p.creditos FROM usuario u " +
                    "JOIN practicante p ON u.id_usuario = p.id_usuario " +
                    "WHERE u.id_usuario = ?";
    private static final String SELECT_ALL_INTERNS_SQL =
            "SELECT u.id_usuario, u.matricula, u.nombre, u.apellido_paterno, " +
                    "u.apellido_materno, u.contrasenia, u.estado, p.creditos FROM usuario u " +
                    "JOIN practicante p ON u.id_usuario = p.id_usuario";
    private static final String UPDATE_INTERN_STATUS_SQL =
            "UPDATE usuario SET estado = 'Inactivo' WHERE id_usuario = ?";
    private static final String UPDATE_INTERN_CREDITS_SQL =
            "UPDATE practicante SET creditos = ? WHERE id_usuario = ?";

    @Override
    public boolean saveIntern(Intern intern) throws ServiceException, ValidationException {
        if (intern.getId() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + intern.getId());
        }
        if (intern.getCredits() < 0) {
            throw new ValidationException(
                    "Los créditos no pueden ser negativos. Valor recibido: " + intern.getCredits());
        }
        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(INSERT_INTERN_SQL)) {

            ps.setInt(1, intern.getId());
            ps.setInt(2, intern.getCredits());

            if (ps.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar practicante con ID {0}: {1}",
                    new Object[]{intern.getId(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar el practicante en la base de datos.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Intern findById(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + id);
        }
        Intern internResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_INTERN_BY_ID_SQL)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    internResult = mapIntern(rs);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar practicante con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al buscar el practicante por ID.", sqlException);
        }

        return internResult;
    }

    @Override
    public List<Intern> findAll() throws ServiceException {
        List<Intern> internList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL_INTERNS_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                internList.add(mapIntern(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la lista de practicantes: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar la lista de practicantes.", sqlException);
        }

        return internList;
    }

    @Override
    public boolean deactivateIntern(int id) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + id);
        }
        boolean isDeactivated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(UPDATE_INTERN_STATUS_SQL)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() > 0) {
                isDeactivated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al desactivar practicante con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al desactivar el practicante.", sqlException);
        }

        return isDeactivated;
    }

    @Override
    public boolean updateCredits(int id, int credits) throws ServiceException, ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + id);
        }
        if (credits < 0) {
            throw new ValidationException(
                    "Los créditos no pueden ser negativos. Valor recibido: " + credits);
        }
        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(UPDATE_INTERN_CREDITS_SQL)) {

            ps.setInt(1, credits);
            ps.setInt(2, id);

            if (ps.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar créditos del practicante con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al actualizar los créditos del practicante.", sqlException);
        }

        return isUpdated;
    }

    private Intern mapIntern(ResultSet rs) throws SQLException {
        return new Intern(
                rs.getInt   ("id_usuario"),
                rs.getString("matricula"),
                rs.getString("nombre"),
                rs.getString("apellido_paterno"),
                rs.getString("apellido_materno"),
                rs.getString("contrasenia"),
                rs.getString("estado"),
                rs.getInt   ("creditos")
        );
    }
}
