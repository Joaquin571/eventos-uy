-- Aplicada el 03/11/2026 SOLO en eventosuy_test. Ver H13-procedimiento.md.
-- Otra base requiere autorización específica. No repetir sobre índices existentes.
-- No normaliza, elimina ni fusiona datos. Conserva las claves actuales.
-- Ejecutar con cliente que se detenga ante errores (psql: ON_ERROR_STOP=1).
-- Antes, identificar y respaldar la base; establecer explícitamente en la SESIÓN:
-- SET eventosuy.base_destino = 'nombre_de_la_base_autorizada';
-- Las pruebas automatizadas NO deben ejecutar este script.
BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '60s';
DO $$
BEGIN
    IF current_setting('eventosuy.base_destino', true) IS DISTINCT FROM current_database() THEN
        RAISE EXCEPTION 'Debe confirmar explícitamente la base destinataria en eventosuy.base_destino';
    END IF;
END $$;

-- Evita carreras entre la auditoría y la creación de índices. Bloquea escrituras
-- durante esta transacción: programar una ventana de mantenimiento.
LOCK TABLE public.usuario, public.tipo_registro IN SHARE MODE;
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM public.usuario
               GROUP BY lower(correoelectronico) HAVING count(*) > 1) THEN
        RAISE EXCEPTION 'Hay correos duplicados sin distinguir mayúsculas. Requieren decisión manual.';
    END IF;
    IF EXISTS (SELECT 1 FROM public.tipo_registro
               GROUP BY edicion_id, lower(nombre) HAVING count(*) > 1) THEN
        RAISE EXCEPTION 'Hay tipos duplicados por edición sin distinguir mayúsculas. Requieren decisión manual.';
    END IF;
END $$;

CREATE UNIQUE INDEX ux_usuario_correo_ci ON public.usuario (lower(correoelectronico));
CREATE UNIQUE INDEX ux_tipo_registro_edicion_nombre_ci ON public.tipo_registro (edicion_id, lower(nombre));
COMMIT;
-- No usar IF NOT EXISTS: un índice homónimo con definición distinta debe detener la operación.
-- Cualquier error exige ROLLBACK; no continuar manualmente dentro de una transacción fallida.
