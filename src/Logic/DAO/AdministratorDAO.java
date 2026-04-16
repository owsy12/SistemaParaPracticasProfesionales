package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Administrator;
import Logic.Exceptions.DataAccessException;
import Logic.Interface.IAdministratorDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public boolean saveAdmin(Administrator administrator) throws DataAccessException {
        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_ADMINISTRATOR_SQL)) {

            preparedStatement.setInt(1, administrator.getId());
            if (preparedStatement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error saving administrator with ID {0}: {1}",
                    new Object[]{administrator.getId(), sqlException.getMessage()});
            throw new DataAccessException("Error al guardar el administrador en la base de datos.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Administrator findById(int id) throws DataAccessException {
        Administrator administratorResult = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ADMINISTRATOR_BY_ID_SQL)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    administratorResult = mapAdministrator(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding administrator with ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
            throw new DataAccessException("Error al buscar el administrador por ID.", sqlException);
        }

        return administratorResult;
    }

    @Override
    public List<Administrator> findAll() throws DataAccessException {
        List<Administrator> administratorList = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_ADMINISTRATORS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                administratorList.add(mapAdministrator(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all administrators: {0}", sqlException.getMessage());
            throw new DataAccessException("Error al recuperar la lista de administradores.", sqlException);
        }

        return administratorList;
    }

    private Administrator mapAdministrator(ResultSet resultSet) throws SQLException {
        return new Administrator(
                resultSet.getInt("id_usuario"),
                resultSet.getString("matricula"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido_paterno"),
                resultSet.getString("apellido_materno"),
                resultSet.getString("contrasenia"),
                resultSet.getString("estado")
        );
    }
}