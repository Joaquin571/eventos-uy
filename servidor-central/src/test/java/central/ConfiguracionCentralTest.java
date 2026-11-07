package central;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ConfiguracionCentralTest {
    @TempDir Path directorio;

    private ConfiguracionCentral configurar(String texto) throws Exception {
        Path archivo = directorio.resolve("central.properties");
        Files.writeString(archivo, texto);
        return new ConfiguracionCentral(archivo);
    }

    @Test void construyeEndpointExternoYPropiedadesSinCrearEsquema() throws Exception {
        var config = configurar("central.host=192.0.2.25\ncentral.puerto=9876\ncentral.ruta=/consulta\n"
                + "db.url=jdbc:postgresql://db-prueba:5432/eventosuy_test\ndb.usuario=prueba\ndb.password=valor-simulado\n");
        assertEquals("http://192.0.2.25:9876/consulta", config.urlServicio());
        var props = config.propiedadesJpa();
        assertEquals("validate", props.get("hibernate.hbm2ddl.auto"));
        assertEquals("jdbc:postgresql://db-prueba:5432/eventosuy_test", props.get("jakarta.persistence.jdbc.url"));
        assertEquals("prueba", props.get("jakarta.persistence.jdbc.user"));
        assertEquals("valor-simulado", props.get("jakarta.persistence.jdbc.password"));
    }

    @Test void faltaDeArchivoImpideUsarValoresPorDefecto() {
        assertThrows(IllegalStateException.class, () -> new ConfiguracionCentral(directorio.resolve("ausente")));
    }

    @Test void faltanClavesNoSeUsanCredencialesDelLabDos() throws Exception {
        var config = configurar("central.host=127.0.0.1\n");
        assertThrows(IllegalStateException.class, config::urlServicio);
        assertThrows(IllegalStateException.class, config::propiedadesJpa);
    }

    @Test void rechazaPuertoFueraDeRangoYRutaInvalida() throws Exception {
        var config = configurar("central.host=127.0.0.1\ncentral.puerto=65536\ncentral.ruta=/eventos\n");
        assertThrows(IllegalStateException.class, config::urlServicio);
        config = configurar("central.host=127.0.0.1\ncentral.puerto=9876\ncentral.ruta=sin-barra\n");
        assertThrows(IllegalStateException.class, config::urlServicio);
    }
}
