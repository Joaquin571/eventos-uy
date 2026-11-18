package web.soap;

import datatypes.Actor;
import implementacion.Fabrica;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistencia.BaseDeDatos;
import web.remoto.ClienteCentral;

import java.net.URI;
import java.nio.file.*;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Instala los artefactos reales sobre un esquema desechable de eventosuy_test. */
class InstalacionLimpiaIT {
    @Test void preparaMigraArrancaYReiniciaSinIde(@TempDir Path temporal) throws Exception {
        PortalPersistenciaHttpsTest.verificarBase();
        var db = BaseDeDatos.propiedadesJpa();
        String esquema = "instalacion_" + UUID.randomUUID().toString().replace("-", "");
        Path jar = Path.of("../servidor-central/target/servidor.jar").toAbsolutePath();
        assertTrue(Files.isRegularFile(jar));
        temporal = temporal.toAbsolutePath();
        String url = db.get("jakarta.persistence.jdbc.url") + "?currentSchema=" + esquema + ",public";
        try (var conexion = DriverManager.getConnection(db.get("jakarta.persistence.jdbc.url"),
                db.get("jakarta.persistence.jdbc.user"), db.get("jakarta.persistence.jdbc.password"));
             var sql = conexion.createStatement()) {
            try (var resultado = sql.executeQuery("SELECT current_database()")) {
                assertTrue(resultado.next()); assertEquals("eventosuy_test", resultado.getString(1));
            }
            sql.execute("CREATE SCHEMA " + esquema);
            try {
                var instalador = new ProcessBuilder(javaBin(), "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-cp", jar.toString(),
                        "persistencia.PrepararEsquemaNuevo", "--base-vacia", "--schema", esquema);
                instalador.environment().put("EVENTOS_DB_HOST", System.getenv("EVENTOS_DB_HOST"));
                instalador.environment().put("EVENTOS_DB_PORT", System.getenv("EVENTOS_DB_PORT"));
                instalador.environment().put("EVENTOS_DB_NAME", "eventosuy_test");
                instalador.environment().put("EVENTOS_DB_USER", System.getenv("EVENTOS_DB_USER"));
                instalador.environment().put("EVENTOS_DB_PASSWORD", System.getenv("EVENTOS_DB_PASSWORD"));
                instalador.redirectErrorStream(true).redirectOutput(temporal.resolve("preparador.log").toFile());
                var preparador = instalador.start();
                assertTrue(preparador.waitFor(45, TimeUnit.SECONDS));
                assertEquals(0, preparador.exitValue(), Files.readString(temporal.resolve("preparador.log")));
                var repetido = instalador.redirectOutput(temporal.resolve("repetido.log").toFile()).start();
                assertTrue(repetido.waitFor(30, TimeUnit.SECONDS));
                assertNotEquals(0, repetido.exitValue(), "Una segunda instalación debe rechazarse.");

                sql.execute("SET search_path TO " + esquema + ",public");
                for (String nombre : List.of("V20261116_01__visitas_evento.sql", "V20261116_02__unicidad_ci.sql")) {
                    String texto = Files.readString(Path.of("../logica/src/main/resources/db/migration", nombre));
                    sql.execute(texto);
                }
                for (String nombre : List.of("lab3-emmanuel-busqueda.sql", "lab3-emmanuel-video.sql")) {
                    String texto = Files.readString(Path.of("../sql", nombre));
                    for (String sentencia : texto.split(";")) {
                        String ejecutable = sentencia.replaceAll("(?m)^--.*$", "").trim();
                        if (!ejecutable.isEmpty()) sql.execute(ejecutable);
                    }
                }
                sql.execute("INSERT INTO evento(nombre,visitas,finalizado) VALUES ('Evento instalación',0,false)");

                Path configuracion = temporal.resolve(".eventosUy"); Files.createDirectories(configuracion);
                Path almacen = temporal.resolve("central.p12");
                char[] clave = UUID.randomUUID().toString().toCharArray();
                SoporteTls.generar(almacen, clave);
                var confianza = SoporteTls.confianza(almacen, clave);
                var p = new Properties();
                p.setProperty("central.host", "127.0.0.1"); p.setProperty("central.escucha", "127.0.0.1");
                p.setProperty("central.puerto", "0"); p.setProperty("central.ruta", "/eventos");
                p.setProperty("central.ruta.sesiones", "/sesiones"); p.setProperty("central.ruta.portal", "/portal");
                p.setProperty("sesiones.duracion.segundos", "300");
                p.setProperty("tls.almacen", almacen.toString());
                p.setProperty("tls.almacen.password", new String(clave));
                p.setProperty("tls.clave.password", new String(clave)); Arrays.fill(clave, '\0');
                p.setProperty("db.url", url); p.setProperty("db.usuario", db.get("jakarta.persistence.jdbc.user"));
                p.setProperty("db.password", db.get("jakarta.persistence.jdbc.password"));
                try (var salida = Files.newBufferedWriter(configuracion.resolve("central.properties"))) { p.store(salida, "Solo prueba"); }

                try (var central = iniciar(jar, temporal, "primero")) {
                    var cliente = new ClienteCentral(central.portal, null, confianza);
                    assertNotNull(cliente.listarEventos());
                    cliente.registrarVisitaEvento("Evento instalación");
                }
                int nuevoPuerto;
                try (var disponible = new java.net.ServerSocket(0)) { nuevoPuerto = disponible.getLocalPort(); }
                p.setProperty("central.puerto", Integer.toString(nuevoPuerto));
                p.setProperty("central.ruta.portal", "/portal-nuevo");
                try (var salida = Files.newBufferedWriter(configuracion.resolve("central.properties"))) { p.store(salida, "Cambio sin recompilar"); }
                try (var central = iniciar(jar, temporal, "segundo")) {
                    assertEquals(nuevoPuerto, central.portal.getPort());
                    assertEquals("/portal-nuevo", central.portal.getPath());
                    assertNotNull(new ClienteCentral(central.portal, null, confianza).listarEventos());
                    try (var resultado = sql.executeQuery("SELECT visitas FROM evento WHERE nombre='Evento instalación'")) {
                        assertTrue(resultado.next()); assertEquals(1, resultado.getLong(1));
                    }
                }
                var aislada = new HashMap<>(db);
                aislada.put("jakarta.persistence.jdbc.url", url);
                BaseDeDatos.inicializar(aislada);
                try {
                    var ranking = Fabrica.getInstance().getISistema().obtenerEventosMasVisitados(Actor.administradorLocal());
                    assertEquals("Evento instalación", ranking.getFirst().getNombre());
                    assertEquals(1, ranking.getFirst().getVisitas());
                } finally { BaseDeDatos.cerrar(); }
            } finally {
                sql.execute("SET search_path TO public");
                sql.execute("DROP SCHEMA " + esquema + " CASCADE");
            }
        }
    }

    private static String javaBin() { return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString(); }

    private static Central iniciar(Path jar, Path temporal, String nombre) throws Exception {
        Path log = temporal.resolve(nombre + ".log");
        var proceso = new ProcessBuilder(javaBin(), "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-Duser.home=" + temporal, "-jar", jar.toString(), "--sin-swing")
                .directory(temporal.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        long limite = System.nanoTime() + Duration.ofSeconds(50).toNanos();
        while (System.nanoTime() < limite) {
            assertTrue(proceso.isAlive(), "Central terminó: " + Files.readString(log));
            for (String linea : Files.readAllLines(log))
                if (linea.startsWith("WSDL del portal: "))
                    return new Central(proceso, URI.create(linea.substring("WSDL del portal: ".length()).replace("?wsdl", "")));
            Thread.sleep(100);
        }
        proceso.destroyForcibly();
        fail("Central no publicó HTTPS: " + Files.readString(log));
        return null;
    }

    private record Central(Process proceso, URI portal) implements AutoCloseable {
        @Override public void close() throws Exception {
            proceso.destroy();
            if (!proceso.waitFor(15, TimeUnit.SECONDS)) proceso.destroyForcibly();
        }
    }
}
