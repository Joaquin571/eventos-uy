package manejadores;

import clases.*;

import java.time.LocalDate;
import java.util.*;

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
        return usuariosNickname.containsKey(nickname);
    }

    public boolean existeCorreo(String correoElectronico) {

        for (Usuario usuario : usuariosNickname.values()) {

            if (usuario.getCorreoElectronico()
                    .equalsIgnoreCase(correoElectronico)) {

                return true;
            }
        }

        return false;
    }

    public boolean estaRegistradoAEdicion(
            String nicknameAsistente,
            String nombreEdicion
    ) {

        Asistente asistente =
                obtenerAsistente(nicknameAsistente);

        if (asistente == null) {
            return false;
        }

        for (Registro registro : asistente.getRegistros()) {

            if (registro.getEdicion() != null &&
                    registro.getEdicion()
                            .getIdNombre()
                            .equalsIgnoreCase(nombreEdicion)) {

                return true;
            }
        }

        return false;
    }

    public Collection<Usuario> listarUsuarios() {
        return usuariosNickname.values();
    }

    public Asistente obtenerAsistente(String nickname) {

        Usuario usuario =
                obtenerUsuario(nickname);

        if (usuario instanceof Asistente asistente) {
            return asistente;
        }

        return null;
    }

    public boolean modificarAsistente(
            String nickname,
            String nombre,
            String correo,
            String apellido,
            LocalDate fechaNacimiento,
            Institucion institucion
    ) {

        Usuario usuario =
                obtenerUsuario(nickname);

        if (!(usuario instanceof Asistente asistente)) {
            return false;
        }

        asistente.setNombre(nombre);
        asistente.setCorreoElectronico(correo);
        asistente.setApellido(apellido);
        asistente.setFechaNacimiento(fechaNacimiento);
        asistente.setInstitucion(institucion);

        return true;
    }

    public boolean modificarOrganizador(
            String nickname,
            String nombre,
            String correo,
            String descripcion,
            String sitioWeb
    ) {

        Usuario usuario =
                obtenerUsuario(nickname);

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