package persistencia;

import org.junit.jupiter.api.Test;
import soporte.PruebaConBaseSegura;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PrepararEsquemaNuevoTest extends PruebaConBaseSegura {
    @Test void creaSoloEsquemaVacioYPermiteValidar() throws Exception {
        var base = BaseDeDatos.propiedadesJpa();
        String esquema = "instalacion_" + UUID.randomUUID().toString().replace("-", "");
        try (var c = DriverManager.getConnection(base.get("jakarta.persistence.jdbc.url"),
                base.get("jakarta.persistence.jdbc.user"), base.get("jakarta.persistence.jdbc.password"));
             var q = c.createStatement()) {
            q.execute("CREATE SCHEMA " + esquema);
            try {
                var aislada = new HashMap<>(base);
                aislada.put("jakarta.persistence.jdbc.url", base.get("jakarta.persistence.jdbc.url") + "?currentSchema=" + esquema + ",public");
                PrepararEsquemaNuevo.preparar(aislada);
                try (var r = q.executeQuery("SELECT count(*) FROM information_schema.tables WHERE table_schema='" + esquema + "' AND table_name='evento'")) {
                    assertTrue(r.next()); assertEquals(1, r.getInt(1));
                }
                assertThrows(IllegalStateException.class, () -> PrepararEsquemaNuevo.preparar(aislada));
                aislada.put("hibernate.hbm2ddl.auto", "validate");
                try (var factory = jakarta.persistence.Persistence.createEntityManagerFactory("postgres", aislada)) {
                    assertNotNull(factory.createEntityManager());
                }
            } finally { q.execute("DROP SCHEMA " + esquema + " CASCADE"); }
        }
    }
}
