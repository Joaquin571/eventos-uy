package soap;

public class FalloUsuarios {
    private String codigo;
    private String mensaje;
    public FalloUsuarios() { }
    public FalloUsuarios(String codigo, String mensaje) { this.codigo = codigo; this.mensaje = mensaje; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String valor) { codigo = valor; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String valor) { mensaje = valor; }
}