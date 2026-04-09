package Logic.DAO;

import DataAccess.DataBaseConnection;
import Logic.DTOs.SelfEvaluation;
import Logic.Interface.ISelfEvaluationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SelfEvaluationDAO implements ISelfEvaluationDAO {
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
                    "FROM autoevaluacion " +
                    "WHERE id_autoevaluacion = ?";

    private static final String SQL_SELECT_ALL =
            "SELECT id_autoevaluacion, id_practicante, id_proyecto, periodo, " +
                    "       afirmacion_01, afirmacion_02, afirmacion_03, afirmacion_04, afirmacion_05, " +
                    "       afirmacion_06, afirmacion_07, afirmacion_08, afirmacion_09, afirmacion_10, " +
                    "       puntuacion_final, lugar_fecha, ruta_documento, estado, fecha_entrega " +
                    "FROM autoevaluacion";

    // ---------------------------------------------------------------

    @Override
    public int save(SelfEvaluation selfEvaluation) throws SQLException {
        int rowsAffected = 0;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(
                     SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt   (1,  selfEvaluation.getIdIntern());
            statement.setInt   (2,  selfEvaluation.getIdProyect());
            statement.setString(3,  selfEvaluation.getPeriod());
            statement.setInt   (4,  selfEvaluation.getStatement01());
            statement.setInt(5,  selfEvaluation.getStatement02());
            statement.setInt(6,  selfEvaluation.getStatement03());
            statement.setInt(7,  selfEvaluation.getStatement04());
            statement.setInt(8,  selfEvaluation.getStatement05());
            statement.setInt(9,  selfEvaluation.getStatement06());
            statement.setInt(10, selfEvaluation.getStatement07());
            statement.setInt(11, selfEvaluation.getStatement08());
            statement.setInt(12, selfEvaluation.getStatement09());
            statement.setInt(13, selfEvaluation.getStatement10());
            statement.setInt(14, selfEvaluation.getFinalScore());
            statement.setString(15, selfEvaluation.getPlaceAndDate());
            statement.setString(16, selfEvaluation.getDocumentPath());
            statement.setString(17, selfEvaluation.getStatus());


            rowsAffected = statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    selfEvaluation.setIdSelfEvalation(generatedKeys.getInt(1));
                }
            }
        }

        return rowsAffected;
    }

    @Override
    public SelfEvaluation getById(int idSelfEvaluation) throws SQLException {
        SelfEvaluation selfEvaluation = null;

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {

            statement.setInt(1, idSelfEvaluation);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    selfEvaluation = mapResultSet(resultSet);
                }
            }
        }

        return selfEvaluation;
    }

    @Override
    public List<SelfEvaluation> getAll() throws SQLException {
        List<SelfEvaluation> selfEvaluations = new ArrayList<>();

        try (Connection connection = DataBaseConnection.connectDatabase();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                selfEvaluations.add(mapResultSet(resultSet));
            }
        }

        return selfEvaluations;
    }


    private SelfEvaluation mapResultSet(ResultSet resultSet) throws SQLException {
        SelfEvaluation selfEvaluation = new SelfEvaluation();
        selfEvaluation.setIdSelfEvalation(resultSet.getInt   ("id_autoevaluacion"));
        selfEvaluation.setIdIntern       (resultSet.getInt   ("id_practicante"));
        selfEvaluation.setIdProyect      (resultSet.getInt   ("id_proyecto"));
        selfEvaluation.setPeriod         (resultSet.getString("periodo"));
        selfEvaluation.setStatement01    (resultSet.getInt   ("afirmacion_01"));
        selfEvaluation.setStatement02(resultSet.getInt("afirmacion_02"));
        selfEvaluation.setStatement03(resultSet.getInt("afirmacion_03"));
        selfEvaluation.setStatement04(resultSet.getInt("afirmacion_04"));
        selfEvaluation.setStatement05(resultSet.getInt("afirmacion_05"));
        selfEvaluation.setStatement06(resultSet.getInt("afirmacion_06"));
        selfEvaluation.setStatement07(resultSet.getInt("afirmacion_07"));
        selfEvaluation.setStatement08(resultSet.getInt("afirmacion_08"));
        selfEvaluation.setStatement09(resultSet.getInt("afirmacion_09"));
        selfEvaluation.setStatement10(resultSet.getInt("afirmacion_10"));
        selfEvaluation.setFinalScore(resultSet.getInt("puntuacion_final"));
        selfEvaluation.setPlaceAndDate(resultSet.getString("lugar_fecha"));
        selfEvaluation.setDocumentPath(resultSet.getString("ruta_documento"));
        selfEvaluation.setStatus(resultSet.getString("estado"));
        return selfEvaluation;
    }
}
