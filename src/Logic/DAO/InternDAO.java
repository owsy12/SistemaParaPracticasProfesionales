package Logic.DAO;

import Logic.DTOs.Intern;
import Logic.Interface.IInternDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InternDAO implements IInternDAO {

    private static final Logger LOGGER = Logger.getLogger(InternDAO.class.getName());

    private final Connection connection;

    public InternDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean saveIntern(Intern intern) {
        String sql = "INSERT INTO practicante (id_usuario, creditos) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, intern.getId());
            preparedStatement.setInt(2, intern.getCreditos());
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar practicante con id {0}: {1}",
                    new Object[]{ intern.getId(), sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public Intern findById(int id) {
        String sql = "SELECT u.*, p.creditos FROM usuario u " +
                "JOIN practicante p ON u.id_usuario = p.id_usuario " +
                "WHERE u.id_usuario = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapIntern(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar practicante con id {0}: {1}",
                    new Object[]{ id, sqlException.getMessage() });
        }
        return null;
    }

    @Override
    public List<Intern> findAll() {
        List<Intern> internList = new ArrayList<>();
        String sql = "SELECT u.*, p.creditos FROM usuario u " +
                "JOIN practicante p ON u.id_usuario = p.id_usuario";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                internList.add(mapIntern(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los practicantes: {0}",
                    sqlException.getMessage());
        }
        return internList;
    }

    @Override
    public boolean deactivateIntern(int id) {
        String sql = "UPDATE usuario SET estado = 'Inactivo' WHERE id_usuario = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al inactivar practicante con id {0}: {1}",
                    new Object[]{ id, sqlException.getMessage() });
            return false;
        }
    }

    @Override
    public boolean updateCredits(int id, int creditos) {
        String sql = "UPDATE practicante SET creditos = ? WHERE id_usuario = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, creditos);
            preparedStatement.setInt(2, id);
            return preparedStatement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar créditos del practicante con id {0}: {1}",
                    new Object[]{ id, sqlException.getMessage() });
            return false;
        }
    }

    private Intern mapIntern(ResultSet resultSet) throws SQLException {
        return new Intern(
                resultSet.getInt("id_usuario"),
                resultSet.getString("matricula"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido_paterno"),
                resultSet.getString("apellido_materno"),
                resultSet.getString("contrasenia"),
                resultSet.getString("estado"),
                resultSet.getInt("creditos")
        );
    }
}