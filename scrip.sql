create table organizacion_vinculada
(
    id_organizacion     int auto_increment
        primary key,
    nombre_organizacion varchar(150)                                 not null,
    correo_organizacion varchar(120)                                 not null,
    direccion           varchar(255)                                 not null,
    sector              varchar(100)                                 not null comment 'sectorOrganizacion (CU-05)',
    estado              enum ('Activa', 'Inactiva') default 'Activa' not null,
    constraint uq_org_nombre
        unique (nombre_organizacion)
)
    comment 'CU-05 registra; CU-06 consulta; CU-09 referencia';

create table tecnico_responsable
(
    id_tecnico         int auto_increment
        primary key,
    id_organizacion    int          not null,
    nombre             varchar(80)  not null,
    apellido_paterno   varchar(60)  not null,
    apellido_materno   varchar(60)  not null,
    correo_responsable varchar(120) not null,
    cargo              varchar(100) not null,
    constraint uq_tec_correo
        unique (correo_responsable),
    constraint fk_tecnico_organizacion
        foreign key (id_organizacion) references organizacion_vinculada (id_organizacion)
            on delete cascade
)
    comment 'CU-07 registra; CU-08 consulta; CU-09 referencia';

create table usuario
(
    id_usuario       int auto_increment
        primary key,
    matricula        varchar(20)  not null,
    nombre           varchar(80)  not null,
    apellido_paterno varchar(60)  not null,
    apellido_materno varchar(60)  not null,
    contrasenia      varchar(255) not null comment 'Hash bcrypt',
    correo           varchar(120) not null,
    constraint uq_usuario_matricula
        unique (matricula),
    constraint uq_usuario_correo
        unique (correo)
)
    comment 'Tabla base de todos los actores del sistema';

create table administrador
(
    id_usuario int not null
        primary key,
    constraint fk_adm_usuario
        foreign key (id_usuario) references usuario (id_usuario)
            on update cascade on delete cascade
)
    comment 'CU-01 registra; sin atributos extra';

create table coordinador
(
    id_usuario int not null
        primary key,
    constraint fk_coord_usuario
        foreign key (id_usuario) references usuario (id_usuario)
            on update cascade on delete cascade
)
    comment 'CU-01 registra; CU-03 inactiva (via usuario.estado)';

create table experiencia_educativa
(
    nrc         varchar(10)  not null,
    nombre      varchar(150) not null,
    id_profesor int          not null,
    periodo     varchar(20)  not null,
    primary key (nrc, periodo),
    constraint fk_ee_profesor
        foreign key (id_profesor) references usuario (id_usuario)
            on update cascade
);

create table practicante
(
    id_usuario int not null
        primary key,
    creditos   int not null comment 'Créditos académicos acumulados',
    constraint fk_prac_usuario
        foreign key (id_usuario) references usuario (id_usuario)
            on update cascade on delete cascade
)
    comment 'CU-14 registra; CU-13 inactiva; CU-19/20/21/22/23/24 operan';

create table practica
(
    id_practica    int auto_increment
        primary key,
    nrc            varchar(10)                                                not null,
    periodo        varchar(20)                                                not null,
    id_practicante int                                                        not null,
    fecha_inicio   date                                                       not null,
    fecha_fin      date                                                       null,
    estado         enum ('Activa', 'Concluida', 'Cancelada') default 'Activa' not null,
    calificacion   decimal(4, 2)                                              null,
    ruta_acta_cierre varchar(500)                                             null comment 'Ruta del PDF del acta de cierre emitida externamente; su carga concluye la practica',
    constraint uq_practica_nrc_periodo_practicante
        unique (nrc, periodo, id_practicante),
    constraint fk_practica_ee
        foreign key (nrc, periodo) references experiencia_educativa (nrc, periodo)
            on update cascade,
    constraint fk_practica_practicante
        foreign key (id_practicante) references practicante (id_usuario)
            on update cascade,
    constraint chk_practica_calificacion
        check ((`calificacion` is null) or (`calificacion` between 0 and 10)),
    constraint chk_practica_fechas
        check ((`fecha_fin` is null) or (`fecha_fin` >= `fecha_inicio`))
);

create table profesor
(
    id_usuario int          not null
        primary key,
    academica  varchar(100) not null comment 'Área académica / academia',
    constraint fk_prof_usuario
        foreign key (id_usuario) references usuario (id_usuario)
            on update cascade on delete cascade
)
    comment 'CU-02 registra; CU-04 inactiva; CU-15 consulta';

create table proyecto
(
    id_proyecto     int auto_increment
        primary key,
    id_organizacion int                                                                         not null,
    id_tecnico      int                                                                         not null,
    nombre          varchar(150)                                                                not null,
    descripcion     text                                                                        not null,
    objetivo        text                                                                        null,
    fecha_inicio    date                                                                        not null,
    fecha_fin       date                                                                        not null,
    cupo_maximo     int                                                                         not null,
    cupo_disponible int                                                                         not null,
    estado          enum ('Disponible', 'Lleno', 'Concluido', 'Cancelado') default 'Disponible' not null,
    id_profesor     int                                                                         null,
    nrc             varchar(10)                                                                 null,
    periodo         varchar(20)                                                                 null,
    constraint uq_proy_nombre_org
        unique (nombre, id_organizacion),
    constraint fk_proy_ee
        foreign key (nrc, periodo) references experiencia_educativa (nrc, periodo)
            on update cascade,
    constraint fk_proy_org
        foreign key (id_organizacion) references organizacion_vinculada (id_organizacion)
            on update cascade,
    constraint fk_proy_tec
        foreign key (id_tecnico) references tecnico_responsable (id_tecnico)
            on update cascade,
    constraint id_profesor
        foreign key (id_profesor) references usuario (id_usuario),
    constraint chk_cupo_disponible
        check (`cupo_disponible` >= 0),
    constraint chk_cupo_maximo
        check (`cupo_maximo` > 0),
    constraint chk_fechas
        check (`fecha_fin` > `fecha_inicio`)
)
    comment 'CU-09 registra; CU-10 asigna; CU-11 elimina; CU-12 actualiza';

create table actividad
(
    id_actividad       int auto_increment
        primary key,
    id_proyecto        int                                             not null,
    id_practicante     int                                             null,
    nombre             varchar(200)                                    not null,
    descripcion        text                                            null,
    semana_inicio_plan int                         default 1           not null,
    semana_fin_plan    int                         default 8           not null,
    fecha_creacion     date                        default (curdate()) not null,
    estado             enum ('Activa', 'Inactiva') default 'Activa'    not null,
    fecha_inicio       date                                            null,
    fecha_fin          date                                            null,
    constraint fk_actividad_proyecto
        foreign key (id_proyecto) references proyecto (id_proyecto)
            on update cascade on delete cascade,
    constraint fk_actividad_practicante
        foreign key (id_practicante) references practicante (id_usuario)
            on update cascade on delete cascade
);

create table actividad_practicante
(
    id_actividad_practicante int auto_increment
        primary key,
    id_actividad             int                                                                 not null,
    id_practicante           int                                                                 not null,
    horas_dedicadas          int                                             default 0           not null,
    estado                   enum ('Pendiente', 'En Progreso', 'Completada') default 'Pendiente' not null,
    fecha_realizacion        date                                                                null,
    observaciones            varchar(500)                                                        null,
    constraint uq_actividad_practicante
        unique (id_actividad, id_practicante),
    constraint fk_actprac_actividad
        foreign key (id_actividad) references actividad (id_actividad)
            on update cascade on delete cascade,
    constraint fk_actprac_practicante
        foreign key (id_practicante) references practicante (id_usuario)
            on update cascade on delete cascade
);

create table autoevaluacion
(
    id_autoevaluacion int auto_increment
        primary key,
    id_practicante    int                                                      not null,
    id_proyecto       int                                                      not null,
    periodo           varchar(50)                                              not null,
    afirmacion_01     tinyint                                                  null comment '1=Tot. desacuerdo…5=Tot. acuerdo',
    afirmacion_02     tinyint                                                  null,
    afirmacion_03     tinyint                                                  null,
    afirmacion_04     tinyint                                                  null,
    afirmacion_05     tinyint                                                  null,
    afirmacion_06     tinyint                                                  null,
    afirmacion_07     tinyint                                                  null,
    afirmacion_08     tinyint                                                  null,
    afirmacion_09     tinyint                                                  null,
    afirmacion_10     tinyint                                                  null,
    puntuacion_final  tinyint                                                  null comment 'Suma afirmaciones (10-50)',
    lugar_fecha       varchar(200)                                             null comment 'Campo lugarYFecha (CU-22)',
    ruta_documento    varchar(500)                                             not null comment 'Ruta del PDF firmado (CU-23)',
    estado            enum ('Pendiente', 'Entregada') default 'Pendiente'       not null,
    fecha_entrega     datetime                       default CURRENT_TIMESTAMP not null,
    constraint uq_autoev_prac_periodo
        unique (id_practicante, periodo),
    constraint fk_autoev_prac
        foreign key (id_practicante) references practicante (id_usuario)
            on update cascade,
    constraint fk_autoev_proy
        foreign key (id_proyecto) references proyecto (id_proyecto)
            on update cascade,
    constraint chk_af01
        check ((`afirmacion_01` is null) or (`afirmacion_01` between 1 and 5)),
    constraint chk_af02
        check ((`afirmacion_02` is null) or (`afirmacion_02` between 1 and 5)),
    constraint chk_af03
        check ((`afirmacion_03` is null) or (`afirmacion_03` between 1 and 5)),
    constraint chk_af04
        check ((`afirmacion_04` is null) or (`afirmacion_04` between 1 and 5)),
    constraint chk_af05
        check ((`afirmacion_05` is null) or (`afirmacion_05` between 1 and 5)),
    constraint chk_af06
        check ((`afirmacion_06` is null) or (`afirmacion_06` between 1 and 5)),
    constraint chk_af07
        check ((`afirmacion_07` is null) or (`afirmacion_07` between 1 and 5)),
    constraint chk_af08
        check ((`afirmacion_08` is null) or (`afirmacion_08` between 1 and 5)),
    constraint chk_af09
        check ((`afirmacion_09` is null) or (`afirmacion_09` between 1 and 5)),
    constraint chk_af10
        check ((`afirmacion_10` is null) or (`afirmacion_10` between 1 and 5))
)
    comment 'CU-22 genera y guarda datos; CU-23 sube PDF firmado';

create table evaluacion_ov
(
    id_evaluacion_ov int auto_increment
        primary key,
    id_practicante   int                                                 not null,
    id_proyecto      int                                                 not null,
    ruta_documento   varchar(500)                                        not null comment 'Ruta del archivo en disco',
    estado           enum ('Pendiente', 'Entregada') default 'Pendiente' not null,
    fecha_entrega    datetime                                            null,
    constraint uq_eov_prac_proy
        unique (id_practicante, id_proyecto),
    constraint fk_eov_prac
        foreign key (id_practicante) references practicante (id_usuario)
            on update cascade,
    constraint fk_eov_proy
        foreign key (id_proyecto) references proyecto (id_proyecto)
            on update cascade
)
    comment 'CU-24: practicante sube evaluación firmada de la organización';

create table formato_inicial
(
    id_formato     int auto_increment
        primary key,
    id_practicante int                                                                                           not null,
    tipo_formato   enum ('Carta de Asignación', 'Horario', 'Certificado de Seguro', 'Cronograma de Actividades') null,
    ruta_archivo   varchar(500)                                                                                  null comment 'Ruta del archivo en disco',
    estado         enum ('Pendiente', 'Entregado') default 'Pendiente'                                           not null,
    fecha_entrega  datetime                                                                                      null,
    id_proyecto    int                                                                                           null,
    constraint uq_fmt_prac_proy_tipo
        unique (id_practicante, id_proyecto, tipo_formato),
    constraint fk_fmt_prac
        foreign key (id_practicante) references practicante (id_usuario)
            on update cascade,
    constraint foreign_key_Id_proyecto
        foreign key (id_proyecto) references proyecto (id_proyecto)
            on update cascade on delete set null
)
    comment 'CU-18: practicante sube los 4 formatos iniciales';

create index idx_fmt_practicante
    on formato_inicial (id_practicante);

create table reporte
(
    id_reporte             int auto_increment
        primary key,
    id_practicante         int                                                                     not null,
    id_proyecto            int                                                                     not null,
    id_profesor            int                                                                     null comment 'Se asigna al evaluar (CU-17)',
    tipo_reporte           enum ('Parcial', 'Final', 'Mensual')                                    not null,
    periodo                varchar(50)                                                             not null comment 'Ej: 2024-01 o número de informe',
    ruta_documento         varchar(500)                                                            not null comment 'Ruta del PDF firmado (CU-21)',
    estado                 enum ('Pendiente', 'En revision', 'Evaluado') default 'Pendiente'       not null,
    horas_reportadas       int                                           default 0                 null,
    observaciones_profesor text                                                                    null,
    fecha_revision         date                                                                    null,
    calificacion           decimal(4, 2)                                                           null comment '0.00 - 10.00',
    fecha_evaluacion       datetime                                                                null,
    fecha_entrega          datetime                                      default CURRENT_TIMESTAMP not null,
    ruta_documento_firmado varchar(150)                                                            null,
    fecha_limite           date                                                                    null,
    entrega_tardia         tinyint(1)                                                              null,
    constraint fk_rep_prac
        foreign key (id_practicante) references practicante (id_usuario)
            on update cascade,
    constraint fk_rep_prof
        foreign key (id_profesor) references profesor (id_usuario)
            on update cascade on delete set null,
    constraint fk_rep_proy
        foreign key (id_proyecto) references proyecto (id_proyecto)
            on update cascade
)
    comment 'CU-20 genera; CU-21 sube firmado; CU-17 evalúa';

create table observacion_reporte
(
    id_observacion    int auto_increment
        primary key,
    id_reporte        int                                not null,
    id_profesor       int                                not null,
    comentario        text                               not null,
    fecha_observacion datetime default CURRENT_TIMESTAMP not null,
    constraint fk_obs_profesor
        foreign key (id_profesor) references profesor (id_usuario)
            on update cascade,
    constraint fk_obs_reporte
        foreign key (id_reporte) references reporte (id_reporte)
            on update cascade on delete cascade
);

create index idx_rep_practicante
    on reporte (id_practicante);

create table reporte_actividad
(
    id_reporte_actividad int auto_increment
        primary key,
    id_reporte           int           not null,
    id_actividad         int           not null,
    periodo              varchar(100)  null,
    plan_semanas         varchar(20)   null,
    real_semanas         varchar(20)   null,
    porcentaje_avance    int default 0 not null,
    observaciones        varchar(500)  null,
    constraint fk_ra_actividad
        foreign key (id_actividad) references actividad (id_actividad)
            on update cascade on delete cascade,
    constraint fk_ra_reporte
        foreign key (id_reporte) references reporte (id_reporte)
            on update cascade on delete cascade
);

create index idx_reporte_actividad_reporte
    on reporte_actividad (id_reporte);

create table reporte_entregable
(
    id_reporte_entregable int auto_increment
        primary key,
    id_reporte            int           not null,
    resultado             varchar(300)  not null,
    descripcion           varchar(500)  null,
    porcentaje_avance     int default 0 not null,
    observaciones         varchar(500)  null,
    constraint fk_re_reporte
        foreign key (id_reporte) references reporte (id_reporte)
            on update cascade on delete cascade
);

create index idx_reporte_entregable_reporte
    on reporte_entregable (id_reporte);

create table reporte_mensual
(
    id_reporte_mensual int           not null,
    mes                varchar(30)   null comment 'Solo Mensual: 1-12',
    anio               year          null comment 'Solo Mensual',
    horas_reportadas   decimal(6, 2) null comment 'Solo Mensual: horas del mes',
    bloque             varchar(100)  null comment 'Solo Mensual: bloque de la EE',
    seccion            varchar(50)   null comment 'Solo Mensual: sección del grupo',
    constraint reporte_mensual_ibfk_1
        foreign key (id_reporte_mensual) references reporte (id_reporte)
            on update cascade on delete cascade
);

create index id_reporte_mensual
    on reporte_mensual (id_reporte_mensual);

create table reporte_parcial_y_final
(
    id_reporte_parcial   int           not null,
    numero_informe       tinyint       null comment 'Solo Parcial: nº de informe',
    horas_cubiertas      decimal(6, 2) null comment 'Parcial/Final: horas al momento',
    objetivo_general     text          null comment 'Parcial/Final',
    metodologia          text          null comment 'Solo Parcial',
    resultados_obtenidos text          null comment 'Solo Parcial',
    observaciones        text          null comment 'Todos los tipos',
    constraint reporte_parcial_y_final_ibfk_1
        foreign key (id_reporte_parcial) references reporte (id_reporte)
            on update cascade on delete cascade
);

create index id_reporte_parcial
    on reporte_parcial_y_final (id_reporte_parcial);

create table solicitud
(
    id_solicitud    int auto_increment
        primary key,
    id_practicante  int                                                                                not null,
    estado          enum ('Pendiente', 'Cancelada', 'Aceptada', 'Rechazada') default 'Pendiente'       not null,
    fecha_solicitud datetime                                                 default CURRENT_TIMESTAMP not null,
    constraint fk_sol_prac
        foreign key (id_practicante) references practicante (id_usuario)
            on update cascade
)
    comment 'CU-19 registra; CU-10 acepta';

create table asignacion
(
    id_asignacion    int auto_increment
        primary key,
    id_practicante   int                                                                 not null,
    id_proyecto      int                                                                 not null,
    id_solicitud     int                                                                 not null,
    fecha_asignacion datetime                                  default CURRENT_TIMESTAMP not null,
    estado           enum ('Activa', 'Concluida', 'Cancelada') default 'Activa'          not null,
    razon_asignacion text                                                                null,
    constraint uq_asig_practicante
        unique (id_practicante),
    constraint fk_asig_prac
        foreign key (id_practicante) references practicante (id_usuario)
            on update cascade,
    constraint fk_asig_proy
        foreign key (id_proyecto) references proyecto (id_proyecto)
            on update cascade,
    constraint fk_asig_sol
        foreign key (id_solicitud) references solicitud (id_solicitud)
            on update cascade
)
    comment 'CU-10 registra; un practicante solo puede tener una asignación';

create index idx_sol_practicante
    on solicitud (id_practicante);

create table solicitud_proyecto
(
    id_solicitud_proyecto int auto_increment
        primary key,
    id_solicitud          int     not null,
    id_proyecto           int     not null,
    orden_preferencia     tinyint null comment '1=primera, 2=segunda, 3=tercera',
    constraint fk_solproy_proy
        foreign key (id_proyecto) references proyecto (id_proyecto)
            on update cascade,
    constraint fk_solproy_sol
        foreign key (id_solicitud) references solicitud (id_solicitud)
            on update cascade on delete cascade
)
    comment 'CU-19: hasta 3 opciones por solicitud';

create index idx_solproy_proyecto
    on solicitud_proyecto (id_proyecto);

create index idx_solproy_solicitud
    on solicitud_proyecto (id_solicitud);

create table usuario_rol
(
    id_usuario int                                                              not null,
    rol        enum ('Administrador', 'Coordinador', 'Profesor', 'Practicante') not null,
    estado     enum ('Activo', 'Inactivo') default 'Activo'                     not null,
    primary key (id_usuario, rol),
    constraint fk_urol_usuario
        foreign key (id_usuario) references usuario (id_usuario)
            on update cascade on delete cascade
)
    comment 'Un usuario puede tener más de un rol (ej. Coordinador + Profesor)';

create definer = root@localhost trigger trg_single_active_coordinator
    before insert
    on usuario_rol
    for each row
begin
    declare active_count int;
    if new.rol = 'Coordinador' and new.estado = 'Activo' then
        select count(*) into active_count
        from usuario_rol
        where rol = 'Coordinador' and estado = 'Activo';
        if active_count >= 1 then
            signal sqlstate '45000'
                set message_text = 'Solo puede existir un coordinador activo en el sistema.';
        end if;
    end if;
end;

create definer = root@localhost trigger trg_single_active_coordinator_upd
    before update
    on usuario_rol
    for each row
begin
    declare active_count int;
    if new.rol = 'Coordinador' and new.estado = 'Activo' and old.estado <> 'Activo' then
        select count(*) into active_count
        from usuario_rol
        where rol = 'Coordinador' and estado = 'Activo';
        if active_count >= 1 then
            signal sqlstate '45000'
                set message_text = 'Solo puede existir un coordinador activo en el sistema.';
        end if;
    end if;
end;

