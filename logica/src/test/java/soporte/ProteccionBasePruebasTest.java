package soporte;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas puras: los escenarios inseguros nunca abren conexiones reales. */
class ProteccionBasePruebasTest {
    private final Map<String, String> propiedades = Map.of(
            "jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:5432/eventosuy_test");

    @Test
    void configuracionAusenteOVariableIncorrectaBloqueaAntesDeConectar() {
        for (String nombre : new String[]{null, "", " ", "eventosuy", "otra"}) {
            assertThrows(IllegalStateException.class, () -> ProteccionBasePruebas.verificar(
                    nombre, propiedades, (url, usuario, clave) -> {
                        fail("No debe abrir una conexión con configuración insegura.");
                        return null;
                    }));
        }
    }

    @Test
    void urlEfectivaIncorrectaOAusenteBloqueaAntesDeConectar() {
        for (Map<String, String> configuracion : java.util.List.of(Map.<String, String>of(),
                Map.of("jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost/eventosuy"))) {
            assertThrows(IllegalStateException.class, () -> ProteccionBasePruebas.verificar(
                    "eventosuy_test", configuracion, (url, usuario, clave) -> {
                        fail("No debe conectar a otra base.");
                        return null;
                    }));
        }
    }

    @Test
    void conexionNoVerificableBloqueaConSuCausa() {
        SQLException causa = new SQLException("Sin conexión simulada");
        var error = assertThrows(IllegalStateException.class, () -> ProteccionBasePruebas.verificar(
                "eventosuy_test", propiedades, (url, usuario, clave) -> { throw causa; }));
        assertSame(causa, error.getCause());
    }

    @Test
    void nombreRealDistintoOResultadoVacioBloqueaYCierraLaConexion() {
        for (boolean hayFila : new boolean[]{true, false}) {
            AtomicBoolean cerrada = new AtomicBoolean();
            assertThrows(IllegalStateException.class, () -> ProteccionBasePruebas.verificar(
                    "eventosuy_test", propiedades,
                    (url, usuario, clave) -> conexion("otra_base_simulada", hayFila, cerrada)));
            assertTrue(cerrada.get());
        }
    }

    @Test
    void permiteSoloNombreRealCorrectoConConsultaDeSoloLectura() {
        AtomicBoolean cerrada = new AtomicBoolean();
        assertDoesNotThrow(() -> ProteccionBasePruebas.verificar("eventosuy_test", propiedades,
                (url, usuario, clave) -> conexion("eventosuy_test", true, cerrada)));
        assertTrue(cerrada.get());
    }

    private Connection conexion(String baseReal, boolean hayFila, AtomicBoolean cerrada) {
        AtomicBoolean soloLectura = new AtomicBoolean();
        ResultSet resultado = doble(ResultSet.class, (metodo, argumentos) -> switch (metodo) {
            case "next" -> hayFila;
            case "getString" -> baseReal;
            case "close" -> null;
            default -> throw new AssertionError("Operación inesperada: " + metodo);
        });
        Statement consulta = doble(Statement.class, (metodo, argumentos) -> switch (metodo) {
            case "executeQuery" -> {
                assertTrue(soloLectura.get());
                assertEquals("SELECT current_database()", argumentos[0]);
                yield resultado;
            }
            case "setQueryTimeout", "close" -> null;
            default -> throw new AssertionError("No se permiten escrituras: " + metodo);
        });
        return doble(Connection.class, (metodo, argumentos) -> switch (metodo) {
            case "setReadOnly" -> { soloLectura.set((boolean) argumentos[0]); yield null; }
            case "createStatement" -> consulta;
            case "close" -> { cerrada.set(true); yield null; }
            default -> throw new AssertionError("Operación inesperada: " + metodo);
        });
    }

    @FunctionalInterface
    private interface Invocacion { Object ejecutar(String metodo, Object[] argumentos); }

    private <T> T doble(Class<T> tipo, Invocacion invocacion) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo},
                (proxy, metodo, argumentos) -> invocacion.ejecutar(metodo.getName(), argumentos)));
    }
}
