package central.soap;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"nickname", "nombre", "rol"})
public class UsuarioSesionSoap {
    public enum Rol { ASISTENTE, ORGANIZADOR }

    private String nickname;
    private String nombre;
    private Rol rol;

    public UsuarioSesionSoap() { }

    UsuarioSesionSoap(String nickname, String nombre, Rol rol) {
        this.nickname = nickname;
        this.nombre = nombre;
        this.rol = rol;
    }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}
