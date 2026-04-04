package Logic.DAO;

import Logic.DTOs.Professor;
import Logic.Interface.IProfessorDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfessorDAO implements IProfessorDAO {

    private static final Logger LOGGER = Logger.getLogger(ProfessorDAO.class.getName());

    private final Connection connection;

    public ProfessorDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveProfessor(Professor professor) {
        String sql = "INSERT INTO profesor (id_usuario, academica) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, professor.getId());
            preparedStatement.setString(2, professor.getAcademica());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar profesor con id {0}: {1}",
                    new Object[]{ professor.getId(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public Professor findById(int id) {
        String sql = "SELECT u.*, p.academica FROM usuario u " +
                "JOIN profesor p ON u.id_usuario = p.id_usuario " +
                "WHERE u.id_usuario = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapProfessor(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar profesor con id {0}: {1}",
                    new Object[]{ id, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public List<Professor> findAll() {
        List<Professor> professorList = new ArrayList<>();
        String sql = "SELECT u.*, p.academica FROM usuario u " +
                "JOIN profesor p ON u.id_usuario = p.id_usuario";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                professorList.add(mapProfessor(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los profesores: {0}",
                    sqlException.getMessage());
        }
        return professorList;
    }

    @Override
    public boolean deactivateProfessor(int id) {
        String sql = "UPDATE usuario SET estado = 'Inactivo' WHERE id_usuario = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al inactivar profesor con id {0}: {1}",
                    new Object[]{ id, sqlException.getMessage() });
            return false;
        }
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