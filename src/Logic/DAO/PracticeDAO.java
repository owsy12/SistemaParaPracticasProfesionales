package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.Practice;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.IPracticeDAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PracticeDAO implements IPracticeDAO {

    private static final Logger LOGGER = Logger.getLogger(PracticeDAO.class.getName());

    private static final String DEFAULT_STATUS = "Activa";

    private static final String SQL_INSERT =
            "INSERT INTO practica (nrc, id_practicante, fecha_inicio, fecha_fin, estado, calificacion) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_COLUMNS =
            "SELECT id_practica, nrc, id_practicante, fecha_inicio, fecha_fin, estado, calificacion ";

    private static final String SQL_SELECT_BY_ID =
            SQL_SELECT_COLUMNS + "FROM practica WHERE id_practica = ?";

    private static final String SQL_SELECT_BY_NRC =
            SQL_SELECT_COLUMNS + "FROM practica WHERE nrc = ?";

    private static final String SQL_SELECT_BY_INTERN =
            SQL_SELECT_COLUMNS + "FROM practica WHERE id_practicante = ?";

    private static final String SQL_SELECT_ACTIVE_BY_INTERN =
            SQL_SELECT_COLUMNS +
            "FROM practica WHERE id_practicante = ? AND estado = 'Activa' LIMIT 1";

    private static final String SQL_HAS_CONCLUDED =
            "SELECT COUNT(*) AS total FROM practica WHERE id_practicante = ? AND estado = 'Concluida'";

    private static final String SQL_CANCEL_BY_INTERN_AND_PROJECT =
            "UPDATE practica p " +
            "INNER JOIN proyecto pr ON p.nrc = pr.nrc " +
            "SET p.estado = 'Cancelada' " +
            "WHERE p.id_practicante = ? AND pr.id_proyecto = ? AND p.estado = 'Activa'";

    private static final String SQL_REACTIVATE_CANCELLED =
            "UPDATE practica SET estado = 'Activa', fecha_inicio = ? " +
            "WHERE id_practicante = ? AND nrc = ? AND estado = 'Cancelada' LIMIT 1";

    private static final String SQL_UPDATE =
            "UPDATE practica SET nrc = ?, id_practicante = ?, fecha_inicio = ?, " +
                    "fecha_fin = ?, estado = ?, calificacion = ? WHERE id_practica = ?";

    private static final String SQL_DELETE =
            "DELETE FROM practica WHERE id_practica = ?";

    @Override
    public boolean save(Practice practice) throws ServiceException, ValidationException {
        validatePractice(practice);

        boolean isSaved = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, practice.getNrc());
            statement.setInt(2, practice.getIdIntern());
            statement.setDate(3, Date.valueOf(practice.getStartDate()));
            statement.setDate(4, practice.getEndDate() != null ? Date.valueOf(practice.getEndDate()) : null);
            statement.setString(5, DEFAULT_STATUS);
            statement.setBigDecimal(6, practice.getGrade() != null
                    ? java.math.BigDecimal.valueOf(practice.getGrade()) : null);

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        practice.setIdPractice(generatedKeys.getInt(1));
                    }
                }
                isSaved = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar practica para practicante {0}: {1}",
                    new Object[]{practice.getIdIntern(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "El practicante ya tiene una practica registrada en esa experiencia educativa.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar la practica.", sqlException);
        }

        return isSaved;
    }

    @Override
    public Practice findById(int idPractice) throws ServiceException, ValidationException {
        if (idPractice <= 0) {
            throw new ValidationException(
                    "El ID de la practica debe ser mayor a cero. ID recibido: " + idPractice);
        }

        Practice result = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idPractice);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = mapResultSet(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar practica con ID {0}: {1}",
                    new Object[]{idPractice, sqlException.getMessage()});
            throw new ServiceException("Error al buscar la practica.", sqlException);
        }

        return result;
    }

    @Override
    public List<Practice> findByNrc(String nrc) throws ServiceException, ValidationException {
        if (nrc == null || nrc.isBlank()) {
            throw new ValidationException("El NRC no puede estar vacío.");
        }

        List<Practice> list = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_NRC)) {

            statement.setString(1, nrc);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar practicas por NRC {0}: {1}",
                    new Object[]{nrc, sqlException.getMessage()});
            throw new ServiceException("Error al buscar las practicas por NRC.", sqlException);
        }

        return list;
    }

    @Override
    public List<Practice> findByIntern(int idIntern) throws ServiceException, ValidationException {
        if (idIntern <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + idIntern);
        }

        List<Practice> list = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_INTERN)) {

            statement.setInt(1, idIntern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapResultSet(resultSet));
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar practicas del practicante {0}: {1}",
                    new Object[]{idIntern, sqlException.getMessage()});
            throw new ServiceException("Error al buscar las practicas del practicante.", sqlException);
        }

        return list;
    }

    @Override
    public boolean update(Practice practice) throws ServiceException, ValidationException {
        validatePractice(practice);
        if (practice.getIdPractice() <= 0) {
            throw new ValidationException(
                    "El ID de la practica debe ser mayor a cero. ID recibido: " + practice.getIdPractice());
        }

        boolean isUpdated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {

            statement.setString(1, practice.getNrc());
            statement.setInt(2, practice.getIdIntern());
            statement.setDate(3, Date.valueOf(practice.getStartDate()));
            statement.setDate(4, practice.getEndDate() != null ? Date.valueOf(practice.getEndDate()) : null);
            statement.setString(5, practice.getStatus());
            statement.setBigDecimal(6, practice.getGrade() != null
                    ? java.math.BigDecimal.valueOf(practice.getGrade()) : null);
            statement.setInt(7, practice.getIdPractice());

            if (statement.executeUpdate() > 0) {
                isUpdated = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al actualizar practica con ID {0}: {1}",
                    new Object[]{practice.getIdPractice(), sqlException.getMessage()});
            throw new ServiceException("Error al actualizar la practica.", sqlException);
        }

        return isUpdated;
    }

    @Override
    public boolean delete(int idPractice) throws ServiceException, ValidationException {
        if (idPractice <= 0) {
            throw new ValidationException(
                    "El ID de la practica debe ser mayor a cero. ID recibido: " + idPractice);
        }

        boolean isDeleted = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE)) {

            statement.setInt(1, idPractice);

            if (statement.executeUpdate() > 0) {
                isDeleted = true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al eliminar practica con ID {0}: {1}",
                    new Object[]{idPractice, sqlException.getMessage()});
            throw new ServiceException("Error al eliminar la practica.", sqlException);
        }

        return isDeleted;
    }

    public Practice findActiveByIntern(int internId) throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }

        Practice result = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ACTIVE_BY_INTERN)) {

            statement.setInt(1, internId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = mapResultSet(resultSet);
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al buscar práctica activa del practicante {0}: {1}",
                    new Object[]{internId, sqlException.getMessage()});
            throw new ServiceException("Error al buscar la práctica activa.", sqlException);
        }

        return result;
    }

    public boolean hasConcludedPractice(int internId) throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }

        boolean hasConcluded = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_HAS_CONCLUDED)) {

            statement.setInt(1, internId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    hasConcluded = resultSet.getInt("total") > 0;
                }
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al verificar práctica concluida del practicante {0}: {1}",
                    new Object[]{internId, sqlException.getMessage()});
            throw new ServiceException("Error al verificar estado de la práctica.", sqlException);
        }

        return hasConcluded;
    }

    public boolean cancelActiveByInternAndProject(int internId, int projectId)
            throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }
        if (projectId <= 0) {
            throw new ValidationException(
                    "El ID del proyecto debe ser mayor a cero. ID recibido: " + projectId);
        }

        boolean updated = false;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_CANCEL_BY_INTERN_AND_PROJECT)) {

            statement.setInt(1, internId);
            statement.setInt(2, projectId);
            updated = statement.executeUpdate() > 0;

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al cancelar práctica del practicante {0} en proyecto {1}: {2}",
                    new Object[]{internId, projectId, sqlException.getMessage()});
            throw new ServiceException("Error al cancelar la práctica.", sqlException);
        }

        return updated;
    }

    public boolean reactivateOrCreate(int internId, String nrc, java.time.LocalDate startDate)
            throws ServiceException, ValidationException {
        if (internId <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + internId);
        }
        if (nrc == null || nrc.isBlank()) {
            throw new ValidationException("El NRC del proyecto no puede estar vacío.");
        }

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement updateStmt = connection.prepareStatement(SQL_REACTIVATE_CANCELLED)) {

            updateStmt.setDate  (1, Date.valueOf(startDate));
            updateStmt.setInt   (2, internId);
            updateStmt.setString(3, nrc);

            boolean reactivated = updateStmt.executeUpdate() > 0;
            if (reactivated) {
                return true;
            }

        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE,
                    "Error al reactivar práctica del practicante {0}: {1}",
                    new Object[]{internId, sqlException.getMessage()});
            throw new ServiceException("Error al reactivar la práctica.", sqlException);
        }

        Practice practice = new Practice();
        practice.setNrc(nrc);
        practice.setIdIntern(internId);
        practice.setStartDate(startDate);
        practice.setStatus(DEFAULT_STATUS);
        return save(practice);
    }

    private void validatePractice(Practice practice) throws ValidationException {
        if (practice.getNrc() == null || practice.getNrc().isBlank()) {
            throw new ValidationException("El NRC no puede estar vacío.");
        }
        if (practice.getIdIntern() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: " + practice.getIdIntern());
        }
        if (practice.getStartDate() == null) {
            throw new ValidationException("La fecha de inicio no puede estar vacía.");
        }
    }

    private Practice mapResultSet(ResultSet resultSet) throws SQLException {
        Practice practice = new Practice();
        practice.setIdPractice(resultSet.getInt("id_practica"));
        practice.setNrc(resultSet.getString("nrc"));
        practice.setIdIntern(resultSet.getInt("id_practicante"));
        practice.setStartDate(resultSet.getDate("fecha_inicio").toLocalDate());

        Date endDate = resultSet.getDate("fecha_fin");
        if (endDate != null) {
            practice.setEndDate(endDate.toLocalDate());
        }

        practice.setStatus(resultSet.getString("estado"));

        java.math.BigDecimal grade = resultSet.getBigDecimal("calificacion");
        if (grade != null) {
            practice.setGrade(grade.doubleValue());
        }

        return practice;
    }
}
