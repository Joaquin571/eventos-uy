-- Aplicar una vez antes de arrancar Main (que valida el esquema, no lo modifica).
-- Conserva eventos y contadores existentes. No contiene credenciales ni selecciona una base.
BEGIN;
ALTER TABLE evento ADD COLUMN IF NOT EXISTS visitas BIGINT NOT NULL DEFAULT 0;
ALTER TABLE evento ADD COLUMN IF NOT EXISTS finalizado BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE evento SET visitas = 0 WHERE visitas IS NULL;
UPDATE evento SET finalizado = FALSE WHERE finalizado IS NULL;
ALTER TABLE evento ALTER COLUMN visitas SET DEFAULT 0;
ALTER TABLE evento ALTER COLUMN visitas SET NOT NULL;
ALTER TABLE evento ALTER COLUMN finalizado SET DEFAULT FALSE;
ALTER TABLE evento ALTER COLUMN finalizado SET NOT NULL;
COMMIT;
