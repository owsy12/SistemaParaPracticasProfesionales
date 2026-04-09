package Logic.DAO;

import Logic.DTOs.Coordinator;
import Logic.Interface.ICoordinatorDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoordinatorDAO extends UserDAO implements ICoordinatorDAO {

    private static final Logger LOGGER = Logger.getLogger(CoordinatorDAO.class.getName());

    private static final String SELECT_COORDINATOR_BY_ID_SQL =
            "SELECT * FROM usuario WHERE id_usuario = ? AND rol = 'Coordinador'";
    private static final String SELECT_ALL_COORDINATORS_SQL =
            "SELECT * FROM usuario WHERE rol = 'Coordinador'";

    private final Connection connection;

    public CoordinatorDAO(Connection connection) {
        super(connection);
        this.connection = connection;
    }

    @Override
    public boolean save(Coordinator coordinator) {
        boolean isSaved = false;
        try {
            isSaved = super.saveUser(coordinator);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Error saving coordinator: {0}", exception.getMessage());
        }
        return isSaved;
    }

    @Override
    public boolean update(Coordinator coordinator) {
        boolean isUpdated = false;
        try {
            isUpdated = super.update(coordinator);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Error updating coordinator: {0}", exception.getMessage());
        }
        return isUpdated;
    }

    @Override
    public boolean delete(int id) {
        boolean isDeleted = false;
        try {
            isDeleted = super.delete(id);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Error deleting coordinator with ID {0}: {1}",
                    new Object[]{id, exception.getMessage()});
        }
        return isDeleted;
    }

    @Override
    public Optional<Coordinator> findById(int id) {
        Optional<Coordinator> coordinatorResult = Optional.empty();

        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_COORDINATOR_BY_ID_SQL)) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    coordinatorResult = Optional.of(mapCoordinator(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error finding coordinator with ID {0}: {1}",
                    new Object[]{id, sqlException.getMessage()});
        }

        return coordinatorResult;
    }

    @Override
    public List<Coordinator> findAllCoordinators() {
        List<Coordinator> coordinatorList = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_COORDINATORS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                coordinatorList.add(mapCoordinator(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error retrieving all coordinators: {0}", sqlException.getMessage());
        }

        return coordinatorList;
    }

    private Coordinator mapCoordinator(ResultSet resultSet) throws SQLException {
        return new Coordinator(
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