import DataAccess.DataBaseConnection;
import Logic.Exceptions.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseDAOTest {

    private static final Logger LOGGER = Logger.getLogger(BaseDAOTest.class.getName());

    private static final String DISABLE_FK_CHECKS = "SET FOREIGN_KEY_CHECKS = 0";
    private static final String ENABLE_FK_CHECKS = "SET FOREIGN_KEY_CHECKS = 1";

    private static final String[] DELETE_STATEMENTS = {
            "DELETE FROM reporte_mensual",
            "DELETE FROM reporte_parcial_y_final",
            "DELETE FROM reporte_actividad",
            "DELETE FROM reporte_entregable",
            "DELETE FROM observacion_reporte",
            "DELETE FROM reporte",
            "DELETE FROM autoevaluacion",
            "DELETE FROM evaluacion_ov",
            "DELETE FROM formato_inicial",
            "DELETE FROM prorroga",
            "DELETE FROM actividad_practicante",
            "DELETE FROM actividad",
            "DELETE FROM asignacion",
            "DELETE FROM solicitud_proyecto",
            "DELETE FROM solicitud",
            "DELETE FROM practica",
            "DELETE FROM proyecto",
            "DELETE FROM tecnico_responsable",
            "DELETE FROM organizacion_vinculada",
            "DELETE FROM practicante",
            "DELETE FROM profesor",
            "DELETE FROM coordinador",
            "DELETE FROM administrador",
            "DELETE FROM experiencia_educativa",
            "DELETE FROM usuario_rol",
            "DELETE FROM usuario"
    };

    private static final String[] RESET_AUTO_INCREMENT_STATEMENTS = {
            "ALTER TABLE usuario AUTO_INCREMENT = 1",
            "ALTER TABLE organizacion_vinculada AUTO_INCREMENT = 1",
            "ALTER TABLE tecnico_responsable AUTO_INCREMENT = 1",
            "ALTER TABLE proyecto AUTO_INCREMENT = 1",
            "ALTER TABLE practica AUTO_INCREMENT = 1",
            "ALTER TABLE actividad AUTO_INCREMENT = 1",
            "ALTER TABLE actividad_practicante AUTO_INCREMENT = 1",
            "ALTER TABLE solicitud AUTO_INCREMENT = 1",
            "ALTER TABLE solicitud_proyecto AUTO_INCREMENT = 1",
            "ALTER TABLE asignacion AUTO_INCREMENT = 1",
            "ALTER TABLE reporte AUTO_INCREMENT = 1",
            "ALTER TABLE reporte_actividad AUTO_INCREMENT = 1",
            "ALTER TABLE reporte_entregable AUTO_INCREMENT = 1",
            "ALTER TABLE observacion_reporte AUTO_INCREMENT = 1",
            "ALTER TABLE autoevaluacion AUTO_INCREMENT = 1",
            "ALTER TABLE evaluacion_ov AUTO_INCREMENT = 1",
            "ALTER TABLE formato_inicial AUTO_INCREMENT = 1",
            "ALTER TABLE prorroga AUTO_INCREMENT = 1"
    };

    @BeforeEach
    protected void cleanDatabaseBeforeTest() throws ServiceException {
        resetDatabase();
    }

    @AfterEach
    protected void cleanDatabaseAfterTest() throws ServiceException {
        resetDatabase();
    }

    private void resetDatabase() throws ServiceException {
        try (Connection connection = DataBaseConnection.connectDatabase();
             Statement statement = connection.createStatement()) {
            statement.execute(DISABLE_FK_CHECKS);
            for (String deleteStatement : DELETE_STATEMENTS) {
                statement.execute(deleteStatement);
            }
            for (String resetStatement : RESET_AUTO_INCREMENT_STATEMENTS) {
                statement.execute(resetStatement);
            }
            statement.execute(ENABLE_FK_CHECKS);
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, "Error resetting test database: {0}", sqlException.getMessage());
            throw new ServiceException("Error resetting test database", sqlException);
        }
    }
}
