package usuarios;

public class ErrorAccesoUsuarios extends IllegalArgumentException {
    private final String codigo;
    public ErrorAccesoUsuarios(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
    public String getCodigo() { return codigo; }
}