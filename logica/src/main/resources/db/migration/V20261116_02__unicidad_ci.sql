-- Rechaza duplicados preexistentes antes de imponer unicidad sin distinguir mayúsculas.
-- Ejecutar en la base prevista tras revisar los conflictos; no modifica filas.
BEGIN;
CREATE UNIQUE INDEX IF NOT EXISTS ux_usuario_correo_ci ON usuario (lower(correoelectronico));
CREATE UNIQUE INDEX IF NOT EXISTS ux_tipo_registro_edicion_nombre_ci
    ON tipo_registro (edicion_id, lower(nombre));
COMMIT;
