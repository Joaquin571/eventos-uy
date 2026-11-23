package central;

import implementacion.Fabrica;
import interfaces.ISistema;
import persistencia.BaseDeDatos;
import java.nio.file.Path;

public final class Main {
    private Main() { }

    static Path archivoConfiguracion() {
        String externo = System.getProperty("eventos.central.config");
        return externo == null || externo.isBlank()
                ? Path.of(System.getProperty("user.home"), ".eventosUy", "central.properties")
                : Path.of(externo);
    }

    public static void main(String[] args) {
        ConfiguracionCentral configuracion = new ConfiguracionCentral(archivoConfiguracion());
        // Configuración TLS y listener preparados antes de intentar inicializar persistencia.
        ServidorHttpsCentral servidor = new ServidorHttpsCentral(configuracion);
        try {
            BaseDeDatos.inicializar(configuracion.propiedadesJpa());
            ISistema sistema = Fabrica.getInstance().getISistema();
            servidor.iniciar(sistema);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { servidor.close(); } finally { BaseDeDatos.cerrar(); }
            }, "cerrar-central"));
            System.out.println("WSDL de eventos: " + servidor.urlEventos() + "?wsdl");
            System.out.println("WSDL de sesiones: " + servidor.urlSesiones() + "?wsdl");
            System.out.println("WSDL del portal: " + servidor.urlPortal() + "?wsdl");
            if (!java.util.Arrays.asList(args).contains("--sin-swing")) swing.principal.main(args);
        } catch (RuntimeException | Error error) {
            try {
                servidor.close();
            } catch (RuntimeException cierre) {
                error.addSuppressed(cierre);
            } finally {
                BaseDeDatos.cerrar();
            }
            throw error;
        }
    }
}
