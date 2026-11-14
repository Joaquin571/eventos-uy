package swing;

import persistencia.BaseDeDatos;
import soap.PublicadorUsuarios;

/** Arranque provisional del bloque 2; el responsable de integración unificará el arranque final. */
public final class IniciarConUsuarios {
    private IniciarConUsuarios() { }
    public static void main(String[] args) {
        BaseDeDatos.inicializar();
        PublicadorUsuarios.publicar();
        principal.main(args);
    }
}