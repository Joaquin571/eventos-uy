package web.remoto;

import datatypes.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConversorUsuariosTest {
    @Test void conservaSubtipoYFechasDelAsistenteEnAmbasDirecciones() {
        DtAsistente original = new DtAsistente("ana", "Ana", "ana@test.uy", "Pérez",
                LocalDate.of(2000, 2, 29), "UTEC", LocalDate.of(2026, 11, 14));
        original.setContrasenia("clave"); original.setConfirmacionContrasenia("clave");
        var soap = (web.remoto.generado.DtAsistente) ConversorUsuarios.convertir(original, web.remoto.generado.DtUsuario.class);
        assertEquals("2000-02-29", soap.getFechaNacimiento());
        DtAsistente copia = (DtAsistente) ConversorUsuarios.convertir(soap, DtUsuario.class);
        assertEquals(original.getNickname(), copia.getNickname());
        assertEquals(original.getFechaAlta(), copia.getFechaAlta());
        assertEquals(original.getFechaNacimiento(), copia.getFechaNacimiento());
        assertEquals(original.getContrasenia(), copia.getContrasenia());
    }
    @Test void conservaColeccionesEnumeradosYFechasNulas() {
        DtFormularioPatrocinio original = new DtFormularioPatrocinio();
        original.setEventos(List.of(new DtEvento("Evento", "EV", "Descripción", null, List.of("Música", "Arte"))));
        DtEdicion edicion = new DtEdicion("Edición", "ED", null, null, null, "San José", "Uruguay", "org");
        edicion.setEstado(EstadoEdicion.RECHAZADA);
        original.setEdiciones(List.of(edicion));
        original.setTipos(List.of(new DtTipoRegistro("General", "Entrada", 10.5f, 30)));
        var soap = ConversorUsuarios.convertir(original, web.remoto.generado.DtFormularioPatrocinio.class);
        DtFormularioPatrocinio copia = (DtFormularioPatrocinio) ConversorUsuarios.convertir(soap, DtFormularioPatrocinio.class);
        assertEquals(List.of("Música", "Arte"), copia.getEventos().getFirst().getCategorias());
        assertEquals(EstadoEdicion.RECHAZADA, copia.getEdiciones().getFirst().getEstado());
        assertNull(copia.getEdiciones().getFirst().getFechaFin());
        assertEquals(10.5f, copia.getTipos().getFirst().getCosto());
    }
    @Test void conviertePerfilConRegistrosYBooleanos() {
        DtPerfilUsuario perfil = new DtPerfilUsuario();
        perfil.setUsuario(new DtOrganizador("org", "Organizador", "org@test.uy", "Desc", ""));
        perfil.setPropio(true);
        perfil.setRegistros(List.of(new DtRegistro(LocalDate.of(2026, 11, 14), 10, "General", "Edición", "ana", true)));
        var soap = ConversorUsuarios.convertir(perfil, web.remoto.generado.DtPerfilUsuario.class);
        var copia = (DtPerfilUsuario) ConversorUsuarios.convertir(soap, DtPerfilUsuario.class);
        assertTrue(copia.isPropio());
        assertInstanceOf(DtOrganizador.class, copia.getUsuario());
        assertTrue(copia.getRegistros().getFirst().isUsoCodigo());
    }
    @Test void convierteBytesSinAlterarlosYConservaCodigoNulo() {
        DtImagen imagen = new DtImagen("image/png", new byte[]{1, 2, 3});
        var soap = ConversorUsuarios.convertir(imagen, web.remoto.generado.DtImagen.class);
        assertArrayEquals(imagen.getContenido(), ((DtImagen) ConversorUsuarios.convertir(soap, DtImagen.class)).getContenido());
        DtPatrocinio p = new DtPatrocinio(null, 100, 1, null, clases.NivelPatrocinio.values()[0], "UTEC", "ED", "General");
        var pSoap = ConversorUsuarios.convertir(p, web.remoto.generado.DtPatrocinio.class);
        var copia = (DtPatrocinio) ConversorUsuarios.convertir(pSoap, DtPatrocinio.class);
        assertNull(copia.getCodigoPatrocinio());
        assertEquals(p.getNivel(), copia.getNivel());
    }
}
