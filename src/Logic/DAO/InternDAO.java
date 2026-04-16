package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Intern;
import Logic.Exceptions.DataAccessException;
import Logic.Interface.IInternDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public boolean saveIntern(Intern intern) throws DataAccessException {
        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_INTERN_SQL)) {

            preparedStatement.setInt(1, intern.getId());
            preparedStatement.setInt(2, intern.getCredits());

            if (preparedStatement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving intern with ID {0}: {1}",
                    new Object[]{intern.getId(), sqlException.getMessage()});
            throw new DataAccessException("Error al guardar el practicante en la base de datos.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Intern findById(int id) throws DataAccessException {
        Intern internResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_INTERN_BY_ID_SQL)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    internResult = mapIntern(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding intern with ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            throw new DataAccessException("Error al buscar el practicante por ID.", sqlException);
        }

        return internResult;
    }

    @Override
    public List<Intern> findAll() throws DataAccessException {
        List<Intern> internList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_INTERNS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                internList.add(mapIntern(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all interns: {0}", sqlException.getMessage());
            throw new DataAccessException("Error al recuperar la lista de practicantes.", sqlException);
        }

        return internList;
    }

    @Override
    public boolean deactivateIntern(int id) throws DataAccessException {
        boolean isDeactivated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_INTERN_STATUS_SQL)) {

            preparedStatement.setInt(1, id);

            if (preparedStatement.executeUpdate() > 0) {
                isDeactivated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error deactivating intern with ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            throw new DataAccessException("Error al desactivar el practicante.", sqlException);
        }

        return isDeactivated;
    }

    @Override
    public boolean updateCredits(int id, int credits) throws DataAccessException {
        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_INTERN_CREDITS_SQL)) {

            preparedStatement.setInt(1, credits);
            preparedStatement.setInt(2, id);

            if (preparedStatement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error updating credits for intern with ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            throw new DataAccessException("Error al actualizar los créditos del practicante.", sqlException);
        }

        return isUpdated;
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