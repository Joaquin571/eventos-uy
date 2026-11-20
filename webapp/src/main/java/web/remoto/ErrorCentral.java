package web.remoto;
/** Error remoto controlado; no incluye causas de transporte ni secretos. */
public final class ErrorCentral extends RuntimeException {
    private final String codigo;
    public ErrorCentral(String codigo,String mensaje){super(mensaje);this.codigo=codigo;}
    public String getCodigo(){return codigo;}
}
