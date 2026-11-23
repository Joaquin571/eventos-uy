package central;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
class MainConfiguracionTest {
    @Test void archivoExternoEvitaDependenciaDelPerfilWindows() {
        String anterior = System.getProperty("eventos.central.config");
        try {
            System.setProperty("eventos.central.config", "C:/entrega-lab3/PC1/config/central.properties");
            assertEquals(Path.of("C:/entrega-lab3/PC1/config/central.properties"), Main.archivoConfiguracion());
        } finally {
            if (anterior == null) System.clearProperty("eventos.central.config");
            else System.setProperty("eventos.central.config", anterior);
        }
    }
}
