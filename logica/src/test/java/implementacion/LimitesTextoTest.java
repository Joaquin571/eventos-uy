package implementacion;

import clases.NivelPatrocinio;
import datatypes.*;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Límites del esquema real, comprobados antes de cualquier persistencia parcial. */
class LimitesTextoTest extends soporte.PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private final String sufijo = Long.toString(System.nanoTime());
    private final String excesivo = "x".repeat(256);

    private DtOrganizador organizador(String nick, String nombre, String descripcion, String sitio) {
        DtOrganizador dt = new DtOrganizador(nick, nombre, nick + "@test.uy", descripcion, sitio);
        dt.setContrasenia("original"); dt.setConfirmacionContrasenia("original");
        return dt;
    }

    @Test void limiteExactoDeNombreSePersisteYElExcesoNoCreaUsuario() {
        String nick = "lim" + sufijo;
        assertTrue(sistema.altaOrganizador(organizador(nick, "n".repeat(255), "Descripción", null)));
        assertEquals("n".repeat(255), sistema.consultarUsuario(nick).getNombre());
        String otro = "otro" + sufijo;
        assertThrows(IllegalArgumentException.class, () -> sistema.altaOrganizador(organizador(otro, excesivo, "D", null)));
        assertNull(sistema.consultarUsuario(otro));
    }

    @Test void caracteresUnicodeSeCuentanComoEnPostgresql() {
        String nick = "unicode" + sufijo;
        String nombre = "😀".repeat(255);
        assertTrue(sistema.altaOrganizador(organizador(nick, nombre, "D", null)));
        assertEquals(nombre, sistema.consultarUsuario(nick).getNombre());
        assertThrows(IllegalArgumentException.class, () -> sistema.altaOrganizador(
                organizador("unicodeEx" + sufijo, nombre + "😀", "D", null)));
    }

    @Test void textoNuloBinarioSeRechazaAntesDePostgresql() {
        String nick = "nul" + sufijo;
        assertThrows(IllegalArgumentException.class, () -> sistema.altaOrganizador(organizador(nick, "a\0b", "D", null)));
        assertNull(sistema.consultarUsuario(nick));
    }

    @Test void perfilExcesivoNoCambiaDatosNiContrasenia() {
        String nick = "perfil" + sufijo;
        assertTrue(sistema.altaOrganizador(organizador(nick, "Original", "D", null)));
        for (DtOrganizador dt : List.of(organizador(nick, excesivo, "D", null),
                organizador(nick, "Nuevo", excesivo, null), organizador(nick, "Nuevo", "D", excesivo))) {
            dt.setContrasenia("nueva"); dt.setConfirmacionContrasenia("nueva");
            assertThrows(IllegalArgumentException.class, () -> sistema.modificarOrganizador(dt));
            assertEquals("Original", sistema.consultarUsuario(nick).getNombre());
            assertNotNull(sistema.autenticar(nick, "original"));
            assertThrows(IllegalArgumentException.class, () -> sistema.autenticar(nick, "nueva"));
        }
    }

    @Test void apellidoExcesivoNoCreaAsistente() {
        String nick = "apellido" + sufijo;
        DtAsistente dt = new DtAsistente(nick, "N", nick + "@test.uy", excesivo, LocalDate.of(2000,1,1), null);
        dt.setContrasenia("clave"); dt.setConfirmacionContrasenia("clave");
        assertThrows(IllegalArgumentException.class, () -> sistema.altaAsistente(dt));
        assertNull(sistema.consultarUsuario(nick));
    }

    @Test void institucionValidaTodosSusTextosAntesDeInsertar() {
        String nombre = "inst" + sufijo;
        for (DtInstitucion dt : List.of(new DtInstitucion(excesivo,"D",null),
                new DtInstitucion(nombre,excesivo,null),new DtInstitucion(nombre,"D",excesivo))) {
            assertThrows(IllegalArgumentException.class, () -> sistema.altaInstitucion(dt));
        }
        assertFalse(sistema.existeInstitucion(nombre));
    }

    @Test void eventoYEdicionRechazanTextoAntesDeResolverReferencias() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaEvento(new DtEvento(
                "ev" + sufijo, excesivo, "D", null, List.of("inexistente"))));
        assertThrows(IllegalArgumentException.class, () -> sistema.altaEdicion(new DtEdicion(
                "ed" + sufijo,"ED",LocalDate.now(),LocalDate.now(),null,excesivo,"País","org"),"evento"));
    }

    @Test void tipoYCodigoExcesivosSeRechazanSinInsertar() {
        assertThrows(IllegalArgumentException.class, () -> sistema.altaTipoRegistro(new DtTipoRegistro(excesivo,"D",0,1),"edición"));
        assertThrows(IllegalArgumentException.class, () -> sistema.altaPatrocinio(new DtPatrocinio(
                null,100,0,excesivo,NivelPatrocinio.ORO,"institución","edición","tipo")));
        assertNull(sistema.consultarPatrocinio(excesivo));
    }
}
