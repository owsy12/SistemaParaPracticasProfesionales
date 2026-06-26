package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Professor;
import Logic.Exceptions.ServiceException;
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

    private static final String ROLE_PROFESSOR = "Profesor";
    private static final String STATUS_ACTIVE = "Activo";
    private static final String STATUS_INACTIVE = "Inactivo";

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

    public ProfessorDAO() throws ValidationException, ServiceException {
    }

    @Override
    public boolean saveProfessor(Professor professor) throws ServiceException, ValidationException {
        boolean isSaved = false;
        UserDAO userDAO = new UserDAO();
        int userId = userDAO.saveUser(professor);

        if (userId > 0) {
            try (Connection databaseConnection = DataBaseConnection.connectDatabase();
                 PreparedStatement preparedStatement =
                         databaseConnection.prepareStatement(INSERT_PROFESSOR_SQL)) {
                preparedStatement.setInt(1, userId);
                preparedStatement.setString(2, professor.getAcademicArea());

                if (preparedStatement.executeUpdate() > 0) {
                    isSaved = true;
                }

            } catch (SQLException sqlException) {
                LOGGER.log(Level.SEVERE, "Error registering professor: {0}",
                        sqlException.getMessage());
                throw new ServiceException("Error al registrar profesor.", sqlException);
            }
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

        try (Connection databaseConnection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     databaseConnection.prepareStatement(SELECT_PROFESSOR_BY_ID_SQL)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    professorResult = mapProfessor(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding professor with ID {0}: {1}",
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

        try (Connection databaseConnection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     databaseConnection.prepareStatement(SELECT_ALL_PROFESSORS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                professorList.add(mapProfessor(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving professor list: {0}",
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

        try (Connection databaseConnection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement =
                     databaseConnection.prepareStatement(UPDATE_PROFESSOR_STATUS_SQL)) {

            preparedStatement.setInt(1, id);

            if (preparedStatement.executeUpdate() > 0) {
                isDeactivated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deactivating professor with ID {0}: {1}",
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
        String sql = "SELECT u.*, p.academica " +
                "FROM usuario u " +
                "JOIN profesor p ON u.id_usuario = p.id_usuario " +
                "JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                "WHERE ur.rol = '" + ROLE_PROFESSOR + "' " +
                "AND ur.estado = '" + STATUS_ACTIVE + "' " +
                "AND NOT EXISTS ( " +
                "    SELECT 1" +
                "    FROM usuario_rol ur2" +
                "    WHERE ur2.id_usuario = u.id_usuario " +
                "    AND ur2.rol <> '" + ROLE_PROFESSOR + "'" +
                "    AND ur2.estado = '" + STATUS_ACTIVE + "')";

        try (Connection databaseConnection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                professorList.add(mapProfessor(resultSet));
            }
        } catch (SQLException sqlException) {
            throw new ServiceException("Error al recuperar profesores sin rol de coordinador.", sqlException);
        }

        return professorList;
    }

    @Override
    public List<Professor> findActiveProfessors() throws ServiceException {
        List<Professor> professorList = new ArrayList<>();
        String sql = "SELECT u.*, p.academica " +
                "FROM usuario u " +
                "JOIN profesor p ON u.id_usuario = p.id_usuario " +
                "JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                "WHERE ur.estado = '" + STATUS_ACTIVE + "' " +
                "AND ur.rol = '" + ROLE_PROFESSOR + "'";

        try (Connection databaseConnection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                professorList.add(mapProfessor(resultSet));
            }

        } catch (SQLException sqlException) {
            throw new ServiceException("Error al recuperar profesores activos.", sqlException);
        }

        return professorList;
    }

    private Professor mapProfessor(ResultSet resultSet) throws SQLException {

        Professor professor = new Professor();
        professor.setIdUser(resultSet.getInt("id_usuario"));
        professor.setRegistrationNumber(resultSet.getString("matricula"));
        professor.setFirstName(resultSet.getString("nombre"));
        professor.setLastName(resultSet.getString("apellido_paterno"));
        professor.setSecondLastName(resultSet.getString("apellido_materno"));
        professor.setPassword(resultSet.getString("contrasenia"));
        professor.setEmail(resultSet.getString("correo"));
        professor.setAcademicArea(resultSet.getString("academica"));

        return professor;
    }
}
