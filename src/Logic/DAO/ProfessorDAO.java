package Logic.DAO;

import Logic.DTOs.Professor;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IProfessorDAO;
import static Logic.Utils.Connection.createdConnection;
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

    private Connection databaseConnection;

    public ProfessorDAO() throws ValidationException, ServiceException {
     databaseConnection =  createdConnection();
    }


    @Override
    public boolean saveProfessor(Professor professor) throws ServiceException, ValidationException {
        boolean isSaved = false;

        try {
            databaseConnection.setAutoCommit(false);
            UserDAO userDAO = new UserDAO();
            int userId = userDAO.saveUser(professor);
            if (userId > 0) {
                try (PreparedStatement ps = databaseConnection.prepareStatement(INSERT_PROFESSOR_SQL)) {
                    ps.setInt(1, userId);
                    ps.setString(2, professor.getAcademicArea());
                    if (ps.executeUpdate() > 0) {
                        databaseConnection.commit();
                        isSaved = true;
                    } else {
                        databaseConnection.rollback();
                    }
                }
            } else {
                databaseConnection.rollback();
            }
        } catch (SQLException sqlException) {
            try { databaseConnection.rollback(); } catch (SQLException rollbackEx) { /* Ignore */ }
            throw new ServiceException("Error al registrar profesor.", sqlException);
        } finally {
            try { databaseConnection.setAutoCommit(true); } catch (SQLException ex) { /* Ignore */ }
        }
        return isSaved;
    }

    @Override
    public Professor findById(int id) throws ServiceException, ValidationException {
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
            throw new ServiceException("Error al buscar el profesor por ID.", sqlException);
        }

        return professorResult;
    }

    @Override
    public List<Professor> findAll() throws ServiceException {
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
            throw new ServiceException("Error al recuperar la lista de profesores.", sqlException);
        }

        return professorList;
    }

    @Override
    public boolean deactivateProfessor(int id) throws ServiceException, ValidationException {
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
            throw new ServiceException("Error al desactivar al profesor.", sqlException);
        }

        return isDeactivated;
    }

    @Override
    public List<Professor> findProfessorsWithoutCoordinatorRole() throws ServiceException {
        List<Professor> professorList = new ArrayList<>();
        String sql = "SELECT u.*, p.academica FROM usuario u " +
                     "JOIN profesor p ON u.id_usuario = p.id_usuario " +
                     "LEFT JOIN coordinador c ON u.id_usuario = c.id_usuario " +
                     "WHERE c.id_usuario IS NULL AND u.estado = 'Activo'";
        try (PreparedStatement ps = databaseConnection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                professorList.add(mapProfessor(rs));
            }
        } catch (SQLException sqlException) {
            throw new ServiceException("Error al recuperar profesores sin rol de coordinador.", sqlException);
        }
        return professorList;
    }

    private Professor mapProfessor(ResultSet rs) throws SQLException {

        Professor professor = new Professor(rs.getInt   ("id_usuario"),
                rs.getString("matricula"),
                rs.getString("nombre"),
                rs.getString("apellido_paterno"),
                rs.getString("apellido_materno"),
                rs.getString("contrasenia"),
                rs.getString("estado"),
                rs.getString("academica"));

        return professor;
    }
}
