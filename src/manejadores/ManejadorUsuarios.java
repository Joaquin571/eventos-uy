package manejadores;

import clases.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
}

