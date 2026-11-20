package datatypes;

import java.util.Objects;

/** Contexto creado por una entrada local de confianza a partir de la identidad de sesión o de la estación. */
public final class Actor {
    private final String nickname;
    private final boolean organizador;
    private final boolean administradorLocal;

    private Actor(String nickname, boolean organizador, boolean administradorLocal) {
        this.nickname = nickname;
        this.organizador = organizador;
        this.administradorLocal = administradorLocal;
    }

    public static Actor usuarioAutenticado(DtUsuario usuario) {
        Objects.requireNonNull(usuario, "Se requiere un usuario autenticado.");
        return new Actor(usuario.getNickname(), usuario instanceof DtOrganizador, false);
    }

    /** Para el arranque administrativo local y los preparadores de datos de confianza. */
    public static Actor administradorLocal() {
        return new Actor(null, false, true);
    }

    public String nickname() { return nickname; }
    public boolean esOrganizador() { return organizador; }
    public boolean esAdministradorLocal() { return administradorLocal; }
}
