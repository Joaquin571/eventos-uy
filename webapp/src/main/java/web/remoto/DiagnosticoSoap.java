package web.remoto;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Diagnóstico de transporte para el log del servidor, sin cuerpos SOAP ni credenciales. */
public final class DiagnosticoSoap {
    private static final Logger LOG = Logger.getLogger(DiagnosticoSoap.class.getName());

    private DiagnosticoSoap() { }

    public static void transporte(String servicio, URI destino, Throwable error) {
        LOG.log(Level.WARNING, "Fallo SOAP {0} en {1}: {2}",
                new Object[]{servicio, destino, describir(error)});
    }

    public static void configuracion(Path archivo, Throwable error) {
        LOG.log(Level.WARNING, "No se pudo cargar web.properties desde {0}: {1}",
                new Object[]{archivo.toAbsolutePath().normalize(), describir(error)});
    }

    static String describir(Throwable error) {
        if (error == null) return "causa desconocida";
        Throwable raiz = error;
        Set<Throwable> vistas = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int i = 0; i < 20 && raiz.getCause() != null && vistas.add(raiz); i++) {
            if (vistas.contains(raiz.getCause())) break;
            raiz = raiz.getCause();
        }
        return "clase=" + error.getClass().getName() + " mensaje=" + limpiar(error.getMessage())
                + "; causaRaiz=" + raiz.getClass().getName() + " mensaje=" + limpiar(raiz.getMessage());
    }

    private static String limpiar(String mensaje) {
        if (mensaje == null) return "(sin mensaje)";
        String limpio = mensaje.replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("(?i)(Authorization\\s*[:=]\\s*(?:Bearer|Basic)\\s+)\\S+", "$1[REDACTADO]")
                .replaceAll("(?i)\\bBearer\\s+\\S+", "Bearer [REDACTADO]")
                .replaceAll("(?i)((?:password|passwd|contrasena|contraseña|token|secret|clave)\\s*[:=]\\s*)\\S+", "$1[REDACTADO]")
                .replaceAll("(?i)https?://[^\\s/@]+@", "https://[REDACTADO]@");
        return limpio.length() > 350 ? limpio.substring(0, 350) + "…" : limpio;
    }
}
