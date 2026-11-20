package web.soap;
import contratos.ConversorDatos;
import contratos.dto.*;
import jakarta.xml.bind.JAXBContext;
import org.junit.jupiter.api.Test;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConversorContratoTest {
    @Test void entradaSeparaCredencialesDeDatosDePerfil(){
        var usuario=new DtAsistente("ana","Ana ñ","ana@test.uy","Pérez",LocalDate.of(2000,2,29),null);
        usuario.setContrasenia("NO-EN-PERFIL");var entrada=new EntradaUsuario();entrada.setUsuario(usuario);
        entrada.setContrasenia("clave-entrada");entrada.setConfirmacion("clave-entrada");
        var generado=ConversorDatos.convertir(entrada,web.soap.generado.portal.EntradaUsuario.class);
        assertEquals("clave-entrada",generado.getContrasenia());
        var vuelta=ConversorDatos.convertir(generado,EntradaUsuario.class);
        assertInstanceOf(DtAsistente.class,vuelta.getUsuario());assertNull(vuelta.getUsuario().getContrasenia());
        assertEquals(LocalDate.of(2000,2,29),((DtAsistente)vuelta.getUsuario()).getFechaNacimiento());
    }
    @Test void coleccionesEnumeradosFechasNulasYBytesSeConservan(){
        var opciones=new DtOpcionesAltaPatrocinio();opciones.setEventos(List.of(new DtEvento("Evento","EV","Descripción",null,List.of("Música"))));
        var ed=new DtEdicion("Edición","ED",null,null,null,"Ciudad","País","org");ed.setEstado(EstadoEdicion.RECHAZADA);opciones.setEdiciones(List.of(ed));
        var generado=ConversorDatos.convertir(opciones,web.soap.generado.portal.DtOpcionesAltaPatrocinio.class);
        var vuelta=ConversorDatos.convertir(generado,DtOpcionesAltaPatrocinio.class);
        assertEquals(EstadoEdicion.RECHAZADA,vuelta.getEdiciones().getFirst().getEstado());assertNull(vuelta.getEdiciones().getFirst().getFechaFin());
        assertEquals(List.of("Música"),new java.util.ArrayList<>(vuelta.getEventos().getFirst().getCategorias()));
        var foto=new DtImagen("image/png",new byte[]{1,2,3});
        assertArrayEquals(foto.getContenido(),ConversorDatos.convertir(ConversorDatos.convertir(foto,web.soap.generado.portal.DtImagen.class),DtImagen.class).getContenido());
    }
    @Test void xmlDeSalidaNoTieneCamposDeContraseniaAunqueElObjetoLosContenga() throws Exception {
        var usuario=new DtOrganizador("org","Nombre","org@test.uy","Desc","");usuario.setContrasenia("SECRETO-NO-SERIALIZABLE");usuario.setConfirmacionContrasenia("SECRETO-NO-SERIALIZABLE");
        var salida=new StringWriter();var marshal=JAXBContext.newInstance(DtUsuario.class,DtOrganizador.class).createMarshaller();
        marshal.marshal(new jakarta.xml.bind.JAXBElement<>(new javax.xml.namespace.QName("usuario"),DtUsuario.class,usuario),salida);
        assertFalse(salida.toString().contains("contrasenia"));assertFalse(salida.toString().contains("SECRETO"));
    }
    @Test void perfilTransportaBooleanosYRegistrosSinCodigoDePatrocinio(){
        var perfil=new DtPerfilUsuario();perfil.setUsuario(new DtOrganizador("org","Nombre","org@test.uy","Desc",""));perfil.setPropio(true);
        perfil.setRegistros(List.of(new DtRegistro(LocalDate.of(2026,11,14),0,"General","Edición","ana",true)));
        var copia=ConversorDatos.convertir(ConversorDatos.convertir(perfil,web.soap.generado.portal.DtPerfilUsuario.class),DtPerfilUsuario.class);
        assertTrue(copia.isPropio());assertTrue(copia.getRegistros().getFirst().isUsoCodigo());assertEquals("ana",copia.getRegistros().getFirst().getNicknameAsistente());
    }
}
