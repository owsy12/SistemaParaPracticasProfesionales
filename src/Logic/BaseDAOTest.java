package Logic;

import DataAccess.BDConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase base para todos los tests de DAOs.
 *
 * ESTRATEGIA:
 *   @BeforeEach  → desactiva FK, limpia tablas, reactiva FK,
 *                  inserta datos mínimos de soporte (usuario,
 *                  practicante, proyecto, etc.)
 *   @AfterEach   → limpia todas las tablas en orden inverso
 *                  para dejar la BD en estado inicial.
 *
 * Así cada test parte de un estado conocido y no depende
 * del orden de ejecución.
 */
public abstract class BaseDAOTest {

    // IDs fijos de los registros de soporte
    protected static final int ID_PRACTICANTE  = 1;
    protected static final int ID_COORDINADOR  = 2;
    protected static final int ID_PROFESOR     = 3;
    protected static final int ID_ORGANIZACION = 1;
    protected static final int ID_TECNICO      = 1;
    protected static final int ID_PROYECTO     = 1;
    protected static final int ID_SOLICITUD    = 1;
    protected static final int ID_REPORTE      = 1;

    @BeforeEach
    void setUpDatabase() throws SQLException {
        try (Connection conn = BDConnection.connectDatabase();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            cleanAllTables(stmt);
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            insertSupportData(stmt);
        }
    }

    @AfterEach
    void tearDownDatabase() throws SQLException {
        try (Connection conn = BDConnection.connectDatabase();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            cleanAllTables(stmt);
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    // ---------------------------------------------------------------
    // Limpia en orden inverso al de creación (hijas antes que padres)
    // ---------------------------------------------------------------
    private void cleanAllTables(Statement stmt) throws SQLException {
        stmt.execute("DELETE FROM evaluacion_ov");
        stmt.execute("DELETE FROM autoevaluacion");
        stmt.execute("DELETE FROM evaluacion_reporte");
        stmt.execute("DELETE FROM reporte_mensual");
        stmt.execute("DELETE FROM reporte_parcial_y_final");
        stmt.execute("DELETE FROM reporte");
        stmt.execute("DELETE FROM formato_inicial");
        stmt.execute("DELETE FROM asignacion");
        stmt.execute("DELETE FROM solicitud_proyecto");
        stmt.execute("DELETE FROM solicitud");
        stmt.execute("DELETE FROM proyecto");
        stmt.execute("DELETE FROM tecnico_responsable");
        stmt.execute("DELETE FROM organizacion_vinculada");
        stmt.execute("DELETE FROM practicante");
        stmt.execute("DELETE FROM profesor");
        stmt.execute("DELETE FROM coordinador");
        stmt.execute("DELETE FROM administrador");
        stmt.execute("DELETE FROM usuario_rol");
        stmt.execute("DELETE FROM usuario");

        // Reinicia AUTO_INCREMENT para IDs predecibles
        stmt.execute("ALTER TABLE usuario             AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE organizacion_vinculada AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE tecnico_responsable AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE proyecto            AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE solicitud           AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE solicitud_proyecto  AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE asignacion          AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE reporte             AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE evaluacion_reporte  AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE autoevaluacion      AUTO_INCREMENT = 1");
        stmt.execute("ALTER TABLE formato_inicial     AUTO_INCREMENT = 1");
    }

    // ---------------------------------------------------------------
    // Inserta el mínimo necesario para satisfacer FK de todos los DAOs
    // ---------------------------------------------------------------
    private void insertSupportData(Statement stmt) throws SQLException {

        // usuarios
        stmt.execute(
            "INSERT INTO usuario (id_usuario, matricula, nombre, apellido_paterno, " +
            "apellido_materno, contrasenia, estado) VALUES " +
            "(" + ID_PRACTICANTE + ", 'S21013142', 'Ana',    'García',  'López',   '$2b$10$hash1', 'Activo')," +
            "(" + ID_COORDINADOR + ", 'P20001001', 'Carlos', 'Ramírez', 'Soto',    '$2b$10$hash2', 'Activo')," +
            "(" + ID_PROFESOR    + ", 'P20002002', 'Elena',  'Torres',  'Mendoza', '$2b$10$hash3', 'Activo')"
        );

        // roles
        stmt.execute(
            "INSERT INTO usuario_rol (id_usuario, rol) VALUES " +
            "(" + ID_PRACTICANTE + ", 'Practicante')," +
            "(" + ID_COORDINADOR + ", 'Coordinador')," +
            "(" + ID_PROFESOR    + ", 'Profesor')"
        );

        // tablas hija de usuario
        stmt.execute(
            "INSERT INTO practicante (id_usuario, creditos) VALUES (" + ID_PRACTICANTE + ", 200)");
        stmt.execute(
            "INSERT INTO coordinador (id_usuario) VALUES (" + ID_COORDINADOR + ")");
        stmt.execute(
            "INSERT INTO profesor (id_usuario, academica) VALUES (" + ID_PROFESOR + ", 'Ingeniería de Software')");

        // organización y técnico
        stmt.execute(
            "INSERT INTO organizacion_vinculada (id_organizacion, nombre_organizacion, " +
            "correo_organizacion, direccion, sector) VALUES " +
            "(" + ID_ORGANIZACION + ", 'TechCorp SA', 'contacto@techcorp.mx', " +
            "'Av. Central 100, Xalapa', 'Tecnología')"
        );
        stmt.execute(
            "INSERT INTO tecnico_responsable (id_tecnico, id_organizacion, nombre, " +
            "apellido_paterno, apellido_materno, correo_responsable, cargo) VALUES " +
            "(" + ID_TECNICO + ", " + ID_ORGANIZACION + ", 'Luis', 'Pérez', 'Vega', " +
            "'luis.perez@techcorp.mx', 'Gerente de TI')"
        );

        // proyecto
        stmt.execute(
            "INSERT INTO proyecto (id_proyecto, id_organizacion, id_tecnico, id_coordinador, " +
            "nombre, descripcion, fecha_inicio, fecha_fin, cupo_maximo, cupo_disponible, estado) " +
            "VALUES (" + ID_PROYECTO + ", " + ID_ORGANIZACION + ", " + ID_TECNICO + ", " + ID_COORDINADOR + ", " +
            "'Sistema de Inventario', 'Sistema web de inventario.', " +
            "'2025-01-15', '2025-07-15', 5, 4, 'Disponible')"
        );

        // solicitud (FK de asignacion)
        stmt.execute(
            "INSERT INTO solicitud (id_solicitud, id_practicante, estado) " +
            "VALUES (" + ID_SOLICITUD + ", " + ID_PRACTICANTE + ", 'Aceptada')"
        );
        stmt.execute(
            "INSERT INTO solicitud_proyecto (id_solicitud, id_proyecto, orden_preferencia) " +
            "VALUES (" + ID_SOLICITUD + ", " + ID_PROYECTO + ", 1)"
        );

        // reporte base + parcial (FK de evaluacion_reporte)
        stmt.execute(
            "INSERT INTO reporte (id_reporte, id_practicante, id_proyecto, " +
            "tipo_reporte, periodo, ruta_documento, estado) " +
            "VALUES (" + ID_REPORTE + ", " + ID_PRACTICANTE + ", " + ID_PROYECTO + ", " +
            "'Parcial', '2025-01', '/docs/reporte_1.pdf', 'Pendiente')"
        );
        stmt.execute(
            "INSERT INTO reporte_parcial_y_final (id_reporte_parcial, numero_informe, " +
            "horas_cubiertas, objetivo_general, metodologia, resultados_obtenidos, observaciones) " +
            "VALUES (" + ID_REPORTE + ", 1, 40, 'Objetivo general', 'Scrum', 'Resultados ok', 'Sin obs')"
        );
    }
}
