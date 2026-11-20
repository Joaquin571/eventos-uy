package implementacion;

import datatypes.*;
import interfaces.ISistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConsultaTipoRegistroTest extends soporte.PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = String.valueOf(System.nanoTime());
    private final String primera = "Primera consulta " + sufijo, segunda = "Segunda consulta " + sufijo;

    @BeforeEach
    void prepararSoloEnBaseDePruebas() throws Exception {
        String nick = "orgTipo" + sufijo;
        DtOrganizador d = new DtOrganizador(nick,"Organizador",nick+"@test.uy","Descripción","");
        d.setContrasenia("clave");d.setConfirmacionContrasenia("clave");assertTrue(sistema.altaOrganizador(d));
        String cat = "catTipo"+sufijo, evento="Evento tipos "+sufijo;
        sistema.altaCategoria(cat,null, datatypes.Actor.administradorLocal());LocalDate hoy=LocalDate.now();
        assertTrue(sistema.altaEvento(new DtEvento(evento,"EV","Descripción",hoy,List.of(cat)), datatypes.Actor.administradorLocal()));
        for (String nombre : List.of(primera,segunda)) {
            assertTrue(sistema.altaEdicion(new DtEdicion(nombre,"ED",hoy,hoy,hoy,"Ciudad","País",nick),evento, datatypes.Actor.administradorLocal()));
        }
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General","Primera descripción",100,25),primera, Actor.administradorLocal()));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("General","Segunda descripción",200,50),segunda, Actor.administradorLocal()));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("Exclusivo","Solo en segunda",0,5),segunda, Actor.administradorLocal()));
    }

    @Test
    void mismoNombreEnDistintasEdicionesConservaSusDatos() {
        DtTipoRegistro a=sistema.consultarTipoRegistro(primera,"General"),b=sistema.consultarTipoRegistro(segunda,"General");
        assertEquals("General",a.getIdNombre());assertEquals("Primera descripción",a.getDescripcion());
        assertEquals(100f,a.getCosto());assertEquals(25,a.getCupo());assertEquals(200f,b.getCosto());assertEquals(50,b.getCupo());
    }

    @Test
    void tiposAjenosOInexistentesDevuelvenNull() {
        assertNull(sistema.consultarTipoRegistro(primera,"Exclusivo"));
        assertEquals("General", sistema.consultarTipoRegistro(primera,"general").getIdNombre());
        assertNull(sistema.consultarTipoRegistro("NoExiste"+sufijo,"General"));
    }

    @Test
    void listadoSoloIncluyeLosTiposDeLaEdicion() {
        assertEquals(List.of("General"),sistema.obtenerTiposRegistroEdicion(primera).stream().map(DtTipoRegistro::getIdNombre).toList());
        assertEquals(2,sistema.obtenerTiposRegistroEdicion(segunda).size());
    }

    @Test
    void propietarioYAdministradorPuedenDarDeAltaConLaMismaOperacion() {
        var propietario = Actor.usuarioAutenticado(sistema.consultarUsuario("orgTipo" + sufijo));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("Propio", "Descripción", 0, 1),
                primera, propietario));
        assertTrue(sistema.altaTipoRegistro(new DtTipoRegistro("Local", "Descripción", 0, 1),
                primera, Actor.administradorLocal()));
        assertNotNull(sistema.consultarTipoRegistro(primera, "Propio"));
        assertNotNull(sistema.consultarTipoRegistro(primera, "Local"));
    }

    @Test
    void organizadorAjenoSeRechazaAntesDeConsultarDuplicadosYSinInsertar() {
        String nick = "orgAjeno" + sufijo;
        var organizador = new DtOrganizador(nick, "Ajeno", nick + "@test.uy", "Descripción", "");
        organizador.setContrasenia("clave");
        organizador.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaOrganizador(organizador));
        var actor = Actor.usuarioAutenticado(sistema.autenticar(nick, "clave"));

        for (String nombre : List.of("Ajeno", "GENERAL")) {
            var error = assertThrows(IllegalArgumentException.class, () -> sistema.altaTipoRegistro(
                    new DtTipoRegistro(nombre, "Descripción", 0, 1), primera, actor));
            assertEquals("La edición no pertenece al organizador.", error.getMessage());
        }
        assertNull(sistema.consultarTipoRegistro(primera, "Ajeno"));
        assertEquals(List.of("General"), sistema.obtenerTiposRegistroEdicion(primera)
                .stream().map(DtTipoRegistro::getIdNombre).toList());
    }

    @Test
    void asistenteYActorAusenteSeRechazanSinInsertar() {
        String nick = "asTipo" + sufijo;
        var asistente = new DtAsistente(nick, "Asistente", nick + "@test.uy", "Apellido",
                LocalDate.of(2000, 1, 1), null);
        asistente.setContrasenia("clave");
        asistente.setConfirmacionContrasenia("clave");
        assertTrue(sistema.altaAsistente(asistente));
        var actor = Actor.usuarioAutenticado(sistema.autenticar(nick, "clave"));
        var tipo = new DtTipoRegistro("Denegado", "Descripción", 0, 1);

        assertThrows(IllegalArgumentException.class, () -> sistema.altaTipoRegistro(tipo, primera, actor));
        assertThrows(IllegalArgumentException.class, () -> sistema.altaTipoRegistro(tipo, primera, null));
        assertNull(sistema.consultarTipoRegistro(primera, "Denegado"));
    }
}
