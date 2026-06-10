public final class TestConstants {

    public static final int INVALID_ID_ZERO = 0;
    public static final int INVALID_ID_NEGATIVE = -1;
    public static final int NON_EXISTENT_ID = 999999;
    public static final int SINGLE_RESULT = 1;
    public static final int TWO_RESULTS = 2;
    public static final int THREE_RESULTS = 3;
    public static final int ZERO_RESULTS = 0;
    public static final int ONE_ROW_AFFECTED = 1;

    public static final int DEFAULT_INTERN_CREDITS = 200;
    public static final int DEFAULT_PROJECT_MAX_SLOTS = 5;
    public static final int DEFAULT_PROJECT_AVAILABLE_SLOTS = 4;
    public static final int DEFAULT_DEDICATED_HOURS = 8;
    public static final int DEFAULT_REPORTED_HOURS = 40;
    public static final int DEFAULT_REPORT_NUMBER = 1;
    public static final int DEFAULT_ADVANCE_PERCENTAGE = 50;
    public static final int DEFAULT_PREFERENCE_ORDER = 1;
    public static final int DEFAULT_STATEMENT_SCORE = 5;
    public static final int DEFAULT_FINAL_SCORE = 50;
    public static final int DEFAULT_REPORT_YEAR = 2025;
    public static final int DEFAULT_MONTHLY_HOURS = 80;

    public static final String DEFAULT_NRC = "10001";
    public static final String ALTERNATE_NRC = "20002";
    public static final String UNUSED_NRC = "99999";
    public static final String BLANK_TEXT = "  ";

    public static final String STATUS_ACTIVE_USER = "Activo";
    public static final String STATUS_INACTIVE_USER = "Inactivo";
    public static final String STATUS_ACTIVE_ORG = "Activa";
    public static final String STATUS_INACTIVE_ORG = "Inactiva";
    public static final String STATUS_AVAILABLE = "Disponible";
    public static final String STATUS_CANCELLED = "Cancelado";
    public static final String STATUS_PENDING = "Pendiente";
    public static final String STATUS_ACCEPTED = "Aceptada";
    public static final String STATUS_CANCELLED_REQUEST = "Cancelada";
    public static final String STATUS_REJECTED = "Rechazada";
    public static final String STATUS_PRACTICE_ACTIVE = "Activa";
    public static final String STATUS_PRACTICE_CONCLUDED = "Concluida";
    public static final String STATUS_ASSIGNMENT_ACTIVE = "Activa";
    public static final String STATUS_ASSIGNMENT_CONCLUDED = "Concluida";
    public static final String STATUS_ACTIVITY_ACTIVE = "Activa";
    public static final String STATUS_ACTIVITY_INACTIVE = "Inactiva";
    public static final String STATUS_INTERN_ACTIVITY_PENDING = "Pendiente";
    public static final String STATUS_INTERN_ACTIVITY_IN_PROGRESS = "En Progreso";
    public static final String STATUS_INTERN_ACTIVITY_COMPLETED = "Completada";
    public static final String STATUS_REPORT_PENDING = "Pendiente";
    public static final String STATUS_REPORT_IN_REVIEW = "En revision";
    public static final String STATUS_REPORT_EVALUATED = "Evaluado";
    public static final String STATUS_SELF_EVAL_PENDING = "Pendiente";
    public static final String STATUS_SELF_EVAL_DELIVERED = "Entregada";
    public static final String STATUS_OV_EVAL_PENDING = "Pendiente";
    public static final String STATUS_OV_EVAL_DELIVERED = "Entregada";
    public static final String STATUS_INITIAL_FORMAT_PENDING = "Pendiente";
    public static final String STATUS_INITIAL_FORMAT_DELIVERED = "Entregado";

    public static final String ROLE_ADMINISTRATOR = "Administrador";
    public static final String ROLE_COORDINATOR = "Coordinador";
    public static final String ROLE_PROFESSOR = "Profesor";
    public static final String ROLE_INTERN = "Practicante";
    public static final String UNUSED_ROLE = "Administrador";

    public static final String REPORT_TYPE_PARTIAL = "Parcial";
    public static final String REPORT_TYPE_FINAL = "Final";
    public static final String REPORT_TYPE_MONTHLY = "Mensual";

    public static final String INITIAL_FORMAT_TYPE_ASSIGNMENT = "Carta de Asignación";
    public static final String INITIAL_FORMAT_TYPE_SCHEDULE = "Horario";
    public static final String INITIAL_FORMAT_TYPE_CERTIFICATE = "Certificado de Seguro";
    public static final String INITIAL_FORMAT_TYPE_TIMELINE = "Cronograma de Actividades";

    public static final String DEFAULT_PASSWORD_HASH = "$2b$10$placeholderHashForTesting";
    public static final String DEFAULT_DOCUMENT_PATH = "/documents/test_document.pdf";
    public static final String DEFAULT_SIGNED_PATH = "/documents/signed_document.pdf";
    public static final String DEFAULT_PERIOD = "2025-01";
    public static final String DEFAULT_REPORT_MONTH = "Enero";
    public static final String DEFAULT_PLACE_AND_DATE = "Xalapa, 2025-01-15";
    public static final String DEFAULT_BLOCK = "Bloque A";
    public static final String DEFAULT_SECTION = "Sección 1";
    public static final String DEFAULT_FEEDBACK = "Buen avance del periodo.";
    public static final String DEFAULT_COMMENT = "Observación de seguimiento.";
    public static final String DEFAULT_OBSERVATIONS = "Sin observaciones adicionales.";
    public static final String DEFAULT_GENERAL_OBJECTIVE = "Objetivo general del reporte.";
    public static final String DEFAULT_METHODOLOGY = "Metodología Scrum.";
    public static final String DEFAULT_OBTAINED_RESULTS = "Resultados satisfactorios.";
    public static final String DEFAULT_PLAN_WEEKS = "1-4";
    public static final String DEFAULT_REAL_WEEKS = "1-5";
    public static final String DEFAULT_ASSIGNMENT_REASON = "Asignación por orden de preferencia.";
    public static final String DEFAULT_DELIVERABLE_RESULT = "Entregable validado.";
    public static final String DEFAULT_DELIVERABLE_DESCRIPTION = "Descripción del entregable.";

    private TestConstants() {
    }
}
