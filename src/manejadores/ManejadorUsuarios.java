package manejadores;

import clases.Usuario;
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
}
