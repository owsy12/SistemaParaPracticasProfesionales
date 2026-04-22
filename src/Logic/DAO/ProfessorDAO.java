package Logic.DAO;

import Logic.DTOs.Professor;
import Logic.Exceptions.DatabaseException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IProfessorDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfessorDAO implements IProfessorDAO {

    private static final Logger LOGGER = Logger.getLogger(ProfessorDAO.class.getName());
    private static final String INSERT_PROFESSOR_SQL =
            "INSERT INTO profesor (id_usuario, academica) VALUES (?, ?)";
    private static final String SELECT_PROFESSOR_BY_ID_SQL =
            "SELECT u.*, p.academica FROM usuario u " +
                    "JOIN profesor p ON u.id_usuario = p.id_usuario " +
                    "WHERE u.id_usuario = ?";
    private static final String SELECT_ALL_PROFESSORS_SQL =
            "SELECT u.*, p.academica FROM usuario u " +
                    "JOIN profesor p ON u.id_usuario = p.id_usuario";
    private static final String UPDATE_PROFESSOR_STATUS_SQL =
            "UPDATE usuario SET estado = 'Inactivo' WHERE id_usuario = ?";

    private final Connection databaseConnection;

    public ProfessorDAO(Connection databaseConnection) throws ValidationException {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public boolean saveProfessor(Professor professor) throws DatabaseException, ValidationException {
        if (professor.getId() <= 0) {
            throw new ValidationException(
                    "El ID del profesor debe ser mayor a cero. ID recibido: " + professor.getId());
        }
        boolean isSaved = false;

        try (PreparedStatement ps = databaseConnection.prepareStatement(INSERT_PROFESSOR_SQL)) {

            ps.setInt   (1, professor.getId());
            ps.setString(2, professor.getAcademicArea());

            if (ps.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar profesor con ID {0}: {1}",
                    new Object[]{professor.getId(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al guardar el profesor en la base de datos.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Professor findById(int id) throws DatabaseException, ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    "El ID del profesor debe ser mayor a cero. ID recibido: " + id);
        }
        Professor professorResult = null;

        try (PreparedStatement ps = databaseConnection.prepareStatement(SELECT_PROFESSOR_BY_ID_SQL)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    professorResult = mapProfessor(rs);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar profesor con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al buscar el profesor por ID.", sqlException);
        }

        return professorResult;
    }

    @Override
    public List<Professor> findAll() throws DatabaseException {
        List<Professor> professorList = new ArrayList<>();

        try (PreparedStatement ps = databaseConnection.prepareStatement(SELECT_ALL_PROFESSORS_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                professorList.add(mapProfessor(rs));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar la lista de profesores: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al recuperar la lista de profesores.", sqlException);
        }

        return professorList;
    }

    @Override
    public boolean deactivateProfessor(int id) throws DatabaseException, ValidationException {
        if (id <= 0) {
            throw new ValidationException(
                    "El ID del profesor debe ser mayor a cero. ID recibido: " + id);
        }
        boolean isDeactivated = false;

        try (PreparedStatement ps = databaseConnection.prepareStatement(UPDATE_PROFESSOR_STATUS_SQL)) {

            ps.setInt(1, id);

            if (ps.executeUpdate() > 0) {
                isDeactivated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al desactivar profesor con ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new DatabaseException("Error al desactivar al profesor.", sqlException);
        }

        return isDeactivated;
    }

    private Professor mapProfessor(ResultSet rs) throws SQLException {
        return new Professor(
                rs.getInt   ("id_usuario"),
                rs.getString("matricula"),
                rs.getString("nombre"),
                rs.getString("apellido_paterno"),
                rs.getString("apellido_materno"),
                rs.getString("contrasenia"),
                rs.getString("estado"),
                rs.getString("academica")
        );
    }
}
