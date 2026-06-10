-- Migracion v8: se colapsa la tabla evaluacion_reporte dentro de reporte.
-- La relacion era 1:1; calificacion y fecha_evaluacion eran los unicos datos
-- exclusivos. retroalimentacion duplicaba reporte.observaciones_profesor y
-- porcentaje_avance estaba sin uso.

ALTER TABLE reporte
    ADD COLUMN calificacion     DECIMAL(4, 2) NULL COMMENT '0.00 - 10.00' AFTER fecha_revision,
    ADD COLUMN fecha_evaluacion DATETIME      NULL                        AFTER calificacion;

UPDATE reporte r
    JOIN evaluacion_reporte er ON er.id_reporte = r.id_reporte
SET r.calificacion     = er.calificacion,
    r.fecha_evaluacion = er.fecha_evaluacion;

DROP TABLE evaluacion_reporte;
