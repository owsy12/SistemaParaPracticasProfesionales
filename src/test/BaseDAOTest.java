package test.Logic;

import DataAccess.DataBaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDAOTest {

    protected static final int ID_INTERN = 1;
    protected static final int ID_COORDINATOR = 2;
    protected static final int ID_PROFESSOR = 3;
    protected static final int ID_ORGANIZATION = 1;
    protected static final int ID_TECHNICAL = 1;
    protected static final int ID_PROJECT = 1;
    protected static final int ID_APPLICATION = 1;
    protected static final int ID_REPORT = 1;

    protected static final String NRC_EDUCATIONAL_EXPERIENCE = "10001";

    protected static final String STATUS_ACTIVE = "Activo";
    protected static final String STATUS_AVAILABLE = "Disponible";
    protected static final String STATUS_PENDING = "Pendiente";
    protected static final String STATUS_ACCEPTED = "Aceptada";

    protected static final String ROLE_INTERN = "Practicante";
    protected static final String ROLE_COORDINATOR = "Coordinador";
    protected static final String ROLE_PROFESSOR = "Profesor";

    protected static final String REPORT_TYPE_PARTIAL = "Parcial";

    private static final int INTERN_CREDITS = 200;
    private static final int PROJECT_MAX_SLOTS = 5;
    private static final int PROJECT_AVAILABLE_SLOTS = 4;
    private static final int PARTIAL_REPORT_NUMBER = 1;
    private static final int PARTIAL_REPORT_HOURS = 40;

    @BeforeEach
    void setUpDatabase() throws SQLException {
        try (Connection conn = DataBaseConnection.connectDatabase();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            cleanAllTables(stmt);
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            insertSupportData(stmt);
        }
    }

    @AfterEach
    void tearDownDatabase() throws SQLException {
        try (Connection conn = DataBaseConnection.connectDatabase();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            cleanAllTables(stmt);
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private void cleanAllTables(Statement stmt) throws SQLException {
        stmt.execute("DELETE FROM evaluacion_ov");
        stmt.execute("DELETE FROM autoevaluacion");
        stmt.execute("DELETE FROM evaluacion_reporte");
        stmt.execute("DELETE FROM reporte_mensual");
        stmt.execute("DELETE FROM reporte_parcial_y_final");
        stmt.execute("DELETE FROM reporte_actividad");
        stmt.execute("DELETE FROM reporte_entregable");
        stmt.execute("DELETE FROM observacion_reporte");
        stmt.execute("DELETE FROM reporte");
        stmt.execute("DELETE FROM formato_inicial");
        stmt.execute("DELETE FROM prorroga");
        stmt.execute("DELETE FROM actividad_practicante");
        stmt.execute("DELETE FROM actividad");
        stmt.execute("DELETE FROM asignacion");
        stmt.execute("DELETE FROM solicitud_proyecto");
        stmt.execute("DELETE FROM solicitud");
        stmt.execute("DELETE FROM practica");
        stmt.execute("DELETE FROM proyecto");
        stmt.execute("DELETE FROM tecnico_responsable");
        stmt.execute("DELETE FROM organizacion_vinculada");
        stmt.execute("DELETE FROM practicante");
        stmt.execute("DELETE FROM profesor");
        stmt.execute("DELETE FROM coordinador");
        stmt.execute("DELETE FROM administrador");
        stmt.execute("DELETE FROM experiencia_educativa");
        stmt.execute("DELETE FROM usuario_rol");
        stmt.execute("DELETE FROM usuario");

        stmt.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE organizacion_vinculada AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE tecnico_responsable AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE proyecto AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE practica AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE actividad AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE actividad_practicante AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE solicitud AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE solicitud_proyecto AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE asignacion AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE reporte AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE reporte_actividad AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE reporte_entregable AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE observacion_reporte AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE autoevaluacion AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE evaluacion_ov AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE formato_inicial AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE prorroga AUTO_INCREMENT = 1");
    }

    private void insertSupportData(Statement stmt) throws SQLException {
        insertSupportUsers(stmt);
        insertSupportRoles(stmt);
        insertSupportUserChildren(stmt);
        insertSupportOrganizationAndTechnical(stmt);
        insertSupportProject(stmt);
        insertSupportApplication(stmt);
        insertSupportReport(stmt);
    }

    private void insertSupportUsers(Statement stmt) throws SQLException {
        stmt.execute(
            "INSERT INTO usuario (id_usuario, matricula, nombre, apellido_paterno, " +
            "apellido_materno, contrasenia, correo) VALUES " +
            "(" + ID_INTERN + ", 'S21013142', 'Ana', 'García', 'López', '$2b$10$hash1', 'ana.garcia@uv.mx')," +
            "(" + ID_COORDINATOR + ", 'P20001001', 'Carlos', 'Ramírez', 'Soto', '$2b$10$hash2', 'carlos.ramirez@uv.mx')," +
            "(" + ID_PROFESSOR + ", 'P20002002', 'Elena', 'Torres', 'Mendoza', '$2b$10$hash3', 'elena.torres@uv.mx')"
        );
    }

    private void insertSupportRoles(Statement stmt) throws SQLException {
        stmt.execute(
            "INSERT INTO usuario_rol (id_usuario, rol, estado) VALUES " +
            "(" + ID_INTERN + ", '" + ROLE_INTERN + "', '" + STATUS_ACTIVE + "')," +
            "(" + ID_COORDINATOR + ", '" + ROLE_COORDINATOR + "', '" + STATUS_ACTIVE + "')," +
            "(" + ID_PROFESSOR + ", '" + ROLE_PROFESSOR + "', '" + STATUS_ACTIVE + "')"
        );
    }

    private void insertSupportUserChildren(Statement stmt) throws SQLException {
        stmt.execute(
            "INSERT INTO practicante (id_usuario, creditos) VALUES (" + ID_INTERN + ", " + INTERN_CREDITS + ")");
        stmt.execute(
            "INSERT INTO coordinador (id_usuario) VALUES (" + ID_COORDINATOR + ")");
        stmt.execute(
            "INSERT INTO profesor (id_usuario, academica) VALUES (" + ID_PROFESSOR + ", 'Ingeniería de Software')");
        stmt.execute(
            "INSERT INTO experiencia_educativa (nrc, nombre, id_profesor) VALUES " +
            "('" + NRC_EDUCATIONAL_EXPERIENCE + "', 'Prácticas Profesionales', " + ID_PROFESSOR + ")");
    }

    private void insertSupportOrganizationAndTechnical(Statement stmt) throws SQLException {
        stmt.execute(
            "INSERT INTO organizacion_vinculada (id_organizacion, nombre_organizacion, " +
            "correo_organizacion, direccion, sector, estado) VALUES " +
            "(" + ID_ORGANIZATION + ", 'TechCorp SA', 'contacto@techcorp.mx', " +
            "'Av. Central 100, Xalapa', 'Tecnología', 'Activa')"
        );
        stmt.execute(
            "INSERT INTO tecnico_responsable (id_tecnico, id_organizacion, nombre, " +
            "apellido_paterno, apellido_materno, correo_responsable, cargo) VALUES " +
            "(" + ID_TECHNICAL + ", " + ID_ORGANIZATION + ", 'Luis', 'Pérez', 'Vega', " +
            "'luis.perez@techcorp.mx', 'Gerente de TI')"
        );
    }

    private void insertSupportProject(Statement stmt) throws SQLException {
        stmt.execute(
            "INSERT INTO proyecto (id_proyecto, id_organizacion, id_tecnico, id_profesor, nrc, " +
            "nombre, descripcion, fecha_inicio, fecha_fin, cupo_maximo, cupo_disponible, estado) " +
            "VALUES (" + ID_PROJECT + ", " + ID_ORGANIZATION + ", " + ID_TECHNICAL + ", " +
            ID_PROFESSOR + ", '" + NRC_EDUCATIONAL_EXPERIENCE + "', " +
            "'Sistema de Inventario', 'Sistema web de inventario.', " +
            "'2025-01-15', '2025-07-15', " + PROJECT_MAX_SLOTS + ", " + PROJECT_AVAILABLE_SLOTS + ", '" + STATUS_AVAILABLE + "')"
        );
    }

    private void insertSupportApplication(Statement stmt) throws SQLException {
        stmt.execute(
            "INSERT INTO solicitud (id_solicitud, id_practicante, estado) " +
            "VALUES (" + ID_APPLICATION + ", " + ID_INTERN + ", '" + STATUS_ACCEPTED + "')"
        );
        stmt.execute(
            "INSERT INTO solicitud_proyecto (id_solicitud, id_proyecto, orden_preferencia) " +
            "VALUES (" + ID_APPLICATION + ", " + ID_PROJECT + ", 1)"
        );
    }

    private void insertSupportReport(Statement stmt) throws SQLException {
        stmt.execute(
            "INSERT INTO reporte (id_reporte, id_practicante, id_proyecto, " +
            "tipo_reporte, periodo, ruta_documento, estado) " +
            "VALUES (" + ID_REPORT + ", " + ID_INTERN + ", " + ID_PROJECT + ", " +
            "'" + REPORT_TYPE_PARTIAL + "', '2025-01', '/docs/reporte_1.pdf', '" + STATUS_PENDING + "')"
        );
        stmt.execute(
            "INSERT INTO reporte_parcial_y_final (id_reporte_parcial, numero_informe, " +
            "horas_cubiertas, objetivo_general, metodologia, resultados_obtenidos, observaciones) " +
            "VALUES (" + ID_REPORT + ", " + PARTIAL_REPORT_NUMBER + ", " + PARTIAL_REPORT_HOURS + ", " +
            "'Objetivo general', 'Scrum', 'Resultados ok', 'Sin obs')"
        );
    }
}
