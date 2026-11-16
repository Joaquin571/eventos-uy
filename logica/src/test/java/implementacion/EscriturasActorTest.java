package implementacion;

import datatypes.*;
import excepciones.AccesoDenegadoException;
import interfaces.ISistema;
import org.junit.jupiter.api.Test;
import soporte.PruebaConBaseSegura;
import soporte.ImagenesPrueba;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EscriturasActorTest extends PruebaConBaseSegura {
    private final ISistema s=Fabrica.getInstance().getISistema();
    private final String sufijo=Long.toString(System.nanoTime());
    private final Actor admin=Actor.administradorLocal();
    private Actor asistente(){
        var u=new DtAsistente("eaAs"+sufijo,"Ana","as"+sufijo+"@test.uy","Apellido",LocalDate.of(2000,1,1),null);
        u.setContrasenia("clave");u.setConfirmacionContrasenia("clave");assertTrue(s.altaAsistente(u));return Actor.usuarioAutenticado(s.autenticar(u.getNickname(),"clave"));
    }
    private Actor organizador(){
        var u=new DtOrganizador("eaOr"+sufijo,"Org","or"+sufijo+"@test.uy","Descripción","");
        u.setContrasenia("clave");u.setConfirmacionContrasenia("clave");assertTrue(s.altaOrganizador(u));return Actor.usuarioAutenticado(s.autenticar(u.getNickname(),"clave"));
    }
    @Test void ningunaEscrituraProtegidaDeLaInterfazPermiteOmitirActor(){
        var nombres=List.of("altaEvento","altaEdicion","altaCategoria","aceptarRechazarEdicion","registroAEdicion","asociarImagen","cambiarContrasenia","modificarAsistente","modificarOrganizador","altaInstitucion","altaTipoRegistro","altaPatrocinio");
        for(var m:ISistema.class.getMethods())if(nombres.contains(m.getName()))
            assertTrue(List.of(m.getParameterTypes()).contains(Actor.class),m.toString());
    }
    @Test void actorAusenteNoAutorizaAunqueLosParametrosParecenValidos(){
        assertThrows(AccesoDenegadoException.class,()->s.altaCategoria("sinActor"+sufijo,null,null));
        assertThrows(AccesoDenegadoException.class,()->s.altaEvento(new DtEvento("sinActor"+sufijo,"EV","Desc",null,List.of()),null));
        assertThrows(AccesoDenegadoException.class,()->s.altaEdicion(null,"evento",null));
        assertThrows(AccesoDenegadoException.class,()->s.aceptarRechazarEdicion("edicion",true,null));
        assertThrows(AccesoDenegadoException.class,()->s.registroAEdicion("asistente","edicion","tipo",new DtRegistro(null,0),null));
        assertThrows(AccesoDenegadoException.class,()->s.asociarImagen(TipoEntidadImagen.USUARIO,"asistente",null,null));
    }
    @Test void administradorEsExplicitoYCategoriasYDecisionesNoAdmitenRolesRemotos(){
        var org=organizador();var as=asistente();
        for(var actor:List.of(org,as)){
            assertThrows(AccesoDenegadoException.class,()->s.altaCategoria("prohibida"+sufijo,null,actor));
            assertThrows(AccesoDenegadoException.class,()->s.aceptarRechazarEdicion("noexiste",true,actor));
        }
        String categoria="permitida"+sufijo;assertDoesNotThrow(()->s.altaCategoria(categoria,null,admin));
        assertThrows(AccesoDenegadoException.class,()->s.altaEvento(new DtEvento("ev"+sufijo,"EV","Desc",null,List.of(categoria)),as));
        assertDoesNotThrow(()->s.altaEvento(new DtEvento("ev"+sufijo,"EV","Desc",null,List.of(categoria)),org));
    }
    @Test void soloPropietarioOAdministradorPuedeCambiarImagenDeUsuario(){
        var as=asistente();var org=organizador();var foto=new DtImagen("image/png",ImagenesPrueba.imagen("png"));
        assertThrows(AccesoDenegadoException.class,()->s.asociarImagen(TipoEntidadImagen.USUARIO,as.nickname(),foto,org));
        assertNull(s.obtenerImagen(TipoEntidadImagen.USUARIO,as.nickname()));
        s.asociarImagen(TipoEntidadImagen.USUARIO,as.nickname(),foto,as);
        assertArrayEquals(foto.getContenido(),s.obtenerImagen(TipoEntidadImagen.USUARIO,as.nickname()).getContenido());
        assertThrows(AccesoDenegadoException.class,()->s.asociarImagen(TipoEntidadImagen.INSTITUCION,"otra",foto,as));
    }
    @Test void registroDeOtroAsistenteNoPuedeSuplantarSuIdentidad(){
        var as=asistente();var org=organizador();
        assertThrows(AccesoDenegadoException.class,()->s.registroAEdicion("otra","edicion","tipo",new DtRegistro(null,0),null,as));
        assertThrows(AccesoDenegadoException.class,()->s.registroAEdicion(as.nickname(),"edicion","tipo",new DtRegistro(null,0),org));
        assertTrue(s.obtenerRegistrosAsistente(as.nickname()).isEmpty());
    }
}
