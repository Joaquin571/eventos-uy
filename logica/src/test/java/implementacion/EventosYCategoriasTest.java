package implementacion;

import datatypes.DtCategoria;
import datatypes.DtEdicion;
import datatypes.DtEvento;
import datatypes.DtOrganizador;
import interfaces.ISistema;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reglas de eventos y categorías vistas desde la interfaz pública (sección 7.5).
 * Complementa a ReglasAltaTest y ConsultasSistemaTest: acá se comprueba que un alta
 * rechazada no deja nada a medias y que las categorías se arman bien.
 * Cada prueba crea datos propios, así que se ejecuta solo en la base eventosuy_test.
 */
class EventosYCategoriasTest {

    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = Long.toString(System.nanoTime());
    private final LocalDate hoy = LocalDate.now();

    @BeforeAll
    static void exigirBaseAislada() {
        Assumptions.assumeTrue(
                "eventosuy_test".equals(System.getenv("EVENTOS_DB_NAME")),
                "Se omiten las pruebas que crean datos fuera de eventosuy_test");
    }

    // ------------------------------------------------------------ preparación

    private String categoria(String prefijo, String padre) throws Exception {
        String nombre = prefijo + " " + sufijo;
        sistema.altaCategoria(nombre, padre);
        return nombre;
    }

    private DtEvento dtEvento(String nombre, String sigla, String descripcion, List<String> categorias) {
        return new DtEvento(nombre, sigla, descripcion, hoy, categorias);
    }

    private String evento(String prefijo, List<String> categorias) throws Exception {
        String nombre = prefijo + " " + sufijo;
        assertTrue(sistema.altaEvento(dtEvento(nombre, "SG", "Descripción de " + prefijo, categorias)));
        return nombre;
    }

    private List<DtEvento> eventosLlamados(String nombre) {
        return sistema.listarEventos().stream().filter(e -> nombre.equals(e.getNombre())).toList();
    }

    private List<DtCategoria> categoriasLlamadas(String nombre) {
        return sistema.listarCategorias().stream().filter(c -> nombre.equals(c.getIdNombre())).toList();
    }

    private String organizador() {
        String nick = "org" + sufijo;
        DtOrganizador dt = new DtOrganizador(nick, "Organizadora", nick + "@test.uy",
                "Descripción", "https://organizador.test");
        dt.setContrasenia("clave1");
        dt.setConfirmacionContrasenia("clave1");
        assertTrue(sistema.altaOrganizador(dt));
        return nick;
    }

    // -------------------------------------------------------------- categorías

    @Test
    void unaCategoriaSinNombreSeRechaza() {
        assertThrows(Exception.class, () -> sistema.altaCategoria(null, null));
        assertThrows(Exception.class, () -> sistema.altaCategoria("", null));
        assertThrows(Exception.class, () -> sistema.altaCategoria("   ", null));
    }

    @Test
    void unaCategoriaConPadreInexistenteSeRechazaYNoQuedaRegistrada() {
        String nombre = "Huérfana " + sufijo;

        assertThrows(Exception.class, () -> sistema.altaCategoria(nombre, "NoExiste " + sufijo));

        assertTrue(categoriasLlamadas(nombre).isEmpty());
    }

    @Test
    void unPadreEnBlancoCreaUnaCategoriaRaiz() throws Exception {
        String nombre = categoria("Raíz en blanco", "   ");

        var creadas = categoriasLlamadas(nombre);
        assertEquals(1, creadas.size());
        assertNull(creadas.get(0).getNombrePadre());
    }

    @Test
    void unaCategoriaRepetidaNoCambiaLaOriginal() throws Exception {
        String padre = categoria("Padre", null);
        String hija = categoria("Hija", padre);

        // Se intenta volver a crear la hija como raíz: se rechaza y sigue colgando de su padre
        assertThrows(Exception.class, () -> sistema.altaCategoria(hija, null));

        var existentes = categoriasLlamadas(hija);
        assertEquals(1, existentes.size());
        assertEquals(padre, existentes.get(0).getNombrePadre());
    }

    // ------------------------------------------------------------------ eventos

    @Test
    void unEventoRepetidoSeRechazaYNoModificaElOriginal() throws Exception {
        String categoriaOriginal = categoria("Original", null);
        String categoriaNueva = categoria("Nueva", null);
        String nombre = evento("Evento repetido", List.of(categoriaOriginal));

        assertThrows(Exception.class, () -> sistema.altaEvento(
                dtEvento(nombre, "OTRA", "Otra descripción", List.of(categoriaNueva))));

        var existentes = eventosLlamados(nombre);
        assertEquals(1, existentes.size());
        assertEquals("SG", existentes.get(0).getSigla());
        assertEquals("Descripción de Evento repetido", existentes.get(0).getDescripcion());
        assertEquals(Set.of(categoriaOriginal), Set.copyOf(existentes.get(0).getCategorias()));
    }

    @Test
    void unEventoConUnaCategoriaInexistenteNoQuedaRegistradoNiSiquieraParcialmente() throws Exception {
        String valida = categoria("Válida", null);
        String nombre = "Evento a medias " + sufijo;

        // La primera categoría existe y la segunda no: no tiene que quedar nada guardado
        assertThrows(Exception.class, () -> sistema.altaEvento(
                dtEvento(nombre, "AM", "Evento a medias", List.of(valida, "NoExiste " + sufijo))));

        assertTrue(eventosLlamados(nombre).isEmpty());
    }

    @Test
    void unEventoSinCategoriasNoQuedaRegistrado() {
        String nombre = "Evento sin categorías " + sufijo;

        assertThrows(Exception.class, () -> sistema.altaEvento(
                dtEvento(nombre, "SC", "Sin categorías", List.of())));
        assertThrows(Exception.class, () -> sistema.altaEvento(
                dtEvento(nombre, "SC", "Sin categorías", null)));

        assertTrue(eventosLlamados(nombre).isEmpty());
    }

    @Test
    void unEventoPuedeTenerUnaCategoriaYSuSubcategoria() throws Exception {
        String padre = categoria("Deporte", null);
        String hija = categoria("Maratón", padre);

        String nombre = evento("Evento con jerarquía", List.of(padre, hija));

        var creado = eventosLlamados(nombre).get(0);
        assertEquals(Set.of(padre, hija), Set.copyOf(creado.getCategorias()));
    }

    @Test
    void unEventoNuevoApareceUnaSolaVezEnElListado() throws Exception {
        String nombre = evento("Evento único", List.of(categoria("Cultura", null)));

        assertEquals(1, eventosLlamados(nombre).size());
    }

    @Test
    void dosEventosPuedenCompartirUnaCategoria() throws Exception {
        String compartida = categoria("Compartida", null);

        String primero = evento("Primer evento", List.of(compartida));
        String segundo = evento("Segundo evento", List.of(compartida));

        assertEquals(Set.of(compartida), Set.copyOf(eventosLlamados(primero).get(0).getCategorias()));
        assertEquals(Set.of(compartida), Set.copyOf(eventosLlamados(segundo).get(0).getCategorias()));
    }

    // --------------------------------------------------- ediciones de un evento

    @Test
    void unEventoRecienCreadoNoTieneEdiciones() throws Exception {
        String nombre = evento("Evento sin ediciones", List.of(categoria("Música", null)));

        assertTrue(sistema.obtenerEdicionesEvento(nombre).isEmpty());
    }

    @Test
    void consultarLasEdicionesDeUnEventoInexistenteDevuelveUnaListaVacia() {
        assertTrue(sistema.obtenerEdicionesEvento("NoExiste " + sufijo).isEmpty());
    }

    @Test
    void unaEdicionSoloApareceBajoSuEvento() throws Exception {
        String cat = categoria("Tecnología", null);
        String conEdicion = evento("Evento con edición", List.of(cat));
        String sinEdicion = evento("Evento hermano", List.of(cat));
        String organizador = organizador();
        String edicion = "Edición " + sufijo;

        assertTrue(sistema.altaEdicion(new DtEdicion(edicion, "ED", hoy.plusDays(10), hoy.plusDays(12),
                hoy, "Montevideo", "Uruguay", organizador), conEdicion));

        assertTrue(sistema.obtenerEdicionesEvento(conEdicion).stream()
                .anyMatch(e -> edicion.equals(e.getIdNombre())));
        assertFalse(sistema.obtenerEdicionesEvento(sinEdicion).stream()
                .anyMatch(e -> edicion.equals(e.getIdNombre())));
    }
}
