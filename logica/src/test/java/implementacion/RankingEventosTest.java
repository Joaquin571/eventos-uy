package implementacion;

import datatypes.*;
import excepciones.AccesoDenegadoException;
import org.junit.jupiter.api.*;
import persistencia.BaseDeDatos;
import soporte.PruebaConBaseSegura;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RankingEventosTest extends PruebaConBaseSegura {
    private static final String ESQUEMA = "ranking_test_" + UUID.randomUUID().toString().replace("-", "");
    private static final Map<String, String> PROPS = BaseDeDatos.propiedadesJpa();
    private final interfaces.ISistema sistema = Fabrica.getInstance().getISistema();
    private final Actor admin = Actor.administradorLocal();

    private static Connection conexion() throws SQLException {
        return DriverManager.getConnection(PROPS.get("jakarta.persistence.jdbc.url"),
                PROPS.get("jakarta.persistence.jdbc.user"), PROPS.get("jakarta.persistence.jdbc.password"));
    }
    @BeforeAll static void aislar() throws Exception {
        // La superclase ya comprobó current_database() por JDBC antes de cualquier escritura.
        BaseDeDatos.cerrar();
        try (var c = conexion(); var q = c.createStatement()) {
            q.execute("CREATE SCHEMA " + ESQUEMA);
            List<String> tablas = new ArrayList<>();
            try (var r = q.executeQuery("SELECT tablename FROM pg_tables WHERE schemaname='public'")) {
                while (r.next()) tablas.add(r.getString(1));
            }
            for (String tabla : tablas) {
                String identificador = "\"" + tabla.replace("\"", "\"\"") + "\"";
                q.execute("CREATE TABLE " + ESQUEMA + "." + identificador + " (LIKE public." + identificador + " INCLUDING ALL)");
            }
        }
        BaseDeDatos.inicializar(propiedadesAisladas());
    }
    private static Map<String, String> propiedadesAisladas() {
        var p = new HashMap<>(PROPS);
        p.put("jakarta.persistence.jdbc.url", PROPS.get("jakarta.persistence.jdbc.url") + "?currentSchema=" + ESQUEMA + ",public");
        p.put("hibernate.generate_statistics", "true");
        return p;
    }
    @AfterAll static void limpiar() throws Exception {
        BaseDeDatos.cerrar();
        try (var c = conexion(); var q = c.createStatement()) { q.execute("DROP SCHEMA " + ESQUEMA + " CASCADE"); }
    }
    @BeforeEach void vaciar() {
        try (var em = BaseDeDatos.getEntityManager()) {
            var tx = em.getTransaction(); tx.begin(); em.createNativeQuery("DELETE FROM evento").executeUpdate(); tx.commit();
        }
    }
    private void evento(String nombre, long visitas, boolean finalizado) {
        try (var em = BaseDeDatos.getEntityManager()) {
            var tx = em.getTransaction(); tx.begin();
            em.createNativeQuery("INSERT INTO evento(nombre, visitas, finalizado) VALUES (:n,:v,:f)")
                    .setParameter("n", nombre).setParameter("v", visitas).setParameter("f", finalizado).executeUpdate();
            tx.commit();
        }
    }
    @Test void cincoOrdenadosConEmpatesFinalizadosYContadorInalterado() {
        evento("Zeta", 90, false); evento("Beta", 50, false); evento("Alfa", 50, true);
        evento("Delta", 30, false); evento("Epsilon", 20, false); evento("Fuera", 10, false); evento("Cero", 0, false);
        var ranking = sistema.obtenerEventosMasVisitados(admin);
        assertEquals(List.of("Zeta", "Alfa", "Beta", "Delta", "Epsilon"), ranking.stream().map(DtEventoVisitado::getNombre).toList());
        assertTrue(ranking.get(1).isFinalizado());
        for (int i = 0; i < 2; i++) assertEquals(List.of(90L,50L,50L,30L,20L), sistema.obtenerEventosMasVisitados(admin).stream().map(DtEventoVisitado::getVisitas).toList());
        try (var em = BaseDeDatos.getEntityManager()) {
            assertEquals(250L, ((Number) em.createNativeQuery("SELECT sum(visitas) FROM evento").getSingleResult()).longValue());
        }
        BaseDeDatos.cerrar();
        soporte.ProteccionBasePruebas.verificar();
        BaseDeDatos.inicializar(propiedadesAisladas());
        assertEquals(List.of(90L,50L,50L,30L,20L), sistema.obtenerEventosMasVisitados(admin).stream().map(DtEventoVisitado::getVisitas).toList());
    }
    @Test void menosDeCincoYCeroVisitas() {
        evento("Beta", 0, false); evento("Alfa", 0, true);
        var r = sistema.obtenerEventosMasVisitados(admin);
        assertEquals(List.of("Alfa", "Beta"), r.stream().map(DtEventoVisitado::getNombre).toList());
        assertTrue(r.stream().allMatch(e -> e.getVisitas() == 0));
    }
    @Test void vacio() { assertTrue(sistema.obtenerEventosMasVisitados(admin).isEmpty()); }
    @Test void soloAdministradorYUnaConsultaSinEntidades() {
        Actor org = Actor.usuarioAutenticado(new DtOrganizador("org", "Org", "org@test.uy", "Desc", ""));
        Actor asistente = Actor.usuarioAutenticado(new DtAsistente("as", "As", "as@test.uy", "Apellido", java.time.LocalDate.of(2000,1,1), null));
        try (var em = BaseDeDatos.getEntityManager()) {
            var stats = em.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class).getStatistics();
            stats.clear();
            for (Actor actor : Arrays.asList(null, org, asistente))
                assertThrows(AccesoDenegadoException.class, () -> sistema.obtenerEventosMasVisitados(actor));
            assertEquals(0, stats.getPrepareStatementCount());
            sistema.obtenerEventosMasVisitados(admin);
            assertEquals(1, stats.getPrepareStatementCount());
            assertEquals(0, stats.getEntityLoadCount());
        }
    }
}
