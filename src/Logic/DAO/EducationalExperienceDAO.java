package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.EducationalExperience;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IEducationalExperienceDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EducationalExperienceDAO implements IEducationalExperienceDAO {

    private static final Logger LOGGER = Logger.getLogger(EducationalExperienceDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO experiencia_educativa (nrc, nombre, id_profesor) VALUES (?, ?, ?)";

    private static final String SQL_SELECT_BY_NRC =
            "SELECT nrc, nombre, id_profesor FROM experiencia_educativa WHERE nrc = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT nrc, nombre, id_profesor FROM experiencia_educativa";

    private static final String SQL_UPDATE =
            "UPDATE experiencia_educativa SET nombre = ?, id_profesor = ? WHERE nrc = ?";

    private static final String SQL_DELETE =
            "DELETE FROM experiencia_educativa WHERE nrc = ?";

    @Override
    public boolean save(EducationalExperience educationalExperience) throws ServiceException, ValidationException {
        validateEducationalExperience(educationalExperience);

        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {

            statement.setString(1, educationalExperience.getNrc());
            statement.setString(2, educationalExperience.getName());
            statement.setInt(3, educationalExperience.getIdProfessor());

            if (statement.executeUpdate() > 0) {
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar experiencia educativa {0}: {1}",
                    new Object[]{educationalExperience.getNrc(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe una experiencia educativa con ese NRC.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar la experiencia educativa.", sqlException);
        }

        return isSaved;
    }

    @Override
    public EducationalExperience findByNrc(String nrc) throws ServiceException, ValidationException {
        if (nrc == null || nrc.isBlank()) {
            throw new ValidationException("El NRC no puede estar vacío.");
        }

        EducationalExperience result = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_NRC)) {

            statement.setString(1, nrc);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = mapResultSet(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar experiencia educativa con NRC {0}: {1}",
                    new Object[]{nrc, sqlException.getMessage()});
            throw new ServiceException("Error al buscar la experiencia educativa.", sqlException);
        }

        return result;
    }

    @Override
    public List<EducationalExperience> findAll() throws ServiceException {
        List<EducationalExperience> list = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                list.add(mapResultSet(resultSet));
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar experiencias educativas: {0}",
                    sqlException.getMessage());
            throw new ServiceException("Error al recuperar las experiencias educativas.", sqlException);
        }

        return list;
    }

    @Override
    public boolean update(EducationalExperience educationalExperience) throws ServiceException, ValidationException {
        validateEducationalExperience(educationalExperience);

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {

            statement.setString(1, educationalExperience.getName());
            statement.setInt(2, educationalExperience.getIdProfessor());
            statement.setString(3, educationalExperience.getNrc());

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar experiencia educativa {0}: {1}",
                    new Object[]{educationalExperience.getNrc(), sqlException.getMessage()});
            throw new ServiceException("Error al actualizar la experiencia educativa.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean delete(String nrc) throws ServiceException, ValidationException {
        if (nrc == null || nrc.isBlank()) {
            throw new ValidationException("El NRC no puede estar vacío.");
        }

        boolean isDeleted = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setString(1, nrc);

            if (statement.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar experiencia educativa {0}: {1}",
                    new Object[]{nrc, sqlException.getMessage()});
            throw new ServiceException("Error al eliminar la experiencia educativa.", sqlException);
        }

        return isDeleted;
    }

    private void validateEducationalExperience(EducationalExperience educationalExperience) throws ValidationException {
        if (educationalExperience.getNrc() == null || educationalExperience.getNrc().isBlank()) {
            throw new ValidationException("El NRC no puede estar vacío.");
        }
        if (educationalExperience.getName() == null || educationalExperience.getName().isBlank()) {
            throw new ValidationException("El nombre de la experiencia educativa no puede estar vacío.");
        }
        if (educationalExperience.getIdProfessor() <= 0) {
            throw new ValidationException(
                    "El ID del profesor debe ser mayor a cero. ID recibido: " + educationalExperience.getIdProfessor());
        }
    }

    private EducationalExperience mapResultSet(ResultSet resultSet) throws SQLException {
        EducationalExperience educationalExperience = new EducationalExperience();
        educationalExperience.setNrc(resultSet.getString("nrc"));
        educationalExperience.setName(resultSet.getString("nombre"));
        educationalExperience.setIdProfessor(resultSet.getInt("id_profesor"));
        return educationalExperience;
    }
}
