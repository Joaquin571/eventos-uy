package web.remoto;

/** Adaptador de P03; no mantiene un registro de sesiones alternativo. */
public interface ServicioSesiones {
    ClienteSesionesCentral.Inicio iniciar(String identificador, String contrasenia);
    void cerrar(String credencial);
}
