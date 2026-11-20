package implementacion;

import datatypes.*;
import excepciones.AccesoDenegadoException;
import interfaces.ISistema;
import org.junit.jupiter.api.*;
import soporte.PruebaConBaseSegura;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AutenticacionMovilTest extends PruebaConBaseSegura {
    private final ISistema sistema = Fabrica.getInstance().getISistema();
    private String asis, org;
    @BeforeEach void preparar() {
        asis="movAs"+UUID.randomUUID(); org="movOrg"+UUID.randomUUID();
        var a=new DtAsistente(asis,"Asistente",asis+"@test.uy","Apellido",LocalDate.of(2000,1,1),null);
        var o=new DtOrganizador(org,"Organizador",org+"@test.uy","Descripcion","");
        for(var u:new DtUsuario[]{a,o}) {u.setContrasenia("clave");u.setConfirmacionContrasenia("clave");}
        assertTrue(sistema.altaAsistente(a)); assertTrue(sistema.altaOrganizador(o));
    }
    @Test void asistenteIngresaConNicknameOCorreo() {
        assertEquals(asis,sistema.autenticarMovil(asis,"clave").getNickname());
        assertEquals(asis,sistema.autenticarMovil(asis+"@test.uy","clave").getNickname());
        assertEquals(asis,sistema.consultarUsuarioMovil(Actor.usuarioAutenticado(sistema.autenticar(asis,"clave"))).getNickname());
    }
    @Test void credencialesInvalidasYOrganizadorNoIngresan() {
        assertThrows(IllegalArgumentException.class,()->sistema.autenticarMovil(asis,"incorrecta"));
        assertThrows(IllegalArgumentException.class,()->sistema.autenticarMovil("inexistente","clave"));
        assertThrows(AccesoDenegadoException.class,()->sistema.autenticarMovil(org,"clave"));
        assertInstanceOf(DtOrganizador.class,sistema.autenticar(org,"clave"));
    }
    @Test void centralCompruebaRolPersistidoAunqueActorDigaAsistente() {
        var fingido=new DtAsistente(org,"", "", "",null,null);
        assertThrows(AccesoDenegadoException.class,()->sistema.consultarUsuarioMovil(Actor.usuarioAutenticado(fingido)));
        assertThrows(AccesoDenegadoException.class,()->sistema.consultarUsuarioMovil(null));
        assertThrows(AccesoDenegadoException.class,()->sistema.consultarUsuarioMovil(Actor.administradorLocal()));
        var inexistente = new DtAsistente("sin-sesion-" + UUID.randomUUID(), "", "", "", null, null);
        assertThrows(AccesoDenegadoException.class, () -> sistema.consultarUsuarioMovil(Actor.usuarioAutenticado(inexistente)));
    }
}
