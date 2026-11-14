package excepciones;

/** Denegación funcional que el adaptador HTTP puede presentar como 403. */
public final class AccesoDenegadoException extends IllegalArgumentException {
    public AccesoDenegadoException(String mensaje) { super(mensaje); }
}
