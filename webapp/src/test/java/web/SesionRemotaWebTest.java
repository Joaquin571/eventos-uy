package web;

import contratos.ServicioPortal;
import contratos.dto.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import web.remoto.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SesionRemotaWebTest {
    @Test void loginRenuevaSesionYGuardaCredencialSinExponerlaALaVista()throws Exception {
        var e=new Escenario();e.sesion.put("anterior","dato");
        new LoginServlet().doPost(e.req,e.resp);
        assertEquals(1,e.invalidaciones);assertEquals(302,e.estado);assertFalse(e.sesion.containsKey("anterior"));
        assertEquals("token-sintetico",e.sesion.get(SesionWeb.CREDENCIAL));
        var usuario=(DtUsuario)e.sesion.get("usuario");assertEquals("org",usuario.getNickname());assertInstanceOf(DtOrganizador.class,usuario);
        assertNull(usuario.getContrasenia());assertNull(usuario.getCorreoElectronico());assertFalse(e.atributos.toString().contains("token-sintetico"));
    }
    @Test void credencialesInvalidasNoCreanSesionAutenticadaNiDevuelvenClave()throws Exception {
        var e=new Escenario();e.falloLogin=true;new LoginServlet().doPost(e.req,e.resp);
        assertFalse(e.sesion.containsKey("usuario"));assertFalse(e.sesion.containsKey(SesionWeb.CREDENCIAL));
        assertTrue(e.forward);assertEquals("id",e.atributos.get("identificador"));assertFalse(e.atributos.toString().contains("clave-sintetica"));
    }
    @Test void logoutRevocaP03AntesDeEliminarLaSesionLocal()throws Exception {
        var e=new Escenario();e.sesion.put(SesionWeb.CREDENCIAL,"token-sintetico");new LogoutServlet().doPost(e.req,e.resp);
        assertEquals(1,e.revocaciones);assertEquals(1,e.invalidaciones);assertTrue(e.sesion.isEmpty());assertEquals(302,e.estado);
    }
    @Test void falloDeTransporteAlCerrarIgualmenteEliminaSesionLocal()throws Exception {
        var e=new Escenario();e.sesion.put(SesionWeb.CREDENCIAL,"token-sintetico");e.falloCierre=true;
        assertThrows(ErrorCentral.class,()->new LogoutServlet().doPost(e.req,e.resp));assertTrue(e.sesion.isEmpty());assertEquals(1,e.invalidaciones);
    }
    @Test void sesionP03ExpiradaInvalidaCookieLocalYNoEjecutaControlador()throws Exception {
        var e=new Escenario();e.sesion.put(SesionWeb.CREDENCIAL,"token-sintetico");
        new FiltroAcceso().doFilter(e.req,e.resp,(r,s)->fail("No continuar con sesión expirada"));
        assertTrue(e.sesion.isEmpty());assertEquals(1,e.invalidaciones);assertEquals(302,e.estado);
        assertEquals("/test/login",e.redireccion);
    }
    @Test void sesionMovilExpiradaVuelveAlLoginMovil()throws Exception {
        var e=new Escenario();e.ruta="/movil/eventos";e.sesion.put(SesionWeb.CREDENCIAL,"token-sintetico");
        new FiltroAcceso().doFilter(e.req,e.resp,(r,s)->fail("No continuar con sesión expirada"));
        assertTrue(e.sesion.isEmpty());assertEquals(1,e.invalidaciones);
        assertEquals("/test/movil/login?motivo=vencida",e.redireccion);
    }
    private static class Escenario {
        final Map<String,Object> sesion=new HashMap<>(),atributos=new HashMap<>();
        int invalidaciones,revocaciones,estado=200;boolean falloLogin,falloCierre,forward;
        String ruta="/usuario",redireccion;
        final ServicioSesiones remoto=new ServicioSesiones(){
            public ClienteSesionesCentral.Inicio iniciar(String id,String clave){assertEquals("id",id);assertEquals("clave-sintetica",clave);if(falloLogin)throw new IllegalArgumentException("Credenciales inválidas.");return new ClienteSesionesCentral.Inicio("org","Organización","ORGANIZADOR","token-sintetico");}
            public void cerrar(String credencial){assertEquals("token-sintetico",credencial);assertTrue(sesion.containsKey(SesionWeb.CREDENCIAL));revocaciones++;if(falloCierre)throw new ErrorCentral("SERVICIO_NO_DISPONIBLE","Central no disponible.");}
        };
        final ServicioPortal portal=CsrfTest.doble(ServicioPortal.class,(p,m,a)->{assertEquals("usuarioActual",m.getName());throw new ErrorCentral("SESION_INVALIDA","Sesión expirada");});
        final ServletContext contexto=CsrfTest.doble(ServletContext.class,(p,m,a)->a[0].equals("sesionesCentral")?remoto:portal);
        final HttpSession session=CsrfTest.doble(HttpSession.class,(p,m,a)->switch(m.getName()){
            case "getAttribute"->sesion.get(a[0]);
            case "setAttribute"->{sesion.put((String)a[0],a[1]);yield null;}
            case "invalidate"->{invalidaciones++;sesion.clear();yield null;}
            default->throw new AssertionError(m.getName());
        });
        final HttpServletRequest req=CsrfTest.doble(HttpServletRequest.class,(p,m,a)->switch(m.getName()){
            case "getSession"->session;
            case "getServletContext"->contexto;
            case "getParameter"->a[0].equals("identificador")?"id":"clave-sintetica";
            case "getMethod"->"GET";
            case "getServletPath"->ruta;
            case "getContextPath"->"/test";
            case "setCharacterEncoding"->null;
            case "getAttribute"->atributos.get(a[0]);
            case "setAttribute"->{atributos.put((String)a[0],a[1]);yield null;}
            case "getRequestDispatcher"->CsrfTest.doble(RequestDispatcher.class,(o,n,v)->{forward=true;return null;});
            default->throw new AssertionError(m.getName());
        });
        final HttpServletResponse resp=CsrfTest.doble(HttpServletResponse.class,(p,m,a)->switch(m.getName()){
            case "sendRedirect"->{estado=302;redireccion=(String)a[0];yield null;}
            case "sendError","setStatus"->{estado=(Integer)a[0];yield null;}
            case "setHeader"->null;
            default->throw new AssertionError(m.getName());
        });
    }
}
