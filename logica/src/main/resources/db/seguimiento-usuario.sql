-- Aplicar al esquema de Central antes de iniciar con hbm2ddl=validate.
CREATE TABLE IF NOT EXISTS seguimiento_usuario (
    seguidor varchar(255) NOT NULL REFERENCES usuario(nickname),
    seguido varchar(255) NOT NULL REFERENCES usuario(nickname),
    CONSTRAINT uq_seguimiento_usuario UNIQUE (seguidor, seguido),
    CONSTRAINT ck_seguimiento_distinto CHECK (seguidor <> seguido)
);
CREATE INDEX IF NOT EXISTS ix_seguimiento_seguido ON seguimiento_usuario(seguido);
