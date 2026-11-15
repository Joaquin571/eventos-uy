package implementacion;

import datatypes.*;
import excepciones.*;
import interfaces.ISistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soporte.PruebaConBaseSegura;
import soporte.ImagenesPrueba;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PortalAutorizacionTest extends PruebaConBaseSegura {
    private final ISistema s=Fabrica.getInstance().getISistema();
    private final String sufijo=Long.toString(System.nanoTime());
    private String evento="portalEv"+sufijo, aceptada="portalAc"+sufijo, privada="portalPr"+sufijo;
    private Actor org,ajeno,asistente;
    private Actor organizador(String prefijo){String nick=prefijo+sufijo;var u=new DtOrganizador(nick,"Org",nick+"@test.uy","Descripción","");
        u.setContrasenia("clave");u.setConfirmacionContrasenia("clave");assertTrue(s.altaOrganizador(u));return Actor.usuarioAutenticado(s.autenticar(nick,"clave"));}
    @BeforeEach void preparar() throws Exception {
        org=organizador("portalOrg");ajeno=organizador("portalAj");
        String nick="portalAs"+sufijo;var u=new DtAsistente(nick,"As",nick+"@test.uy","Apellido",LocalDate.of(2000,1,1),null);
        u.setContrasenia("clave");u.setConfirmacionContrasenia("clave");assertTrue(s.altaAsistente(u));asistente=Actor.usuarioAutenticado(s.autenticar(nick,"clave"));
        String cat="portalCat"+sufijo;s.altaCategoria(cat,null);s.altaEvento(new DtEvento(evento,"EV","Descripción",null,List.of(cat)),null,org);
        for(String nombre:List.of(aceptada,privada)){s.altaEdicion(new DtEdicion(nombre,"ED",LocalDate.now(),LocalDate.now(),null,"Ciudad","País",org.nickname()),evento,null,org);
            s.altaTipoRegistro(new DtTipoRegistro("General","Entrada",10,10),nombre,org);}
        s.aceptarRechazarEdicion(aceptada,true);
    }
    @Test void perfilNoEntregaEdicionesPrivadasNiRegistrosAjenos(){
        assertEquals(1,s.consultarPerfil(org.nickname(),null).ediciones().size());
        assertEquals(2,s.consultarPerfil(org.nickname(),org).ediciones().size());
        assertFalse(s.consultarPerfil(org.nickname(),ajeno).propio());
        assertTrue(s.consultarPerfil(asistente.nickname(),null).registros().isEmpty());
    }
    @Test void seleccionYDetalleSeAutorizanSinDependerDelServlet(){
        for(Actor actor:new Actor[]{null,ajeno,asistente})assertThrows(NoEncontradoException.class,()->s.consultarEdiciones("",privada,actor));
        assertEquals(privada,s.consultarEdiciones("",privada,org).edicion().getIdNombre());
        assertThrows(NoEncontradoException.class,()->s.consultarEdiciones("otro",privada,org));
        assertTrue(s.consultarDetalleEdicion(aceptada,org).propietario());
        assertFalse(s.consultarDetalleEdicion(aceptada,null).propietario());
        assertTrue(s.consultarDetalleEdicion(aceptada,asistente).puedeRegistrarse());
    }
    @Test void registroDerivaLaIdentidadYCalculaCostoEnSistema(){
        assertThrows(AccesoDenegadoException.class,()->s.registrarAutorizado(aceptada,"General","general",null,org));
        assertThrows(AccesoDenegadoException.class,()->s.registrarAutorizado(aceptada,"General","general",null,null));
        assertTrue(s.registrarAutorizado(aceptada,"General","general","ignorado",asistente));
        var detalle=s.consultarDetalleEdicion(aceptada,asistente);assertEquals(10,detalle.registroPropio().getCosto());assertFalse(detalle.puedeRegistrarse());
        assertEquals(asistente.nickname(),detalle.registroPropio().getNicknameAsistente());
        assertEquals(1,s.consultarRegistros(aceptada,"",org).registros().size());
        assertThrows(AccesoDenegadoException.class,()->s.consultarRegistros(aceptada,"",ajeno));
    }
    @Test void modalidadYCodigoSeValidanAntesDeRegistrar(){
        assertThrows(IllegalArgumentException.class,()->s.registrarAutorizado(aceptada,"General","otra",null,asistente));
        assertThrows(IllegalArgumentException.class,()->s.registrarAutorizado(aceptada,"General","codigo","",asistente));
        assertThrows(NoEncontradoException.class,()->s.formularioRegistro("",privada,"General",asistente));
        assertTrue(s.obtenerRegistrosAsistente(asistente.nickname()).isEmpty());
    }
    @Test void imagenDeEdicionPrivadaNoSeEntregaATerceros(){
        var foto=new DtImagen("image/png",ImagenesPrueba.imagen("png"));s.asociarImagen(TipoEntidadImagen.EDICION,privada,foto);
        assertThrows(NoEncontradoException.class,()->s.obtenerImagenVisible(TipoEntidadImagen.EDICION,privada,null));
        assertArrayEquals(foto.getContenido(),s.obtenerImagenVisible(TipoEntidadImagen.EDICION,privada,org).getContenido());
    }
    @Test void escriturasDeEventoYEdicionExigenOrganizadorYRespetanIdentidad(){
        assertThrows(AccesoDenegadoException.class,()->s.altaEvento(new DtEvento("otro","EV","Desc",null,List.of()),null,asistente));
        var dt=new DtEdicion("falsa"+sufijo,"ED",LocalDate.now(),LocalDate.now(),null,"Ciudad","País",org.nickname());
        assertThrows(AccesoDenegadoException.class,()->s.altaEdicion(dt,evento,null,ajeno));
    }
    @Test void disponibilidadNoReemplazaLaValidacionDefinitiva(){
        assertFalse(s.disponibleNickname(org.nickname()));assertFalse(s.disponibleCorreo((org.nickname()+"@test.uy").toUpperCase()));
        assertTrue(s.disponibleNickname("libre"+sufijo));assertFalse(s.disponibleNickname(" "));assertFalse(s.disponibleCorreo("invalido"));
    }
    @Test void imagenInvalidaNoDejaEventoNiEdicion(){
        var mala=new DtImagen("image/png",new byte[]{1});String nuevo="fallido"+sufijo;
        assertThrows(IllegalArgumentException.class,()->s.altaEvento(new DtEvento(nuevo,"EV","Desc",null,List.of("portalCat"+sufijo)),mala,org));
        assertTrue(s.listarEventos().stream().noneMatch(e->e.getNombre().equals(nuevo)));
        assertThrows(IllegalArgumentException.class,()->s.altaEdicion(new DtEdicion(nuevo,"ED",LocalDate.now(),LocalDate.now(),null,"Ciudad","País",org.nickname()),evento,mala,org));
        assertThrows(NoEncontradoException.class,()->s.consultarEdiciones("",nuevo,org));
    }
}
