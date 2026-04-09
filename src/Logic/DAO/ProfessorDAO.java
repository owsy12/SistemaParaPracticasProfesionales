package Logic.DAO;

import Logic.DTOs.Professor;
import Logic.Exceptions.DataAccessException;
import Logic.Interface.IProfessorDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public ProfessorDAO(Connection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public boolean saveProfessor(Professor professor) throws DataAccessException {
        boolean isSaved = false;

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(INSERT_PROFESSOR_SQL)) {

            preparedStatement.setInt(1, professor.getId());
            preparedStatement.setString(2, professor.getAcademicArea());

            if (preparedStatement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving professor with id {0}: {1}",
                    new Object[]{professor.getId(), sqlException.getMessage()});
            throw new DataAccessException("Error al guardar el profesor en la base de datos.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Professor findById(int id) throws DataAccessException {
        Professor professorResult = null;

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(SELECT_PROFESSOR_BY_ID_SQL)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    professorResult = mapProfessor(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding professor with id {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            throw new DataAccessException("Error al buscar el profesor por ID.", sqlException);
        }

        return professorResult;
    }

    @Override
    public List<Professor> findAll() throws DataAccessException {
        List<Professor> professorList = new ArrayList<>();

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(SELECT_ALL_PROFESSORS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                professorList.add(mapProfessor(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all professors: {0}", sqlException.getMessage());
            throw new DataAccessException("Error al recuperar la lista de profesores.", sqlException);
        }

        return professorList;
    }

    @Override
    public boolean deactivateProfessor(int id) throws DataAccessException {
        boolean isDeactivated = false;

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(UPDATE_PROFESSOR_STATUS_SQL)) {

            preparedStatement.setInt(1, id);

            if (preparedStatement.executeUpdate() > 0) {
                isDeactivated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deactivating professor with id {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            throw new DataAccessException("Error al desactivar al profesor.", sqlException);
        }

        return isDeactivated;
    }

    private Professor mapProfessor(ResultSet resultSet) throws SQLException {
        return new Professor(
                resultSet.getInt("id_usuario"),
                resultSet.getString("matricula"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido_paterno"),
                resultSet.getString("apellido_materno"),
                resultSet.getString("contrasenia"),
                resultSet.getString("estado"),
                resultSet.getString("academica")
        );
    }
}