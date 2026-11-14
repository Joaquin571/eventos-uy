package manejadores;

import java.sql.SQLException;

/** Convierte únicamente violaciones de unicidad; conserva los demás errores técnicos. */
final class ConflictosPersistencia {
    private ConflictosPersistencia() { }

    static RuntimeException traducir(RuntimeException error, String mensaje) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                return new IllegalArgumentException(mensaje, error);
            }
        }
        return error;
    }
}
