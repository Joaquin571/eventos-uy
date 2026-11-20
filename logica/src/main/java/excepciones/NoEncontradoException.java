package excepciones;
/** Recurso ausente o no visible; no distingue existencia de falta de permiso. */
public final class NoEncontradoException extends IllegalArgumentException {
    public NoEncontradoException() { super("No se encontró la información solicitada."); }
}
