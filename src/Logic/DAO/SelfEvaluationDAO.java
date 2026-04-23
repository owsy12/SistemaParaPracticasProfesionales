package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.SelfEvaluation;
import Logic.Exceptions.ServiceException;
import Logic.Exceptions.DuplicateEntryException;
import Logic.Exceptions.ValidationException;
import Logic.Interface.ISelfEvaluationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelfEvaluationDAO implements ISelfEvaluationDAO {

    private static final Logger LOGGER = Logger.getLogger(SelfEvaluationDAO.class.getName());
    private static final String SQL_INSERT =
            "INSERT INTO autoevaluacion " +
                    "(id_practicante, id_proyecto, periodo, " +
                    " afirmacion_01, afirmacion_02, afirmacion_03, afirmacion_04, afirmacion_05, " +
                    " afirmacion_06, afirmacion_07, afirmacion_08, afirmacion_09, afirmacion_10, " +
                    " puntuacion_final, lugar_fecha, ruta_documento, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
            "SELECT id_autoevaluacion, id_practicante, id_proyecto, periodo, " +
                    "       afirmacion_01, afirmacion_02, afirmacion_03, afirmacion_04, afirmacion_05, " +
                    "       afirmacion_06, afirmacion_07, afirmacion_08, afirmacion_09, afirmacion_10, " +
                    "       puntuacion_final, lugar_fecha, ruta_documento, estado, fecha_entrega " +
                    "FROM autoevaluacion WHERE id_autoevaluacion = ?";
    private static final String SQL_SELECT_ALL =
            "SELECT id_autoevaluacion, id_practicante, id_proyecto, periodo, " +
                    "       afirmacion_01, afirmacion_02, afirmacion_03, afirmacion_04, afirmacion_05, " +
                    "       afirmacion_06, afirmacion_07, afirmacion_08, afirmacion_09, afirmacion_10, " +
                    "       puntuacion_final, lugar_fecha, ruta_documento, estado, fecha_entrega " +
                    "FROM autoevaluacion";

    @Override
    public int save(SelfEvaluation selfEvaluation) throws ServiceException, ValidationException {
        if (selfEvaluation.getIdIntern() <= 0) {
            throw new ValidationException(
                    "El ID del practicante debe ser mayor a cero. ID recibido: "
                            + selfEvaluation.getIdIntern());
        }
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt   (1,  selfEvaluation.getIdIntern());
            statement.setInt   (2,  selfEvaluation.getIdProyect());
            statement.setString(3,  selfEvaluation.getPeriod());
            statement.setInt   (4,  selfEvaluation.getStatement01());
            statement.setInt   (5,  selfEvaluation.getStatement02());
            statement.setInt   (6,  selfEvaluation.getStatement03());
            statement.setInt   (7,  selfEvaluation.getStatement04());
            statement.setInt   (8,  selfEvaluation.getStatement05());
            statement.setInt   (9,  selfEvaluation.getStatement06());
            statement.setInt   (10, selfEvaluation.getStatement07());
            statement.setInt   (11, selfEvaluation.getStatement08());
            statement.setInt   (12, selfEvaluation.getStatement09());
            statement.setInt   (13, selfEvaluation.getStatement10());
            statement.setInt   (14, selfEvaluation.getFinalScore());
            statement.setString(15, selfEvaluation.getPlaceAndDate());
            statement.setString(16, selfEvaluation.getDocumentPath());
            statement.setString(17, selfEvaluation.getStatus());

            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    selfEvaluation.setIdSelfEvalation(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al guardar autoevaluación del practicante {0}: {1}",
                    new Object[]{selfEvaluation.getIdIntern(), sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al guardar la autoevaluación.", sqlException);
        }

        return rowsAffected;
    }

    @Override
    public SelfEvaluation getById(int idSelfEvaluation) throws ServiceException, ValidationException {
        if (idSelfEvaluation <= 0) {
            throw new ValidationException(
                    "El ID de la autoevaluación debe ser mayor a cero. ID recibido: "
                            + idSelfEvaluation);
        }
        SelfEvaluation selfEvaluation = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idSelfEvaluation);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    selfEvaluation = mapResultSet(rs);
                }
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar autoevaluación con ID {0}: {1}",
                    new Object[]{idSelfEvaluation, sqlException.getMessage()});
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException(
                    "Error al recuperar la autoevaluación con ID " + idSelfEvaluation, sqlException);
        }

        return selfEvaluation;
    }

    @Override
    public List<SelfEvaluation> getAll() throws ServiceException {
        List<SelfEvaluation> selfEvaluations = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                selfEvaluations.add(mapResultSet(rs));
            }
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error al recuperar todas las autoevaluaciones: {0}",
                    sqlException.getMessage());
            if (DuplicateEntryException.isDuplicateEntry(sqlException)) {
                throw new DuplicateEntryException(
                        "Ya existe un registro con esa clave en la base de datos.",
                        sqlException);
            }
            throw new ServiceException("Error al recuperar las autoevaluaciones.", sqlException);
        }

        return selfEvaluations;
    }

    private SelfEvaluation mapResultSet(ResultSet rs) throws SQLException {
        SelfEvaluation selfEvaluation = new SelfEvaluation();
        selfEvaluation.setIdSelfEvalation(rs.getInt   ("id_autoevaluacion"));
        selfEvaluation.setIdIntern       (rs.getInt   ("id_practicante"));
        selfEvaluation.setIdProyect      (rs.getInt   ("id_proyecto"));
        selfEvaluation.setPeriod         (rs.getString("periodo"));
        selfEvaluation.setStatement01    (rs.getInt   ("afirmacion_01"));
        selfEvaluation.setStatement02    (rs.getInt   ("afirmacion_02"));
        selfEvaluation.setStatement03    (rs.getInt   ("afirmacion_03"));
        selfEvaluation.setStatement04    (rs.getInt   ("afirmacion_04"));
        selfEvaluation.setStatement05    (rs.getInt   ("afirmacion_05"));
        selfEvaluation.setStatement06    (rs.getInt   ("afirmacion_06"));
        selfEvaluation.setStatement07    (rs.getInt   ("afirmacion_07"));
        selfEvaluation.setStatement08    (rs.getInt   ("afirmacion_08"));
        selfEvaluation.setStatement09    (rs.getInt   ("afirmacion_09"));
        selfEvaluation.setStatement10    (rs.getInt   ("afirmacion_10"));
        selfEvaluation.setFinalScore     (rs.getInt   ("puntuacion_final"));
        selfEvaluation.setPlaceAndDate   (rs.getString("lugar_fecha"));
        selfEvaluation.setDocumentPath   (rs.getString("ruta_documento"));
        selfEvaluation.setStatus         (rs.getString("estado"));
        return selfEvaluation;
    }
}
