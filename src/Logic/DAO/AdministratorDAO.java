package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Administrator;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IAdministratorDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdministratorDAO implements IAdministratorDAO {

    private static final Logger LOGGER = Logger.getLogger(AdministratorDAO.class.getName());
    private static final String INSERT_ADMINISTRATOR_SQL =
            "INSERT INTO administrador (id_usuario) VALUES (?)";
    private static final String SELECT_ADMINISTRATOR_BY_ID_SQL =
            "SELECT u.id_usuario, u.matricula, u.nombre, u.apellido_paterno, " +
                    "u.apellido_materno, u.contrasenia, u.estado FROM usuario u " +
                    "JOIN administrador a ON u.id_usuario = a.id_usuario " +
                    "WHERE u.id_usuario = ?";
    private static final String SELECT_ALL_ADMINISTRATORS_SQL =
            "SELECT u.id_usuario, u.matricula, u.nombre, u.apellido_paterno, " +
                    "u.apellido_materno, u.contrasenia, u.estado FROM usuario u " +
                    "JOIN administrador a ON u.id_usuario = a.id_usuario";

    @Override
    public boolean saveAdmin(Administrator administrator) throws DatabaseException, ValidationException {
        if (administrator.getId() <= 0) {
            throw new ValidationException(
                    "El ID del administrador debe ser mayor a cero. ID recibido: " + administrator.getId());
        }
        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(INSERT_ADMINISTRATOR_SQL)) {

            ps.setInt(1, administrator.getId());
            if (ps.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar administrador con ID {0}: {1}",
                    new Object[]{administrator.getId(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al guardar el administrador en la base de datos.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Administrator findById(int id) throws DatabaseException, ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    "El ID del administrador debe ser mayor a cero. ID recibido: " + id);
        }
        Administrator administratorResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_ADMINISTRATOR_BY_ID_SQL)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    administratorResult = mapAdministrator(rs);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar administrador con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al buscar el administrador por ID.", sqlException);
        }

        return administratorResult;
    }

    @Override
    public List<Administrator> findAll() throws DatabaseException {
        List<Administrator> administratorList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL_ADMINISTRATORS_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                administratorList.add(mapAdministrator(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la lista de administradores: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al recuperar la lista de administradores.", sqlException);
        }

        return administratorList;
    }

    private Administrator mapAdministrator(ResultSet rs) throws SQLException {
        return new Administrator(
                rs.getInt   ("id_usuario"),
                rs.getString("matricula"),
                rs.getString("nombre"),
                rs.getString("apellido_paterno"),
                rs.getString("apellido_materno"),
                rs.getString("contrasenia"),
                rs.getString("estado")
        );
    }
}
