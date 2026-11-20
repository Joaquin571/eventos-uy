package web.remoto;

/** Adaptador de P03; no mantiene un registro de sesiones alternativo. */
public interface ServicioSesiones {
    ClienteSesionesCentral.Inicio iniciar(String identificador, String contrasenia);
    default ClienteSesionesCentral.Inicio iniciarMovil(String identificador, String contrasenia) {
        throw new UnsupportedOperationException("El adaptador no implementa autenticacion movil.");
    }
    void cerrar(String credencial);
}
