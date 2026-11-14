package web.remoto;

public class ErrorRemotoUsuarios extends IllegalArgumentException {
    private final String codigo;
    public ErrorRemotoUsuarios(String codigo, String mensaje) { super(mensaje); this.codigo = codigo; }
    public String getCodigo() { return codigo; }
}