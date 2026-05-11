
SET FOREIGN_KEY_CHECKS = 0;
DROP DATABASE IF EXISTS spp;
CREATE DATABASE spp
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE spp;

CREATE TABLE usuario (
                         id_usuario        INT          NOT NULL AUTO_INCREMENT,
                         matricula         VARCHAR(20)  NOT NULL,
                         nombre            VARCHAR(80)  NOT NULL,
                         apellido_paterno  VARCHAR(60)  NOT NULL,
                         apellido_materno  VARCHAR(60)  NOT NULL,
                         contrasenia       VARCHAR(255) NOT NULL COMMENT 'Hash bcrypt',
                         correo            VARCHAR(120) NOT NULL,
                         PRIMARY KEY (id_usuario),
                         UNIQUE KEY uq_usuario_matricula (matricula)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='Tabla base de todos los actores del sistema';

CREATE TABLE usuario_rol (
                             id_usuario  INT  NOT NULL,
                             rol         ENUM('Administrador','Coordinador','Profesor','Practicante')
                                              NOT NULL,
                             estado       ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo' ,
                             PRIMARY KEY (id_usuario, rol),
                             CONSTRAINT fk_urol_usuario
                                 FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
                                     ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='Un usuario puede tener más de un rol (ej. Coordinador + Profesor)';

CREATE TABLE administrador (
                               id_usuario  INT NOT NULL,
                               PRIMARY KEY (id_usuario),
                               CONSTRAINT fk_adm_usuario
                                   FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
                                       ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-01 registra; sin atributos extra';


CREATE TABLE coordinador (
                             id_usuario  INT NOT NULL,
                             PRIMARY KEY (id_usuario),
                             CONSTRAINT fk_coord_usuario
                                 FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
                                     ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-01 registra; CU-03 inactiva (via usuario.estado)';


CREATE TABLE profesor (
                          id_usuario  INT          NOT NULL,
                          academica   VARCHAR(100) NOT NULL COMMENT 'Área académica / academia',
                          PRIMARY KEY (id_usuario),
                          CONSTRAINT fk_prof_usuario
                              FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
                                  ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-02 registra; CU-04 inactiva; CU-15 consulta';


CREATE TABLE practicante (
                             id_usuario  INT NOT NULL,
                             creditos    INT NOT NULL COMMENT 'Créditos académicos acumulados',
                             PRIMARY KEY (id_usuario),
                             CONSTRAINT fk_prac_usuario
                                 FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
                                     ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-14 registra; CU-13 inactiva; CU-19/20/21/22/23/24 operan';



CREATE TABLE organizacion_vinculada (
                                        id_organizacion      INT          NOT NULL AUTO_INCREMENT,
                                        nombre_organizacion  VARCHAR(150) NOT NULL,
                                        correo_organizacion  VARCHAR(120) NOT NULL,
                                        direccion            VARCHAR(255) NOT NULL,
                                        sector               VARCHAR(100) NOT NULL COMMENT 'sectorOrganizacion (CU-05)',
                                        estado               ENUM('Activa','Inactiva') NOT NULL DEFAULT 'Activa',
                                        PRIMARY KEY (id_organizacion),
                                        UNIQUE KEY uq_org_nombre (nombre_organizacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-05 registra; CU-06 consulta; CU-09 referencia';



CREATE TABLE tecnico_responsable (
                                     id_tecnico            INT          NOT NULL AUTO_INCREMENT,
                                     id_organizacion       INT          NOT NULL,
                                     nombre                VARCHAR(80)  NOT NULL,
                                     apellido_paterno      VARCHAR(60)  NOT NULL,
                                     apellido_materno      VARCHAR(60)  NOT NULL,
                                     correo_responsable    VARCHAR(120) NOT NULL,
                                     cargo                 VARCHAR(100) NOT NULL,
                                     PRIMARY KEY (id_tecnico),
                                     UNIQUE KEY uq_tec_correo_org (correo_responsable, id_organizacion),
                                     CONSTRAINT fk_tec_org
                                         FOREIGN KEY (id_organizacion)
                                             REFERENCES organizacion_vinculada (id_organizacion)
                                             ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-07 registra; CU-08 consulta; CU-09 referencia';


CREATE TABLE proyecto (
                          id_proyecto      INT          NOT NULL AUTO_INCREMENT,
                          id_organizacion  INT          NOT NULL,
                          id_tecnico       INT          NOT NULL,
                          id_profesor   INT          NOT NULL COMMENT 'FK → coordinador.id_usuario',
                          nombre           VARCHAR(150) NOT NULL,
                          descripcion      TEXT         NOT NULL,
                          fecha_inicio     DATE         NOT NULL,
                          fecha_fin        DATE         NOT NULL,
                          cupo_maximo      INT          NOT NULL,
                          cupo_disponible  INT          NOT NULL,
                          estado           ENUM('Disponible','Lleno','Concluido','Cancelado')
                                                        NOT NULL DEFAULT 'Disponible',
                          PRIMARY KEY (id_proyecto),
                          UNIQUE KEY uq_proy_nombre_org (nombre, id_organizacion),
                          CONSTRAINT fk_proy_org
                              FOREIGN KEY (id_organizacion)
                                  REFERENCES organizacion_vinculada (id_organizacion)
                                  ON UPDATE CASCADE ON DELETE RESTRICT,
                          CONSTRAINT fk_proy_tec
                              FOREIGN KEY (id_tecnico)
                                  REFERENCES tecnico_responsable (id_tecnico)
                                  ON UPDATE CASCADE ON DELETE RESTRICT,
                          CONSTRAINT fk_proy_coord
                              FOREIGN KEY (id_profesor)
                                  REFERENCES profesor (id_usuario)
                                  ON UPDATE CASCADE ON DELETE RESTRICT,
                          CONSTRAINT chk_fechas
                              CHECK (fecha_fin > fecha_inicio),
                          CONSTRAINT chk_cupo_maximo
                              CHECK (cupo_maximo > 0),
                          CONSTRAINT chk_cupo_disponible
                              CHECK (cupo_disponible >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-09 registra; CU-10 asigna; CU-11 elimina; CU-12 actualiza';

CREATE TABLE solicitud (
                           id_solicitud     INT      NOT NULL AUTO_INCREMENT,
                           id_practicante   INT      NOT NULL,
                           estado           ENUM('Pendiente','Aceptada','Rechazada')
                                                     NOT NULL DEFAULT 'Pendiente',
                           fecha_solicitud  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (id_solicitud),
                           UNIQUE KEY uq_sol_practicante_activa (id_practicante, estado),
                           CONSTRAINT fk_sol_prac
                               FOREIGN KEY (id_practicante)
                                   REFERENCES practicante (id_usuario)
                                   ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-19 registra; CU-10 acepta';


CREATE TABLE solicitud_proyecto (
                                    id_solicitud_proyecto  INT     NOT NULL AUTO_INCREMENT,
                                    id_solicitud           INT     NOT NULL,
                                    id_proyecto            INT     NOT NULL,
                                    orden_preferencia      TINYINT COMMENT '1=primera, 2=segunda, 3=tercera',
                                    PRIMARY KEY (id_solicitud_proyecto),
                                    UNIQUE KEY uq_sol_proy       (id_solicitud, id_proyecto),
                                    UNIQUE KEY uq_sol_orden      (id_solicitud, orden_preferencia),
                                    CONSTRAINT fk_solproy_sol
                                        FOREIGN KEY (id_solicitud)
                                            REFERENCES solicitud (id_solicitud)
                                            ON UPDATE CASCADE ON DELETE CASCADE,
                                    CONSTRAINT fk_solproy_proy
                                        FOREIGN KEY (id_proyecto)
                                            REFERENCES proyecto (id_proyecto)
                                            ON UPDATE CASCADE ON DELETE RESTRICT,
                                    CONSTRAINT chk_orden_preferencia
                                        CHECK (orden_preferencia BETWEEN 1 AND 3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-19: hasta 3 opciones por solicitud';

CREATE TABLE asignacion (
                            id_asignacion     INT      NOT NULL AUTO_INCREMENT,
                            id_practicante    INT      NOT NULL,
                            id_proyecto       INT      NOT NULL,
                            id_solicitud      INT      NOT NULL,
                            fecha_asignacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            estado            ENUM('Activa','Concluida'),
                            PRIMARY KEY (id_asignacion),
                            UNIQUE KEY uq_asig_practicante (id_practicante),
                            CONSTRAINT fk_asig_prac
                                FOREIGN KEY (id_practicante)
                                    REFERENCES practicante (id_usuario)
                                    ON UPDATE CASCADE ON DELETE RESTRICT,
                            CONSTRAINT fk_asig_proy
                                FOREIGN KEY (id_proyecto)
                                    REFERENCES proyecto (id_proyecto)
                                    ON UPDATE CASCADE ON DELETE RESTRICT,
                            CONSTRAINT fk_asig_sol
                                FOREIGN KEY (id_solicitud)
                                    REFERENCES solicitud (id_solicitud)
                                    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-10 registra; un practicante solo puede tener una asignación';

CREATE TABLE formato_inicial (
                                 id_formato        INT          NOT NULL AUTO_INCREMENT,
                                 id_practicante    INT          NOT NULL,
                                 tipo_formato      ENUM(
                                     'Carta de Asignación',
                                     'Horario',
                                     'Certificado de Seguro',
                                     'Cronograma de Actividades'
                                     ) NOT NULL,
                                 ruta_archivo      VARCHAR(500) NOT NULL COMMENT 'Ruta del archivo en disco',
                                 estado            ENUM('Pendiente','Entregado') NOT NULL DEFAULT 'Pendiente',
                                 fecha_entrega     DATETIME     NULL,
                                 PRIMARY KEY (id_formato),
                                 UNIQUE KEY uq_fmt_prac_tipo (id_practicante, tipo_formato),
                                 CONSTRAINT fk_fmt_prac
                                     FOREIGN KEY (id_practicante)
                                         REFERENCES practicante (id_usuario)
                                         ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-18: practicante sube los 4 formatos iniciales';



CREATE TABLE reporte (
                         id_reporte          INT           NOT NULL AUTO_INCREMENT,
                         id_practicante      INT           NOT NULL,
                         id_proyecto         INT           NOT NULL,
                         id_profesor         INT           NULL  COMMENT 'Se asigna al evaluar (CU-17)',
                         tipo_reporte        ENUM('Parcial','Final','Mensual') NOT NULL,
                         periodo             VARCHAR(50)   NOT NULL COMMENT 'Ej: 2024-01 o número de informe',
                         ruta_documento      VARCHAR(500)  NOT NULL COMMENT 'Ruta del PDF firmado (CU-21)',
                         estado              ENUM('Pendiente','En revisión','Evaluado')
                             NOT NULL DEFAULT 'Pendiente',
                         fecha_entrega       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         PRIMARY KEY (id_reporte),
                         UNIQUE KEY uq_rep_prac_tipo_periodo (id_practicante, tipo_reporte, periodo),
                         CONSTRAINT fk_rep_prac
                             FOREIGN KEY (id_practicante)
                                 REFERENCES practicante (id_usuario)
                                 ON UPDATE CASCADE ON DELETE RESTRICT,
                         CONSTRAINT fk_rep_proy
                             FOREIGN KEY (id_proyecto)
                                 REFERENCES proyecto (id_proyecto)
                                 ON UPDATE CASCADE ON DELETE RESTRICT,
                         CONSTRAINT fk_rep_prof
                             FOREIGN KEY (id_profesor)
                                 REFERENCES profesor (id_usuario)
                                 ON UPDATE CASCADE ON DELETE SET NULL

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-20 genera; CU-21 sube firmado; CU-17 evalúa';

CREATE TABLE reporte_parcial_y_final(
                                        id_reporte_parcial INT NOT NULL,
                                        numero_informe      TINYINT       NULL COMMENT 'Solo Parcial: nº de informe',
                                        horas_cubiertas     DECIMAL(6,2)  NULL COMMENT 'Parcial/Final: horas al momento',
                                        objetivo_general    TEXT          NULL COMMENT 'Parcial/Final',
                                        metodologia         TEXT          NULL COMMENT 'Solo Parcial',
                                        resultados_obtenidos TEXT         NULL COMMENT 'Solo Parcial',
                                        observaciones       TEXT          NULL COMMENT 'Todos los tipos',

                                        foreign key (id_reporte_parcial) references reporte (id_reporte)
                                            on update cascade on delete cascade
);

Create table reporte_mensual(
                                id_reporte_mensual int not null ,
                                mes                 TINYINT       NULL COMMENT 'Solo Mensual: 1-12',
                                anio                YEAR          NULL COMMENT 'Solo Mensual',
                                horas_reportadas    DECIMAL(6,2)  NULL COMMENT 'Solo Mensual: horas del mes',
                                bloque              VARCHAR(100)  NULL COMMENT 'Solo Mensual: bloque de la EE',
                                seccion             VARCHAR(50)   NULL COMMENT 'Solo Mensual: sección del grupo',

                                foreign key (id_reporte_mensual) references reporte (id_reporte)
                                    on update cascade on delete cascade,
                                CONSTRAINT chk_rep_mes
                                    CHECK (mes IS NULL OR mes BETWEEN 1 AND 12)

);

CREATE TABLE evaluacion_reporte(

                                   id_evaluacion_reporte int not null,
                                   id_reporte int not null ,
                                   calificacion        DECIMAL(4,2)  NULL COMMENT '0.00 – 10.00',
                                   retroalimentacion   TEXT          NULL,
                                   porcentaje_avance   DECIMAL(5,2)  NULL COMMENT '0.00 – 100.00',
                                   fecha_evaluacion    DATETIME      NULL,
                                   foreign key (id_reporte) references reporte (id_reporte),
                                   CONSTRAINT chk_rep_calificacion
                                       CHECK (calificacion IS NULL OR calificacion BETWEEN 0 AND 10),
                                   CONSTRAINT chk_rep_avance
                                       CHECK (porcentaje_avance IS NULL OR porcentaje_avance BETWEEN 0 AND 100)



);

CREATE TABLE autoevaluacion (
                                id_autoevaluacion  INT          NOT NULL AUTO_INCREMENT,
                                id_practicante     INT          NOT NULL,
                                id_proyecto        INT          NOT NULL,
                                periodo            VARCHAR(50)  NOT NULL,
                                afirmacion_01  TINYINT  NULL COMMENT '1=Tot. desacuerdo…5=Tot. acuerdo',
                                afirmacion_02  TINYINT  NULL,
                                afirmacion_03  TINYINT  NULL,
                                afirmacion_04  TINYINT  NULL,
                                afirmacion_05  TINYINT  NULL,
                                afirmacion_06  TINYINT  NULL,
                                afirmacion_07  TINYINT  NULL,
                                afirmacion_08  TINYINT  NULL,
                                afirmacion_09  TINYINT  NULL,
                                afirmacion_10  TINYINT  NULL,
                                puntuacion_final TINYINT NULL COMMENT 'Suma afirmaciones (10-50)',
                                lugar_fecha    VARCHAR(200) NULL COMMENT 'Campo lugarYFecha (CU-22)',
                                ruta_documento VARCHAR(500) NOT NULL COMMENT 'Ruta del PDF firmado (CU-23)',
                                estado         ENUM('Pendiente','Revisada') NOT NULL DEFAULT 'Pendiente',
                                fecha_entrega  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                PRIMARY KEY (id_autoevaluacion),
                                UNIQUE KEY uq_autoev_prac_periodo (id_practicante, periodo),
                                CONSTRAINT fk_autoev_prac
                                    FOREIGN KEY (id_practicante)
                                        REFERENCES practicante (id_usuario)
                                        ON UPDATE CASCADE ON DELETE RESTRICT,
                                CONSTRAINT fk_autoev_proy
                                    FOREIGN KEY (id_proyecto)
                                        REFERENCES proyecto (id_proyecto)
                                        ON UPDATE CASCADE ON DELETE RESTRICT,
                                CONSTRAINT chk_af01 CHECK (afirmacion_01 IS NULL OR afirmacion_01 BETWEEN 1 AND 5),
                                CONSTRAINT chk_af02 CHECK (afirmacion_02 IS NULL OR afirmacion_02 BETWEEN 1 AND 5),
                                CONSTRAINT chk_af03 CHECK (afirmacion_03 IS NULL OR afirmacion_03 BETWEEN 1 AND 5),
                                CONSTRAINT chk_af04 CHECK (afirmacion_04 IS NULL OR afirmacion_04 BETWEEN 1 AND 5),
                                CONSTRAINT chk_af05 CHECK (afirmacion_05 IS NULL OR afirmacion_05 BETWEEN 1 AND 5),
                                CONSTRAINT chk_af06 CHECK (afirmacion_06 IS NULL OR afirmacion_06 BETWEEN 1 AND 5),
                                CONSTRAINT chk_af07 CHECK (afirmacion_07 IS NULL OR afirmacion_07 BETWEEN 1 AND 5),
                                CONSTRAINT chk_af08 CHECK (afirmacion_08 IS NULL OR afirmacion_08 BETWEEN 1 AND 5),
                                CONSTRAINT chk_af09 CHECK (afirmacion_09 IS NULL OR afirmacion_09 BETWEEN 1 AND 5),
                                CONSTRAINT chk_af10 CHECK (afirmacion_10 IS NULL OR afirmacion_10 BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-22 genera y guarda datos; CU-23 sube PDF firmado';


CREATE TABLE evaluacion_ov (
                               id_evaluacion_ov  INT          NOT NULL AUTO_INCREMENT,
                               id_practicante    INT          NOT NULL,
                               id_proyecto       INT          NOT NULL,
                               ruta_documento    VARCHAR(500) NOT NULL COMMENT 'Ruta del archivo en disco',
                               estado            ENUM('Pendiente','Entregado') NOT NULL DEFAULT 'Pendiente',
                               fecha_entrega     DATETIME     NULL,
                               PRIMARY KEY (id_evaluacion_ov),
                               UNIQUE KEY uq_eov_prac_proy (id_practicante, id_proyecto),
                               CONSTRAINT fk_eov_prac
                                   FOREIGN KEY (id_practicante)
                                       REFERENCES practicante (id_usuario)
                                       ON UPDATE CASCADE ON DELETE RESTRICT,
                               CONSTRAINT fk_eov_proy
                                   FOREIGN KEY (id_proyecto)
                                       REFERENCES proyecto (id_proyecto)
                                       ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    COMMENT='CU-24: practicante sube evaluación firmada de la organización';