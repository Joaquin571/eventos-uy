package web.soap;

import jakarta.xml.ws.WebServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import web.remoto.ConfiguracionWeb;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
class ConfiguracionClienteEventosTest {
    @TempDir Path temporal;

    @Test void todosLosClientesUsanElArchivoExternoSeleccionado() throws Exception {
        Path archivo = temporal.resolve("otro-web.properties");
        Files.writeString(archivo, "central.host=central.example\ncentral.puerto=9443\n"
                + "central.ruta=/consulta\ncentral.ruta.portal=/portal-remoto\n"
                + "central.ruta.sesiones=/sesiones-remotas\n");
        conArchivo(archivo, () -> {
            assertEquals("https://central.example:9443/consulta", ClienteEventosCentral.leerDireccion());
            assertEquals("https://central.example:9443/portal-remoto", ConfiguracionWeb.cargar().portal().toString());
            assertEquals("https://central.example:9443/sesiones-remotas", ConfiguracionWeb.cargar().sesiones().toString());
        });
    }

    @Test void ipTailscaleFormaElPortalExactoSinFallbackALocalhost() throws Exception {
        Path archivo = temporal.resolve("web.properties");
        Files.writeString(archivo, "central.host=100.115.156.116\ncentral.puerto=9128\n"
                + "central.ruta=/eventos\ncentral.ruta.portal=/portal\ncentral.ruta.sesiones=/sesiones\n");
        conArchivo(archivo, () -> {
            assertEquals("https://100.115.156.116:9128/portal", ConfiguracionWeb.cargar().portal().toString());
            assertEquals("https://100.115.156.116:9128/eventos", ClienteEventosCentral.leerDireccion());
        });
    }

    @Test void archivoSeleccionadoInexistenteNoUsaOtroComoRespaldo() {
        conArchivo(temporal.resolve("inexistente.properties"), () ->
                assertThrows(WebServiceException.class, ClienteEventosCentral::leerDireccion));
    }

    @Test void sinArchivoAlternativoConservaLaUbicacionHabitual() {
        String anterior = System.getProperty("eventos.web.config");
        try {
            System.clearProperty("eventos.web.config");
            assertEquals(Path.of(System.getProperty("user.home"), ".eventosUy", "web.properties"),
                    ConfiguracionWeb.archivoConfiguracion());
        } finally { restaurar(anterior); }
    }

    private static void conArchivo(Path archivo, Runnable prueba) {
        String anterior = System.getProperty("eventos.web.config");
        try {
            System.setProperty("eventos.web.config", archivo.toString());
            prueba.run();
        } finally { restaurar(anterior); }
    }

    private static void restaurar(String anterior) {
        if (anterior == null) System.clearProperty("eventos.web.config");
        else System.setProperty("eventos.web.config", anterior);
    }
}
