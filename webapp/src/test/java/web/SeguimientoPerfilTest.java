package web;

import contratos.ServicioPortal;
import contratos.dto.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SeguimientoPerfilTest {
    @Test void propioNoMuestraAccionYAjenoAutenticadoSi() throws Exception {
        var e=new Escenario();e.propio=true;e.autenticar();
        new UsuarioServlet().doGet(e.req,e.resp);assertEquals(false,e.atributos.get("puedeSeguir"));
        e.propio=false;new UsuarioServlet().doGet(e.req,e.resp);assertEquals(true,e.atributos.get("puedeSeguir"));
        assertSame(e.relaciones,e.atributos.get("relaciones"));
    }
    @Test void visitantePuedeConsultarPeroNoModificar() throws Exception {
        var e=new Escenario();new UsuarioServlet().doGet(e.req,e.resp);
        assertEquals(false,e.atributos.get("puedeSeguir"));
        e.post("seguir");new UsuarioServlet().doPost(e.req,e.resp);
        assertEquals(403,e.estado);assertEquals(0,e.cambios);
    }
    @Test void postActualizaEstadoPorSoapYRedirigeAlMismoPerfil() throws Exception {
        var e=new Escenario();e.autenticar();e.post("seguir");
        new UsuarioServlet().doPost(e.req,e.resp);
        assertTrue(e.relaciones.isSiguiendo());assertEquals("/app/usuario?nick=dest",e.redirect);
        new UsuarioServlet().doGet(e.req,e.resp);
        assertTrue(((DtRelacionesUsuario)e.atributos.get("relaciones")).isSiguiendo());
        e.post("dejar-de-seguir");new UsuarioServlet().doPost(e.req,e.resp);
        assertFalse(e.relaciones.isSiguiendo());assertEquals(2,e.cambios);
    }
    @Test void csrfInvalidoYAccionDesconocidaNoInvocanCentral() throws Exception {
        var e=new Escenario();e.autenticar();e.post("seguir");e.token="incorrecto";
        new UsuarioServlet().doPost(e.req,e.resp);assertEquals(403,e.estado);assertEquals(0,e.cambios);
        e.post("desconocida");new UsuarioServlet().doPost(e.req,e.resp);assertEquals(400,e.estado);assertEquals(0,e.cambios);
    }
    @Test void formularioAjenoPreparaTokenCsrf() throws Exception {
        var e=new Escenario();e.autenticar();
        new UsuarioServlet().doGet(e.req,e.resp);
        assertInstanceOf(String.class,e.atributos.get("csrfToken"));
    }
    private static class Escenario {
        Map<String,Object> sesion=new HashMap<>(),atributos=new HashMap<>();
        boolean propio;int cambios,estado;String accion,token,redirect;
        DtRelacionesUsuario relaciones=new DtRelacionesUsuario();
        void autenticar(){sesion.put(SesionWeb.CREDENCIAL,"token-fixture");}
        void post(String valor){accion=valor;Csrf.prepararFormulario(req);token=(String)atributos.get("csrfToken");}
        ServicioPortal portal=CsrfTest.doble(ServicioPortal.class,(p,m,a)->switch(m.getName()) {
            case "consultarPerfil"->{var dto=new DtPerfilUsuario();dto.setUsuario(new DtOrganizador("dest","Destino","d@test.uy","Desc",""));dto.setPropio(propio);yield dto;}
            case "consultarRelaciones"->relaciones;
            case "seguirUsuario","dejarDeSeguirUsuario"->{assertEquals("dest",a[0]);cambios++;relaciones.setSiguiendo(m.getName().equals("seguirUsuario"));yield null;}
            default->throw new AssertionError(m.getName());
        });
        HttpSession session=CsrfTest.doble(HttpSession.class,(p,m,a)->switch(m.getName()) {
            case "getAttribute"->sesion.get(a[0]);
            case "setAttribute"->{sesion.put((String)a[0],a[1]);yield null;}
            default->null;
        });
        ServletContext contexto=CsrfTest.doble(ServletContext.class,(p,m,a)->portal);
        HttpServletRequest req=CsrfTest.doble(HttpServletRequest.class,(p,m,a)->switch(m.getName()) {
            case "getSession"->session;
            case "getServletContext"->contexto;
            case "getContextPath"->"/app";
            case "getMethod"->"GET";
            case "getParameter"->a[0].equals("nick")?"dest":accion;
            case "getParameterValues"->new String[]{token};
            case "getQueryString"->null;
            case "getAttribute"->atributos.get(a[0]);
            case "setAttribute"->{atributos.put((String)a[0],a[1]);yield null;}
            case "getRequestDispatcher"->CsrfTest.doble(RequestDispatcher.class,(o,n,v)->null);
            default->throw new AssertionError(m.getName());
        });
        HttpServletResponse resp=CsrfTest.doble(HttpServletResponse.class,(p,m,a)->switch(m.getName()) {
            case "sendRedirect"->{redirect=(String)a[0];estado=302;yield null;}
            case "sendError"->{estado=(int)a[0];yield null;}
            default->null;
        });
    }
}
