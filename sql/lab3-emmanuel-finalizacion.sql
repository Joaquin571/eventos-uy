-- Ejecutar en base autorizada tras respaldo; no modifica ediciones ni registros.
ALTER TABLE evento ADD COLUMN IF NOT EXISTS finalizado BOOLEAN NOT NULL DEFAULT FALSE;
