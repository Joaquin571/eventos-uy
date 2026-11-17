-- Ampliación mínima: la edición no tenía descripción propia en el modelo previo.
ALTER TABLE edicion ADD COLUMN IF NOT EXISTS descripcion VARCHAR(2048);
