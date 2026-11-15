package web;

import contratos.ServicioPortal;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DisponibilidadUsuarioTest {
    @Test void delegaDisponibilidadEnCentralYRespondeJsonSinReflejarEntrada()throws Exception {
        for(String campo:List.of("nickname","correo"))for(boolean disponible:List.of(true,false)){
            var e=new Escenario(campo,disponible);new DisponibilidadUsuarioServlet().doGet(e.req,e.resp);
            assertEquals(1,e.consultas);assertEquals(200,e.estado);assertEquals("{\"disponible\":"+disponible+"}",e.cuerpo.toString());
            assertEquals("no-store",e.headers.get("Cache-Control"));assertFalse(e.cuerpo.toString().contains("<script>"));
        }
    }
    @Test void campoDesconocidoEsErrorHttpSinInvocarCentral()throws Exception {
        var e=new Escenario("rol",true);new DisponibilidadUsuarioServlet().doGet(e.req,e.resp);assertEquals(400,e.estado);assertEquals(0,e.consultas);
    }
    private static class Escenario {
        String campo;boolean disponible;int consultas,estado=200;
        final StringWriter cuerpo=new StringWriter();final Map<String,String> headers=new HashMap<>();
        final ServicioPortal portal=CsrfTest.doble(ServicioPortal.class,(p,m,a)->{
            assertEquals(campo.equals("nickname")?"disponibleNickname":"disponibleCorreo",m.getName());assertEquals("<script>",a[0]);consultas++;return disponible;
        });
        final ServletContext contexto=CsrfTest.doble(ServletContext.class,(p,m,a)->portal);
        final HttpServletRequest req=CsrfTest.doble(HttpServletRequest.class,(p,m,a)->switch(m.getName()){
            case "getParameter"->a[0].equals("campo")?campo:"<script>";
            case "getServletContext"->contexto;
            default->throw new AssertionError(m.getName());
        });
        final HttpServletResponse resp=CsrfTest.doble(HttpServletResponse.class,(p,m,a)->switch(m.getName()){
            case "setStatus"->{estado=(Integer)a[0];yield null;}
            case "setHeader"->{headers.put((String)a[0],(String)a[1]);yield null;}
            case "setContentType"->{assertEquals("application/json;charset=UTF-8",a[0]);yield null;}
            case "getWriter"->new PrintWriter(cuerpo);
            default->throw new AssertionError(m.getName());
        });
        Escenario(String campo,boolean disponible){this.campo=campo;this.disponible=disponible;}
    }
}
