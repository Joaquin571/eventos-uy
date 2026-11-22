package implementacion;

import datatypes.*;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Requiere la barrera JDBC de PruebaConBaseSegura y el esquema de Lab 3. */
class EventosEdicionesBusquedaLab3Test extends soporte.PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());

    private Actor preparar(String evento) throws Exception {
        String nick = "organizador" + sufijo;
        DtOrganizador organizador = new DtOrganizador(nick, "Organizador", nick + "@test.invalid", "Prueba", "https://example.org");
        organizador.setContrasenia("clave1");
        organizador.setConfirmacionContrasenia("clave1");
        assertTrue(sistema.altaOrganizador(organizador));
        String categoria = "categoria" + sufijo;
        sistema.altaCategoria(categoria, null, Actor.administradorLocal());
        assertTrue(sistema.altaEvento(new DtEvento(evento, "EV", "Descripción buscable " + sufijo,
                LocalDate.now(), List.of(categoria)), Actor.administradorLocal()));
        return Actor.usuarioAutenticado(organizador);
    }

    @Test void videoOpcionalSePersisteYRecupera() throws Exception {
        String evento = "videoEvento" + sufijo;
        Actor actor = preparar(evento);
        DtEdicion edicion = new DtEdicion("videoEdicion" + sufijo, "VE", LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2), LocalDate.now(), "Montevideo", "Uruguay", actor.nickname());
        edicion.setVideoYoutube("https://youtu.be/dQw4w9WgXcQ");
        edicion.setDescripcion("Descripción propia de la edición");
        assertTrue(sistema.altaEdicion(edicion, evento, actor));
        DtEdicion guardada = sistema.obtenerEdicionesEvento(evento).iterator().next();
        assertEquals("https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ", guardada.getVideoYoutube());
        assertEquals("Descripción propia de la edición", guardada.getDescripcion());
    }

    @Test void finalizarImpideAltaYExcluyeDeBusquedaPublica() throws Exception {
        String evento = "finalEvento" + sufijo;
        Actor actor = preparar(evento);
        assertThrows(excepciones.AccesoDenegadoException.class, () -> sistema.finalizarEvento(evento, null));
        assertThrows(excepciones.AccesoDenegadoException.class,
                () -> sistema.finalizarEvento(evento, Actor.administradorLocal()));
        sistema.finalizarEvento(evento, actor);
        assertTrue(sistema.listarEventos().stream().anyMatch(e -> e.getNombre().equals(evento) && e.isFinalizado()));
        assertTrue(sistema.listarEventosPublicos().stream().noneMatch(e -> e.getNombre().equals(evento)));
        assertTrue(sistema.buscarEventosYEdiciones(sufijo, OrdenBusqueda.FECHA_DESC).stream()
                .noneMatch(e -> e.getNombre().equals(evento)));
        DtEdicion edicion = new DtEdicion("nuevaEdicion" + sufijo, "NE", LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2), LocalDate.now(), "Montevideo", "Uruguay", actor.nickname());
        assertThrows(IllegalArgumentException.class, () -> sistema.altaEdicion(edicion, evento, actor));
        assertThrows(IllegalArgumentException.class, () -> sistema.finalizarEvento(evento, actor));
    }

    @Test void listadoPublicoNoConsultaOtraVezCadaEvento() throws Exception {
        String primero="consultaPublicaA"+sufijo, segundo="consultaPublicaB"+sufijo;
        preparar(primero);
        sistema.altaEvento(new DtEvento(segundo,"PB","Segundo",LocalDate.now(),List.of("categoria"+sufijo)),Actor.administradorLocal());
        try(var em=persistencia.BaseDeDatos.getEntityManager()){
            var estadisticas=em.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class).getStatistics();
            boolean habilitadas=estadisticas.isStatisticsEnabled();
            try{
                estadisticas.setStatisticsEnabled(true);estadisticas.clear();
                var eventos=sistema.listarEventosPublicos();
                assertTrue(eventos.stream().anyMatch(e->primero.equals(e.getNombre())));
                assertTrue(eventos.stream().anyMatch(e->segundo.equals(e.getNombre())));
                assertTrue(eventos.stream().noneMatch(DtEvento::isFinalizado));
                System.out.println("[PRUEBA] Consultas JPQL del listado público: "+estadisticas.getQueryExecutionCount());
                assertTrue(estadisticas.getQueryExecutionCount()<=2,
                        "El listado público no debe ejecutar una consulta JPQL por cada evento: "+estadisticas.getQueryExecutionCount());
            }finally{estadisticas.setStatisticsEnabled(habilitadas);}
        }
    }

    @Test void busquedaIgnoraMayusculasYOrdena() throws Exception {
        String a = "Alpha" + sufijo;
        String b = "Zeta" + sufijo;
        preparar(a);
        String categoria = "categoriaDos" + sufijo;
        sistema.altaCategoria(categoria, null, Actor.administradorLocal());
        assertTrue(sistema.altaEvento(new DtEvento(b, "ZE", "Descripción buscable " + sufijo,
                LocalDate.now(), List.of(categoria)), Actor.administradorLocal()));
        var asc = sistema.buscarEventosYEdiciones(sufijo.toUpperCase(), OrdenBusqueda.NOMBRE_ASC);
        assertEquals(2, asc.stream().filter(r -> r.getTipo().equals("EVENTO")).count());
        assertEquals(a, asc.getFirst().getNombre());
        var desc = sistema.buscarEventosYEdiciones(sufijo, OrdenBusqueda.NOMBRE_DESC);
        assertEquals(b, desc.getFirst().getNombre());
        assertTrue(sistema.buscarEventosYEdiciones("no-existe-" + sufijo, OrdenBusqueda.FECHA_DESC).isEmpty());
        assertEquals(2, sistema.buscarEventosYEdiciones(sufijo, null).stream()
                .filter(r -> r.getTipo().equals("EVENTO")).count());
        assertThrows(IllegalArgumentException.class,
                () -> sistema.buscarEventosYEdiciones("x".repeat(256), OrdenBusqueda.FECHA_DESC));
    }
}
