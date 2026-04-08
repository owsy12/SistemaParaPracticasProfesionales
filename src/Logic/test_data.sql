-- ============================================================
--  DATOS DE PRUEBA — spp
--  Insertar antes de ejecutar los tests de inserción.
--  Cubren todas las FK que los DAOs necesitan.
-- ============================================================
USE spp;

-- 1. Usuarios base (practicante, coordinador, profesor)
INSERT INTO usuario (id_usuario, matricula, nombre, apellido_paterno, apellido_materno, contrasenia, estado)
VALUES
    (1, 'S21013142', 'Ana',    'García',  'López',    '$2b$10$hash1', 'Activo'),  -- practicante
    (2, 'P20001001', 'Carlos', 'Ramírez', 'Soto',     '$2b$10$hash2', 'Activo'),  -- coordinador
    (3, 'P20002002', 'Elena',  'Torres',  'Mendoza',  '$2b$10$hash3', 'Activo');  -- profesor

-- 2. Roles
INSERT INTO usuario_rol (id_usuario, rol) VALUES
    (1, 'Practicante'),
    (2, 'Coordinador'),
    (3, 'Profesor');

-- 3. Tablas hija
INSERT INTO practicante (id_usuario, creditos)  VALUES (1, 200);
INSERT INTO coordinador (id_usuario)             VALUES (2);
INSERT INTO profesor    (id_usuario, academica)  VALUES (3, 'Ingeniería de Software');

-- 4. Organización y técnico
INSERT INTO organizacion_vinculada (id_organizacion, nombre_organizacion, correo_organizacion, direccion, sector)
VALUES (1, 'TechCorp SA', 'contacto@techcorp.mx', 'Av. Central 100, Xalapa', 'Tecnología');

INSERT INTO tecnico_responsable (id_tecnico, id_organizacion, nombre, apellido_paterno, apellido_materno, correo_responsable, cargo)
VALUES (1, 1, 'Luis', 'Pérez', 'Vega', 'luis.perez@techcorp.mx', 'Gerente de TI');

-- 5. Proyecto
INSERT INTO proyecto (id_proyecto, id_organizacion, id_tecnico, id_coordinador,
                      nombre, descripcion, fecha_inicio, fecha_fin,
                      cupo_maximo, cupo_disponible, estado)
VALUES (1, 1, 1, 2,
        'Sistema de Inventario', 'Desarrollo de sistema web para control de inventario.',
        '2025-01-15', '2025-07-15', 5, 4, 'Disponible');

-- 6. Solicitud (necesaria para asignacion FK)
INSERT INTO solicitud (id_solicitud, id_practicante, estado)
VALUES (1, 1, 'Aceptada');

INSERT INTO solicitud_proyecto (id_solicitud, id_proyecto, orden_preferencia)
VALUES (1, 1, 1);

-- 7. Reporte base (necesario para evaluacion_reporte FK)
INSERT INTO reporte (id_reporte, id_practicante, id_proyecto, tipo_reporte,
                     periodo, ruta_documento, estado)
VALUES (1, 1, 1, 'Parcial', '2025-01', '/docs/reporte_1.pdf', 'Pendiente');

INSERT INTO reporte_parcial_y_final (id_reporte_parcial, numero_informe, horas_cubiertas,
                                     objetivo_general, metodologia, resultados_obtenidos, observaciones)
VALUES (1, 1, 40, 'Desarrollar módulo de inventario', 'Scrum', 'Módulo de inventario completado', 'Sin observaciones');
