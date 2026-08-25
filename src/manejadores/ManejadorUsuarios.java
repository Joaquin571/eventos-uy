package manejadores;

import clases.*;

import java.util.*;
import java.time.LocalDate;

/** Colección global de usuarios en memoria (singleton). */
public class ManejadorUsuarios {

    private static ManejadorUsuarios instancia = null;

    private final Map<String, Usuario> usuariosNickname;

    private ManejadorUsuarios() {
        usuariosNickname = new HashMap<>();
    }

    public static ManejadorUsuarios getInstance() {
        if (instancia == null) {
            instancia = new ManejadorUsuarios();
        }
        return instancia;
    }

    /** @return true si se agregó; false si la cédula ya existía */
    public boolean addUsuario(Usuario usuario) {
        String nickname = usuario.getNickname();
        if (obtenerUsuario(nickname) != null) {
            return false;
        }
        usuariosNickname.put(nickname, usuario);
        return true;
    }

    public Usuario obtenerUsuario(String nickname) {
        return usuariosNickname.get(nickname);
    }

    public boolean existeUsuario(String nickname) {
        if (usuariosNickname.containsKey(nickname)) {
            return true;
        }
        return false;
    }

    public boolean existeCorreo(String correoElectronico){
        for (Usuario usuario : usuariosNickname.values()){
            if(usuario.getCorreoElectronico().equals(correoElectronico)){
                return true;
            }
        }
        return false;
    }

    public Collection<Usuario> listarUsuarios() {
        return usuariosNickname.values();
    }

    public Asistente obtenerAsistente(String nickname) {

        Usuario usuario = obtenerUsuario(nickname);

        if (usuario instanceof Asistente) {
            return (Asistente) usuario;
        }

        return null;
    }

    public boolean modificarAsistente(
            String nickname,
            String nombre,
            String correo,
            String apellido,
            LocalDate fechaNacimiento
    ) {

        Usuario usuario = obtenerUsuario(nickname);

        if (!(usuario instanceof Asistente asistente)) {
            return false;
        }

        asistente.setNombre(nombre);
        asistente.setCorreoElectronico(correo);
        asistente.setApellido(apellido);
        asistente.setFechaNacimiento(fechaNacimiento);

        return true;
    }

    public boolean modificarOrganizador(
            String nickname,
            String nombre,
            String correo,
            String descripcion,
            String sitioWeb
    ) {

        Usuario usuario = obtenerUsuario(nickname);

        if (!(usuario instanceof Organizador organizador)) {
            return false;
        }

        organizador.setNombre(nombre);
        organizador.setCorreoElectronico(correo);
        organizador.setDescripcion(descripcion);
        organizador.setSitioWeb(sitioWeb);

        return true;
    }
    public Set<String> listarNicknames() {
        return new HashSet<>(usuariosNickname.keySet());
    }
}

